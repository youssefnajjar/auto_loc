package tn.esprit.youssefnajjar4cce10.service.impls;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.youssefnajjar4cce10.domain.Vehicule;
import tn.esprit.youssefnajjar4cce10.repository.IVehiculeRepository;
import tn.esprit.youssefnajjar4cce10.service.IVehiculeServices;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements IVehiculeServices {

    private final IVehiculeRepository vehiculeRepository;

    @Override
    public Vehicule create(Vehicule vehicule) {
        return vehiculeRepository.save(vehicule);
    }

    @Override
    public Vehicule findById(long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Véhicule introuvable avec l'id : " + id));
    }

    @Override
    public List<Vehicule> findAll() {
        return vehiculeRepository.findAll();
    }

    @Override
    public void deleteById(long id) {
        vehiculeRepository.deleteById(id);
    }

    @Override
    public Vehicule update(Vehicule vehicule) {
        Long id = vehicule.getIdVehicule();
        if (id == null || !vehiculeRepository.existsById(id)) {
            throw new EntityNotFoundException("Véhicule introuvable avec l'id : " + id);
        }
        return vehiculeRepository.save(vehicule);
    }
}
