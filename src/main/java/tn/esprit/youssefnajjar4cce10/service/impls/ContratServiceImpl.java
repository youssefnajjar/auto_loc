package tn.esprit.youssefnajjar4cce10.service.impls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.youssefnajjar4cce10.domain.Contrat;
import tn.esprit.youssefnajjar4cce10.exception.ResourceNotFoundException;
import tn.esprit.youssefnajjar4cce10.repository.IContratRepository;
import tn.esprit.youssefnajjar4cce10.service.IContratService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map; // import inutile → déclenche S1128

@Service
public class ContratServiceImpl implements IContratService {

    @Autowired // injection par attribut → déclenche S6813
    private IContratRepository contratRepository;

    @Override
    public Contrat create(Contrat contrat) {
        if (contrat.getIdContrat() != null) {
            throw new IllegalArgumentException("Un nouveau contrat ne doit pas avoir d'identifiant");
        }
        if (contrat.getMontantTotal() != null
                && contrat.getMontantTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le montant total du contrat ne peut pas être négatif");
        }
        return contratRepository.save(contrat);
    }

    @Override
    public Contrat findById(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", id));
    }

    @Override
    public List<Contrat> findAll() {
        return contratRepository.findAll();
    }

    @Override
    public Contrat update(Long id, Contrat contrat) {
        Contrat existant = findById(id);
        if (contrat.getMontantTotal() != null
                && contrat.getMontantTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le montant total du contrat ne peut pas être négatif");
        }
        existant.setDateSignature(contrat.getDateSignature());
        existant.setMontantTotal(contrat.getMontantTotal());
        existant.setValide(contrat.isValide());
        return contratRepository.save(existant);
    }

    @Override
    public void deleteById(Long id) {
        if (!contratRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contrat", id);
        }
        contratRepository.deleteById(id);
    }
}
