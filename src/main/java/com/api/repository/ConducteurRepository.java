package com.api.repository;

import com.api.entity.Conducteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConducteurRepository extends JpaRepository<Conducteur, Long> {
    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
    boolean existsByNumeroPermis(String numeroPermis);
}