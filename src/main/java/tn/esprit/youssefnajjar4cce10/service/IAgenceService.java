package tn.esprit.youssefnajjar4cce10.service;

import tn.esprit.youssefnajjar4cce10.domain.Agence;

import java.util.List;

public interface IAgenceService {
    Agence create(Agence agence);
    Agence findById(Long id);
    List<Agence> findAll();
    Agence update(Long id, Agence agence);
    void deleteById(Long id);
}
