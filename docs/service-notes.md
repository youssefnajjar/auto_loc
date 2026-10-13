# Atelier 4 — Notes : IoC, Injection de Dépendances et couche Service

---

## A. Modes d'injection comparés

| Critère | Injection par constructeur | Injection par attribut (`@Autowired`) |
|---|---|---|
| Champ `final` possible ? | ✅ Oui | ❌ Non |
| Dépendance visible dans la signature de la classe ? | ✅ Oui (dans le constructeur) | ❌ Non (cachée dans la classe) |
| Classe utilisable avec `new` hors conteneur sans dépendance `null` ? | ✅ Oui | ❌ Non (champ null hors conteneur) |
| Signalé par SonarQube for IDE ? | ✅ Non (bonne pratique) | ❌ Oui (règle S6813) |

**Conclusion :** L'injection par constructeur est la seule qui garantit qu'un service ne peut jamais
exister dans un état incomplet. C'est la convention appliquée dans tout le projet AutoLoc.

---

## B. Services — Tableau récapitulatif

| Service | Dépendances injectées | Mode | Justification |
|---|---|---|---|
| `AgenceServiceImpl` | `IAgenceRepository` | Constructeur | Champ `final` + `@RequiredArgsConstructor` ; dépendance obligatoire et immuable |
| `EmployeServiceImpl` | `IEmployeRepository` | Constructeur | Même raison ; l'employé dépend uniquement de son repository |
| `VehiculeServiceImpl` | `IVehiculeRepository` | Constructeur | Même raison ; le service ne connaît que son repository |
| `EquipementServiceImpl` | `IEquipementRepository` | Constructeur | Même raison |
| `ClientServiceImpl` | `IClientRepository` | Constructeur | Même raison |
| `ReservationServiceImpl` | `IReservationRepository` | Constructeur | Même raison |
| `ContratServiceImpl` | `IContratRepository` | Constructeur | Même raison ; la cascade vers Paiement est gérée par JPA |
| `PaiementServiceImpl` | `IPaiementRepository` | Constructeur | Lecture seule ; le repository est quand même injecté pour `findById` et `findAll` |
| `MaintenanceServiceImpl` | `IMaintenanceRepository` | Constructeur | Même raison |

---

## C. Messages d'erreur du conteneur

### Message 1 — `NoSuchBeanDefinitionException`
```
Parameter 0 of constructor in ContratController required a bean of type
'IContratService' that could not be found.
```
**Deux causes probables :**
1. **Annotation oubliée** : `ContratServiceImpl` n'est pas annotée `@Service` → Spring ne la détecte pas comme bean.
2. **Mauvais package** : la classe est placée en dehors de `tn.esprit.youssefnajjar4cce10` → le scan de composants ne la couvre pas.

**Correction :** Ajouter `@Service` sur `ContratServiceImpl` et vérifier qu'elle est bien dans un sous-package de `tn.esprit.youssefnajjar4cce10`.

---

### Message 2 — `NoUniqueBeanDefinitionException`
```
Parameter 0 of constructor in NotificationController required a single bean,
but 2 were found: emailNotificateur, smsNotificateur
```
**Deux corrections possibles :**

1. **`@Primary` sur la classe** : annoter le bean à privilégier par défaut.
   ```java
   @Primary
   @Service
   public class EmailNotificateur implements INotificateur { ... }
   ```

2. **`@Qualifier` sur le paramètre du constructeur** : choisir explicitement le bean à injecter.
   ```java
   public NotificationController(@Qualifier("emailNotificateur") INotificateur notificateur) { ... }
   ```

---

