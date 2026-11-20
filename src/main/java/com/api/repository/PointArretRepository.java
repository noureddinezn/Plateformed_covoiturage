package com.api.repository;

import com.api.entity.PointArret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointArretRepository extends JpaRepository<PointArret, Long> {
    List<PointArret> findByTrajetIdOrderByOrdreAsc(Long trajetId);
}