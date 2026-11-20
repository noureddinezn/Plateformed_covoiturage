package com.api.service.impl;

import com.api.dto.request.TrajetRequestDTO;
import com.api.dto.request.TrajetPointArretRequestDTO;
import com.api.entity.PointArret;
import com.api.dto.response.TrajetResponseDTO;
import com.api.entity.Conducteur;
import com.api.entity.Trajet;
import com.api.entity.Reservation;
import com.api.entity.Vehicule;
import com.api.enums.StatutTrajet;
import com.api.enums.StatutReservation;
import com.api.exception.BusinessException;
import com.api.exception.BadRequestException;
import com.api.exception.ForbiddenException;
import com.api.exception.ResourceNotFoundException;
import com.api.mapper.TrajetMapper;
import com.api.repository.ConducteurRepository;
import com.api.repository.TrajetRepository;
import com.api.repository.VehiculeRepository;
import com.api.service.TrajetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrajetServiceImpl implements TrajetService {

    private final TrajetRepository trajetRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TrajetMapper mapper;

    @Override
    @Transactional
    public TrajetResponseDTO saveTrajet(TrajetRequestDTO requestDTO) {
        preparerPointsArret(requestDTO);
        verifierInformationsTrajet(requestDTO);
        Conducteur conducteur = conducteurRepository.findById(requestDTO.getConducteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable avec l'ID : " + requestDTO.getConducteurId()));

        Vehicule vehicule = vehiculeRepository.findById(requestDTO.getVehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule introuvable avec l'ID : " + requestDTO.getVehiculeId()));

        if (!vehicule.getConducteur().getId().equals(conducteur.getId())) {
            throw new ForbiddenException("Ce vehicule n'appartient pas au conducteur specifie.");
        }

        if (requestDTO.getPlacesProposees() > vehicule.getCapacite()) {
            throw new BadRequestException("Le nombre de places proposees depasse la capacite du vehicule.");
        }

        Trajet trajet = mapper.toEntity(requestDTO);
        trajet.setConducteur(conducteur);
        trajet.setVehicule(vehicule);
        trajet.setStatut(StatutTrajet.PUBLIE);

        if (requestDTO.getPointsArret() != null) {
            requestDTO.getPointsArret().stream().forEach(arret -> {
                PointArret point = new PointArret();
                point.setVille(arret.getVille());
                point.setOrdre(arret.getOrdre());
                point.setHeurePassage(arret.getHeurePassage());
                point.setPrixDepuisDepart(arret.getPrixDepuisDepart());
                trajet.addPointArret(point);
            });
        }

        Trajet savedTrajet = trajetRepository.save(trajet);
        return mapper.toResponseDTO(savedTrajet);
    }

    @Override
    public TrajetResponseDTO getTrajetById(Long id) {
        Trajet trajet = trajetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable avec l'ID : " + id));
        return mapper.toResponseDTO(trajet);
    }

    @Override
    public List<TrajetResponseDTO> getAllTrajets() {
        return trajetRepository.findAll().stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TrajetResponseDTO updateTrajet(Long id, TrajetRequestDTO requestDTO) {
        // 1. Verifier les informations et recuperer les donnees
        verifierInformationsTrajet(requestDTO);
        Long conducteurId = requestDTO.getConducteurId();
        Long vehiculeId = requestDTO.getVehiculeId();
        int placesProposees = requestDTO.getPlacesProposees();
        Trajet trajet = trajetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable avec l'ID : " + id));

        Conducteur conducteur = conducteurRepository.findById(conducteurId)
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable avec l'ID : " + conducteurId));

        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule introuvable avec l'ID : " + vehiculeId));


        if (!vehicule.getConducteur().getId().equals(conducteur.getId())) {
            throw new ForbiddenException("Ce vehicule n'appartient pas au conducteur specifie.");
        }

        if (placesProposees > vehicule.getCapacite()) {
            throw new BadRequestException("Le nombre de places proposees depasse la capacite du vehicule.");
        }

        int placesReservees = trajet.getPlacesProposees() - trajet.getPlacesRestantes();
        if (placesProposees < placesReservees) {
            throw new BusinessException("Le nombre de places proposees ne peut pas etre inferieur aux places deja reservees.");
        }


        trajet.setConducteur(conducteur);
        trajet.setVehicule(vehicule);
        trajet.setVilleDepart(requestDTO.getVilleDepart());
        trajet.setVilleArrivee(requestDTO.getVilleArrivee());
        trajet.setDateDepart(requestDTO.getDateDepart());
        trajet.setPlacesProposees(placesProposees);

        Trajet updatedTrajet = trajetRepository.save(trajet);
        return mapper.toResponseDTO(updatedTrajet);
    }

    @Override
    @Transactional
    public void deleteTrajet(Long id) {
        if (!trajetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trajet introuvable avec l'ID : " + id);
        }
        trajetRepository.deleteById(id);
    }

    @Override
    @Transactional
    public TrajetResponseDTO terminerTrajet(Long id) {
        Trajet trajet = trajetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable avec l'ID : " + id));

        if (LocalDateTime.now().isBefore(trajet.getDateDepart())) {
            throw new BusinessException("Le trajet ne peut pas etre termine avant son depart.");
        }

        trajet.setStatut(StatutTrajet.TERMINE);
        Trajet savedTrajet = trajetRepository.save(trajet);
        return mapper.toResponseDTO(savedTrajet);
    }

    @Override
    @Transactional
    public void annulerTrajet(Long id) {
        Trajet trajet = trajetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable avec l'ID : " + id));

        trajet.setStatut(StatutTrajet.ANNULE);
        trajet.getReservations().stream()
                .filter(reservation -> reservation.getStatut() == StatutReservation.CONFIRMEE)
                .forEach(reservation -> reservation.setStatut(StatutReservation.ANNULEE));
        trajetRepository.save(trajet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrajetResponseDTO> rechercherTrajets(String villeDepart, String villeArrivee) {
        return trajetRepository.rechercherParSegment(villeDepart, villeArrivee)
                .stream()
                .map(trajet -> mapper.toResponseDTO(trajet))
                .toList();
    }

    private void preparerPointsArret(TrajetRequestDTO requestDTO) {
        List<TrajetPointArretRequestDTO> arrets = requestDTO.getPointsArret();
        if (arrets == null || arrets.isEmpty()) {
            return;
        }
        if (arrets.size() < 2) {
            throw new BadRequestException("Il faut au moins deux points d'arret.");
        }
        for (int i = 0; i < arrets.size(); i++) {
            TrajetPointArretRequestDTO arret = arrets.get(i);
            verifierArret(arret, i);

            if (i > 0) {
                TrajetPointArretRequestDTO precedent = arrets.get(i - 1);
                verifierOrdrePassage(precedent, arret);
            }
        }

        TrajetPointArretRequestDTO premier = arrets.get(0);
        TrajetPointArretRequestDTO dernier = arrets.get(arrets.size() - 1);
        if (texteVide(requestDTO.getVilleDepart())) {
            requestDTO.setVilleDepart(premier.getVille());
        }
        if (texteVide(requestDTO.getVilleArrivee())) {
            requestDTO.setVilleArrivee(dernier.getVille());
        }
        if (requestDTO.getDateDepart() == null) {
            requestDTO.setDateDepart(premier.getHeurePassage());
        }
    }

    private void verifierArret(TrajetPointArretRequestDTO arret, int position) {
        if (arret == null) {
            throw new BadRequestException("Chaque arret doit avoir une ville, un ordre, une heure et un prix.");
        }

        boolean informationsManquantes = texteVide(arret.getVille())
                || arret.getOrdre() == null
                || arret.getHeurePassage() == null
                || arret.getPrixDepuisDepart() == null;

        if (informationsManquantes) {
            throw new BadRequestException("Chaque arret doit avoir une ville, un ordre, une heure et un prix.");
        }
        if (arret.getOrdre() != position) {
            throw new BadRequestException("Les ordres doivent commencer a 0 et se suivre sans interruption.");
        }
        if (arret.getPrixDepuisDepart().signum() < 0) {
            throw new BadRequestException("Le prix ne peut pas etre negatif.");
        }
    }

    private void verifierOrdrePassage(TrajetPointArretRequestDTO precedent, TrajetPointArretRequestDTO arret) {
        if (!arret.getHeurePassage().isAfter(precedent.getHeurePassage())) {
            throw new BadRequestException("Les heures des arrets doivent etre strictement croissantes.");
        }
        if (arret.getPrixDepuisDepart().compareTo(precedent.getPrixDepuisDepart()) <= 0) {
            throw new BadRequestException("Les prix des arrets doivent etre strictement croissants.");
        }
    }

    private boolean texteVide(String texte) {
        return texte == null || texte.isBlank();
    }

    private void verifierInformationsTrajet(TrajetRequestDTO requestDTO) {
        if (texteVide(requestDTO.getVilleDepart())
                || texteVide(requestDTO.getVilleArrivee())
                || requestDTO.getDateDepart() == null) {
            throw new BadRequestException("Les villes et la date de depart sont obligatoires sans arrets pour les deduire.");
        }
        if (requestDTO.getDateDepart().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La date de depart doit etre dans le present ou le futur.");
        }
    }
}
