package tn.esprit.youssefnajjar4cce10.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.youssefnajjar4cce10.domain.Employe;

public interface IEmployeRepository extends JpaRepository<Employe, Long> {
}
