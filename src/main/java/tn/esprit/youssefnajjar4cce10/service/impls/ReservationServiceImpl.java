package tn.esprit.youssefnajjar4cce10.service.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.youssefnajjar4cce10.domain.Reservation;
import tn.esprit.youssefnajjar4cce10.exception.ResourceNotFoundException;
import tn.esprit.youssefnajjar4cce10.repository.IReservationRepository;
import tn.esprit.youssefnajjar4cce10.service.IReservationService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements IReservationService {

    private final IReservationRepository reservationRepository;

    @Override
    public Reservation create(Reservation reservation) {
        if (reservation.getIdReservation() != null) {
            throw new IllegalArgumentException("Une nouvelle réservation ne doit pas avoir d'identifiant");
        }
        // La date de fin ne doit pas être antérieure à la date de début
        if (reservation.getDateDebut() != null && reservation.getDateFin() != null
                && reservation.getDateFin().isBefore(reservation.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début");
        }
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    @Override
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation update(Long id, Reservation reservation) {
        Reservation existante = findById(id);
        // Validation
        if (reservation.getDateDebut() != null && reservation.getDateFin() != null
                && reservation.getDateFin().isBefore(reservation.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début");
        }
        // Recopie des attributs modifiables (sans associations)
        existante.setDateDebut(reservation.getDateDebut());
        existante.setDateFin(reservation.getDateFin());
        existante.setStatut(reservation.getStatut());
        return reservationRepository.save(existante);
    }

    @Override
    public void deleteById(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation", id);
        }
        reservationRepository.deleteById(id);
    }
}
