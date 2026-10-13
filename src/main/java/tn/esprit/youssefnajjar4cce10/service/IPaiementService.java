package tn.esprit.youssefnajjar4cce10.service;

import tn.esprit.youssefnajjar4cce10.domain.Paiement;

import java.util.List;

// Paiement : lecture seule (généré par un contrat, jamais créé directement)
public interface IPaiementService {
    Paiement findById(Long id);
    List<Paiement> findAll();
}
