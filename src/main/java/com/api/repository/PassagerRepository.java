package com.api.repository;

import com.api.entity.Passager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassagerRepository extends JpaRepository<Passager, Long> {
    boolean existsByEmail(String email);
}