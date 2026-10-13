package tn.esprit.youssefnajjar4cce10.service.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.youssefnajjar4cce10.domain.Paiement;
import tn.esprit.youssefnajjar4cce10.exception.ResourceNotFoundException;
import tn.esprit.youssefnajjar4cce10.repository.IPaiementRepository;
import tn.esprit.youssefnajjar4cce10.service.IPaiementService;

import java.util.List;

// Lecture seule : un paiement est généré via un contrat, pas créé directement
@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements IPaiementService {

    private final IPaiementRepository paiementRepository;

    @Override
    public Paiement findById(Long id) {
        return paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", id));
    }

    @Override
    public List<Paiement> findAll() {
        return paiementRepository.findAll();
    }
}
