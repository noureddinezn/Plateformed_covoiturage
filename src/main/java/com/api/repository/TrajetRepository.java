package com.api.repository;

import com.api.entity.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    @Query("""
            SELECT DISTINCT trajet
            FROM Trajet trajet
            JOIN trajet.pointsArret depart
            JOIN trajet.pointsArret arrivee
            WHERE trajet.statut = com.api.enums.StatutTrajet.PUBLIE
            AND LOWER(depart.ville) = LOWER(:villeDepart)
            AND LOWER(arrivee.ville) = LOWER(:villeArrivee)
            AND depart.ordre < arrivee.ordre
            """)
    List<Trajet> rechercherParSegment(@Param("villeDepart") String villeDepart,
                                     @Param("villeArrivee") String villeArrivee);
}