### Message 3 — Dépendance circulaire (`BeanCurrentlyInCreationException`)
```
The dependencies of some of the beans form a cycle:
clientServiceImpl → reservationServiceImpl → clientServiceImpl
```
**Pourquoi l'injection par constructeur rend ce cycle impossible à contourner ?**
Avec l'injection par constructeur, Spring doit créer `ClientServiceImpl` avant `ReservationServiceImpl`
et inversement. Il ne peut pas résoudre le graphe : il lève immédiatement l'exception, sans aucun
contournement possible (contrairement à l'injection par attribut où Spring peut injecter un proxy tardif).

**Piste de conception pour supprimer le cycle :**
Extraire la responsabilité partagée dans un troisième service (ex. `LocationServiceImpl`) qui dépend
des deux repositories sans que `ClientServiceImpl` et `ReservationServiceImpl` se référencent mutuellement.

---

## D. Anomalies SonarQube for IDE

| # | Règle | Explication | Correction apportée |
|---|---|---|---|
| 1 | **S6813** — Injection par attribut avec `@Autowired` | L'injection par attribut masque les dépendances, empêche le champ d'être `final` et rend la classe inutilisable hors conteneur (champ `null`). | Remplacé par `@RequiredArgsConstructor` + champ `private final`. |
| 2 | **S112** — Exception générique levée (`RuntimeException`) | Lever `RuntimeException` directement ne communique aucune information métier à l'appelant. | Remplacé par `ResourceNotFoundException` (exception métier spécifique). |
| 3 | **S1118 / @Data** — Usage de `@Data` sur les entités | `@Data` génère `toString()` récursif sur les associations bidirectionnelles → `StackOverflowError` à l'exécution. | Remplacé par `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` ciblés. |

---

## E. Questions de compréhension

### 1. Où est le `new` de `ContratServiceImpl` et qui l'exécute ?

Il n'y a **aucun `new ContratServiceImpl(...)`** dans le code applicatif.
C'est le **conteneur Spring IoC** qui instancie la classe au démarrage de l'application, lors de la
phase d'instanciation du scan de composants (`@SpringBootApplication` → scan → détection de
`@Service` → appel du constructeur généré par `@RequiredArgsConstructor`).

---

### 2. Pourquoi un contrôleur dépendra-t-il de `IContratService` et non de `ContratServiceImpl` ?

Un contrôleur doit dépendre de **l'interface** `IContratService` et non de l'implémentation concrète
`ContratServiceImpl` pour respecter le principe d'**inversion de dépendances** (DIP) :

- Le contrôleur est découplé de l'implémentation : on peut changer `ContratServiceImpl` par une
  autre implémentation sans toucher au contrôleur.
- Spring injecte le bon bean automatiquement par type (il cherche un bean qui implémente `IContratService`).
- Cela facilite les tests : on peut fournir une implémentation de remplacement (mock) sans modifier le contrôleur.

---

### 3. Pourquoi un service singleton doit-il rester sans état ? Donner un exemple de bug.

Un service singleton est **partagé par tous les threads simultanément**. S'il possède un attribut
modifiable propre à un appel, plusieurs requêtes concurrentes liront et écraseront la même valeur.

**Exemple de bug :**
```java
@Service
public class ContratServiceImpl implements IContratService {
    private Contrat contratCourant; // ← attribut d'état : INTERDIT

    public Contrat create(Contrat contrat) {
        this.contratCourant = contrat;       // Requête A écrit
        // ... traitement long ...
        return contratRepository.save(this.contratCourant); // Requête B a écrasé contratCourant
    }
}
```
La requête A sauvegarde le contrat de la requête B → corruption silencieuse des données.

---

### 4. Pourquoi `update` charge-t-il l'entité existante avant d'appeler `save` ?

Passer directement l'objet reçu à `save` aurait deux effets néfastes :

1. **Écrasement total des colonnes** : `save` avec un id renseigné déclenche un `merge` JPA qui
   remplace toutes les colonnes, y compris celles non envoyées par le client (qui deviendraient `null`).
2. **Destruction des collections** : avec `orphanRemoval = true`, passer un objet sans sa collection
   de `paiements` viderait la liste → tous les paiements liés seraient supprimés.

En chargeant l'existant puis en recopiant uniquement les attributs modifiables, on préserve
l'identifiant, les associations et les collections intactes.

---

### 5. Différences entre `@Component`/`@Bean`, entre `@Primary`/`@Qualifier`, et pourquoi `IPaiementService` est en lecture seule

**`@Component` vs `@Bean` :**
- `@Component` : annotation posée **sur la classe** elle-même ; Spring la détecte via le scan automatique de composants. Utilisé quand on contrôle le code source de la classe.
- `@Bean` : annotation posée **sur une méthode** dans une classe `@Configuration` ; Spring exécute la méthode et enregistre la valeur retournée comme bean. Utilisé pour les classes qu'on ne peut pas annoter (bibliothèques tierces, classes du JDK).

**`@Primary` vs `@Qualifier` :**
- `@Primary` : posé **sur la classe** bean ; en cas d'ambiguïté (plusieurs beans du même type), ce bean est choisi par défaut.
- `@Qualifier("nom")` : posé **sur le paramètre d'injection** ; permet de choisir explicitement un bean précis parmi plusieurs candidats.

**Pourquoi `IPaiementService` n'expose ni création, ni modification, ni suppression ?**
Un paiement est généré automatiquement en cascade depuis un `Contrat` (relation `@OneToMany` avec
`CascadeType.ALL`). Créer, modifier ou supprimer un paiement directement court-circuiterait la logique
métier du contrat (cohérence montant total / paiements, statut du contrat...). Le cycle de vie
d'un paiement est entièrement géré par son contrat.
