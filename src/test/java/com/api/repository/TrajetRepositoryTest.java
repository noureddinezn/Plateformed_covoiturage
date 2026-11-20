package com.api.repository;

import com.api.entity.*;
import com.api.enums.StatutTrajet;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TrajetRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TrajetRepository repository;

    private Trajet creerTrajet(StatutTrajet statut) {
        Conducteur conducteur = Conducteur.builder().nom("Alami").prenom("Ahmed")
                .email("segment@example.com").telephone("0612345678").numeroPermis("TEST-1").build();
        entityManager.persist(conducteur);
        Vehicule vehicule = Vehicule.builder().marque("Dacia").modele("Logan")
                .immatriculation("TEST-123").couleur("Blanc").capacite(4).conducteur(conducteur).build();
        entityManager.persist(vehicule);
        Trajet trajet = Trajet.builder().conducteur(conducteur).vehicule(vehicule)
                .villeDepart("Casablanca").villeArrivee("Marrakech")
                .dateDepart(LocalDateTime.now().plusDays(2)).placesProposees(4).statut(statut).build();
        String[] villes = {"Casablanca", "Settat", "Marrakech", "Settat"};
        for (int i = 0; i < villes.length; i++) {
            trajet.addPointArret(PointArret.builder().ville(villes[i]).ordre(i)
                    .heurePassage(trajet.getDateDepart().plusHours(i))
                    .prixDepuisDepart(BigDecimal.valueOf(i * 20)).build());
        }
        entityManager.persistAndFlush(trajet);
        entityManager.clear();
        return trajet;
    }

    @Test
    void findsIntermediateSegmentIgnoringCaseWithoutDuplicates() {
        Trajet trajet = creerTrajet(StatutTrajet.PUBLIE);
        List<Trajet> resultats = repository.rechercherParSegment("cAsAbLaNcA", "sEtTaT");
        assertEquals(1, resultats.size());
        assertEquals(trajet.getId(), resultats.get(0).getId());
        assertEquals(4, resultats.get(0).getPointsArret().size());
    }

    @Test
    void reverseDirectionReturnsEmptyList() {
        creerTrajet(StatutTrajet.PUBLIE);
        assertTrue(repository.rechercherParSegment("Settat", "Casablanca").isEmpty());
    }

    @Test
    void unknownCityReturnsEmptyList() {
        creerTrajet(StatutTrajet.PUBLIE);
        assertTrue(repository.rechercherParSegment("Fes", "Settat").isEmpty());
    }

    @ParameterizedTest
    @EnumSource(value = StatutTrajet.class, names = {"COMPLET", "TERMINE", "ANNULE"})
    void ignoresUnpublishedTrips(StatutTrajet statut) {
        creerTrajet(statut);
        assertTrue(repository.rechercherParSegment("Casablanca", "Settat").isEmpty());
    }
}
