package com.api.service.impl;

import com.api.dto.ReservationRequestDTO;
import com.api.dto.response.ReservationResponseDTO;
import com.api.entity.Passager;
import com.api.entity.PointArret;
import com.api.entity.Reservation;
import com.api.entity.Trajet;
import com.api.enums.StatutReservation;
import com.api.enums.StatutTrajet;
import com.api.exception.BusinessException;
import com.api.exception.BadRequestException;
import com.api.exception.ResourceNotFoundException;
import com.api.mapper.ReservationMapper;
import com.api.repository.PassagerRepository;
import com.api.repository.PointArretRepository;
import com.api.repository.ReservationRepository;
import com.api.repository.TrajetRepository;
import com.api.service.ReservationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final PassagerRepository passagerRepository;
    private final TrajetRepository trajetRepository;
    private final PointArretRepository pointArretRepository;
    private final ReservationMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public ReservationResponseDTO saveReservation(ReservationRequestDTO requestDTO) {
        verifierNombrePlaces(requestDTO);
        Passager passager = trouverPassager(requestDTO.getPassagerId());

        Trajet trajet = trouverTrajet(requestDTO.getTrajetId());

        entityManager.lock(trajet, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        PointArret pointMontee = trouverPointArret(requestDTO.getPointMonteeId(), "montee");

        PointArret pointDescente = trouverPointArret(requestDTO.getPointDescenteId(), "descente");

        verifierPointsArret(trajet, pointMontee, pointDescente);
        int placesRestantes = verifierDisponibilite(trajet, requestDTO.getNombrePlaces());
        BigDecimal montantTotal = calculerMontantTotal(pointMontee, pointDescente, requestDTO.getNombrePlaces());
        verifierAbsenceReservationConfirmee(requestDTO.getPassagerId(), requestDTO.getTrajetId());

        Reservation reservation = mapper.toEntity(requestDTO);
        reservation.setPassager(passager);
        reservation.setTrajet(trajet);
        reservation.setPointMontee(pointMontee);
        reservation.setPointDescente(pointDescente);
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setStatut(StatutReservation.CONFIRMEE);
        reservation.setMontantTotal(montantTotal);

        Reservation savedReservation = reservationRepository.save(reservation);

        if (placesRestantes == requestDTO.getNombrePlaces()) {
            changerStatutTrajet(trajet, StatutTrajet.COMPLET);
        }

        return mapper.toResponseDTO(savedReservation);
    }


    @Override
    public ReservationResponseDTO getReservationById(Long id) {
        return mapper.toResponseDTO(trouverReservation(id));
    }

    @Override
    public List<ReservationResponseDTO> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public ReservationResponseDTO updateReservation(Long id, ReservationRequestDTO requestDTO) {

        Reservation reservation = trouverReservation(id);
        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new BusinessException("Une reservation annulee ne peut pas etre modifiee.");
        }
        verifierNombrePlaces(requestDTO);
        Passager passager = trouverPassager(requestDTO.getPassagerId());
        Trajet trajet = trouverTrajet(requestDTO.getTrajetId());
        entityManager.lock(trajet, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        PointArret montee = trouverPointArret(requestDTO.getPointMonteeId(), "montee");
        PointArret descente = trouverPointArret(requestDTO.getPointDescenteId(), "descente");
        verifierPointsArret(trajet, montee, descente);
        Trajet ancienTrajet = reservation.getTrajet();
        boolean memeTrajet = ancienTrajet.getId().equals(trajet.getId());
        verifierTrajetModifiable(trajet, memeTrajet);
        int anciennesPlaces = reservation.getNombrePlaces();
        int ancienDisponible = ancienTrajet.getPlacesRestantes();
        int disponible = trajet.getPlacesRestantes() + (memeTrajet ? anciennesPlaces : 0);
        verifierPlacesDisponibles(disponible, requestDTO.getNombrePlaces());
        BigDecimal montantTotal = calculerMontantTotal(montee, descente, requestDTO.getNombrePlaces());
        reservation.setPassager(passager);
        reservation.setTrajet(trajet);
        reservation.setPointMontee(montee);
        reservation.setPointDescente(descente);
        reservation.setNombrePlaces(requestDTO.getNombrePlaces());
        reservation.setMontantTotal(montantTotal);
        Reservation saved = reservationRepository.save(reservation);
        if (!memeTrajet && ancienTrajet.getStatut() == StatutTrajet.COMPLET && ancienDisponible + anciennesPlaces > 0) {
            changerStatutTrajet(ancienTrajet, StatutTrajet.PUBLIE);
        }
        StatutTrajet statut = disponible == requestDTO.getNombrePlaces() ? StatutTrajet.COMPLET : StatutTrajet.PUBLIE;
        changerStatutTrajet(trajet, statut);
        return mapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = trouverReservation(id);
        Trajet trajet = reservation.getTrajet();
        boolean liberer = reservation.getStatut() == StatutReservation.CONFIRMEE
                && trajet.getStatut() == StatutTrajet.COMPLET
                && trajet.getPlacesRestantes() + reservation.getNombrePlaces() > 0;
        reservationRepository.deleteById(id);
        if (liberer) {
            changerStatutTrajet(trajet, StatutTrajet.PUBLIE);
        }
    }

    @Override
    @Transactional
    public ReservationResponseDTO annulerReservation(Long id) {
        Reservation reservation = trouverReservation(id);

        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new BusinessException("La reservation est deja annulee.");
        }

        Trajet trajet = reservation.getTrajet();
        if (LocalDateTime.now().plusHours(24).isAfter(trajet.getDateDepart())) {
            throw new BusinessException("L'annulation doit etre effectuee au moins 24 heures avant le depart.");
        }
        int disponiblesApresAnnulation = trajet.getPlacesRestantes() + reservation.getNombrePlaces();
        reservation.setStatut(StatutReservation.ANNULEE);
        Reservation savedReservation = reservationRepository.save(reservation);

        if (trajet.getStatut() == StatutTrajet.COMPLET && disponiblesApresAnnulation > 0) {
            changerStatutTrajet(trajet, StatutTrajet.PUBLIE);
        }

        return mapper.toResponseDTO(savedReservation);
    }

    private Reservation trouverReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation introuvable avec l'ID : " + id));
    }

    private Passager trouverPassager(Long id) {
        return passagerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passager introuvable avec l'ID : " + id));
    }

    private Trajet trouverTrajet(Long id) {
        return trajetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable avec l'ID : " + id));
    }

    private PointArret trouverPointArret(Long id, String type) {
        return pointArretRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Point de " + type + " introuvable"));
    }

    private void changerStatutTrajet(Trajet trajet, StatutTrajet statut) {
        if (trajet.getStatut() != statut) {
            trajet.setStatut(statut);
            trajetRepository.save(trajet);
        }
    }

    private void verifierTrajetModifiable(Trajet trajet, boolean memeTrajet) {
        boolean modificationSurTrajetComplet = memeTrajet && trajet.getStatut() == StatutTrajet.COMPLET;
        if (trajet.getStatut() != StatutTrajet.PUBLIE && !modificationSurTrajetComplet) {
            throw new BusinessException("Ce trajet n'est plus disponible pour la reservation.");
        }
    }

    private void verifierPlacesDisponibles(int disponibles, int demandees) {
        if (disponibles < demandees) {
            throw new BusinessException("Il n'y a pas assez de places disponibles pour ce trajet.");
        }
    }

    private void verifierPointsArret(Trajet trajet, PointArret montee, PointArret descente) {
        if (!montee.getTrajet().getId().equals(trajet.getId())
                || !descente.getTrajet().getId().equals(trajet.getId())) {
            throw new BusinessException("Les points d'arret ne correspondent pas au trajet choisi.");
        }
        if (montee.getOrdre() >= descente.getOrdre()) {
            throw new BadRequestException("Le point de montee doit preceder le point de descente.");
        }
    }

    private int verifierDisponibilite(Trajet trajet, int nombrePlaces) {
        if (trajet.getStatut() != StatutTrajet.PUBLIE) {
            throw new BusinessException("Ce trajet n'est plus disponible pour la reservation.");
        }
        int placesRestantes = trajet.getPlacesRestantes();
        verifierPlacesDisponibles(placesRestantes, nombrePlaces);
        return placesRestantes;
    }

    private BigDecimal calculerMontantTotal(PointArret montee, PointArret descente, int nombrePlaces) {
        BigDecimal prixUnitaire = descente.getPrixDepuisDepart().subtract(montee.getPrixDepuisDepart());
        if (prixUnitaire.signum() < 0) {
            throw new BusinessException("Le prix de descente doit etre superieur ou egal au prix de montee.");
        }
        return prixUnitaire.multiply(BigDecimal.valueOf(nombrePlaces));
    }

    private void verifierAbsenceReservationConfirmee(Long passagerId, Long trajetId) {
        if (reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(
                passagerId, trajetId, StatutReservation.CONFIRMEE)) {
            throw new BusinessException("Ce passager a deja une reservation confirmee sur ce trajet.");
        }
    }

    private void verifierNombrePlaces(ReservationRequestDTO requestDTO) {
        if (requestDTO.getNombrePlaces() == null || requestDTO.getNombrePlaces() < 1) {
            throw new BusinessException("Il faut reserver au moins 1 place.");
        }
    }
}
