# Stratégie de fetch et de cascade — AutoLoc (Atelier 2)

Toutes les associations sont en `FetchType.LAZY` : les données liées ne sont chargées que lorsque le code y accède, ce qui évite des requêtes et des jointures inutiles. Les `@ManyToOne` et `@OneToOne`, EAGER par défaut en JPA, sont donc forcés en LAZY. Aucune entité n'utilise `@Data` : `@Getter` / `@Setter` ciblés évitent les boucles infinies de `toString` / `hashCode` sur les associations bidirectionnelles.

| Association | Fetch | Cascade | Justification |
|---|---|---|---|
| Contrat → Paiement | LAZY | ALL + orphanRemoval | Un paiement n'existe que rattaché à son contrat (composition) ; supprimer le contrat supprime ses paiements, et retirer un paiement de la liste le supprime en base. |
| Agence → Vehicule | LAZY | Aucune | Un véhicule survit à la suppression de son agence ; la flotte ne doit pas disparaître avec elle. |
| Agence → Employe | LAZY | Aucune | Un employé n'est pas supprimé avec l'agence ; la collection n'est chargée qu'à la demande. |
| Vehicule ↔ Equipement | LAZY | Aucune | Les équipements sont partagés entre véhicules : supprimer un véhicule ne doit pas supprimer un équipement utilisé par d'autres. Un `Set` évite les doublons et le comportement « bag » d'Hibernate. |
| Client → Reservation | LAZY | PERSIST | Enregistrer un client avec de nouvelles réservations les enregistre aussi ; la suppression n'est pas propagée, l'historique est conservé. |
| Reservation → Vehicule | LAZY | Aucune | Un véhicule existe indépendamment des réservations ; une réservation ne doit pas créer ni supprimer de véhicule. |
| Reservation ↔ Contrat | LAZY | ALL (côté Reservation) | Le contrat est issu de la réservation et n'a pas de sens sans elle : son cycle de vie suit celui de la réservation. |
| Vehicule → Maintenance | LAZY | PERSIST | Une maintenance enregistrée avec son véhicule est persistée avec lui ; la suppression n'est pas propagée pour conserver l'historique. |

## Côté propriétaire

Le côté propriétaire porte la clé étrangère : `Paiement.contrat`, `Vehicule.agence`, `Employe.agence`, `Reservation.client`, `Reservation.vehicule`, `Contrat.reservation`, `Maintenance.vehicule`, et `Vehicule.equipements` (table de jointure `vehicule_equipement`). Les côtés inverses utilisent `mappedBy` et ne créent aucune colonne.
