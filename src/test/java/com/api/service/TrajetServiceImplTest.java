package com.api.service;

import com.api.dto.request.TrajetRequestDTO;
import com.api.dto.response.TrajetResponseDTO;
import com.api.entity.Conducteur;
import com.api.entity.Trajet;
import com.api.entity.Reservation;
import com.api.entity.Vehicule;
import com.api.enums.StatutTrajet;
import com.api.enums.StatutReservation;
import com.api.exception.ResourceNotFoundException;
import com.api.exception.BusinessException;
import com.api.exception.BadRequestException;
import com.api.exception.ForbiddenException;
import com.api.mapper.TrajetMapper;
import com.api.repository.ConducteurRepository;
import com.api.repository.TrajetRepository;
import com.api.repository.VehiculeRepository;
import com.api.service.impl.TrajetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;
import java.math.BigDecimal;
import com.api.dto.request.TrajetPointArretRequestDTO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrajetServiceImplTest {

    @Mock
    private TrajetRepository trajetRepository;

    @Mock
    private ConducteurRepository conducteurRepository;

    @Mock
    private VehiculeRepository vehiculeRepository;

    @Mock
    private TrajetMapper mapper;

    @InjectMocks
    private TrajetServiceImpl service;

    private TrajetRequestDTO requestDTO;
    private Conducteur conducteur;
    private Vehicule vehicule;
    private Trajet trajet;
    private TrajetResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        conducteur = Conducteur.builder().id(1L).build();

        vehicule = Vehicule.builder()
                .id(1L)
                .capacite(4)
                .conducteur(conducteur)
                .build();

        requestDTO = TrajetRequestDTO.builder()
                .conducteurId(1L)
                .vehiculeId(1L)
                .villeDepart("Casablanca")
                .villeArrivee("Rabat")
                .dateDepart(LocalDateTime.now().plusDays(1))
                .placesProposees(3)
                .build();

        trajet = Trajet.builder()
                .id(1L)
                .conducteur(conducteur)
                .vehicule(vehicule)
                .placesProposees(3)
                .reservations(new ArrayList<>())
                .statut(StatutTrajet.PUBLIE)
                .build();

        responseDTO = TrajetResponseDTO.builder()
                .id(1L)
                .conducteurId(1L)
                .vehiculeId(1L)
                .placesProposees(3)
                .statut(StatutTrajet.PUBLIE)
                .build();
    }

    @Test
    void saveTrajet_Success() {
        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(mapper.toEntity(requestDTO)).thenReturn(trajet);
        when(trajetRepository.save(any(Trajet.class))).thenReturn(trajet);
        when(mapper.toResponseDTO(trajet)).thenReturn(responseDTO);

        TrajetResponseDTO result = service.saveTrajet(requestDTO);

        assertNotNull(result);
        assertEquals(StatutTrajet.PUBLIE, result.getStatut());
        verify(trajetRepository, times(1)).save(any(Trajet.class));
    }

    @Test
    void saveTrajet_VehiculeNotBelongingToConducteur_ThrowsForbiddenException() {
        Conducteur autreConducteur = Conducteur.builder().id(2L).build();
        vehicule.setConducteur(autreConducteur);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));

        assertThrows(ForbiddenException.class, () -> service.saveTrajet(requestDTO));
        verify(trajetRepository, never()).save(any(Trajet.class));
    }

    @Test
    void saveTrajet_PlacesExceedCapacite_ThrowsBadRequestException() {
        requestDTO.setPlacesProposees(5);

        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));

        assertThrows(BadRequestException.class, () -> service.saveTrajet(requestDTO));
        verify(trajetRepository, never()).save(any(Trajet.class));
    }

    @Test
    void updateTrajet_CannotReduceBelowConfirmedReservations() {
        trajet.getReservations().add(com.api.entity.Reservation.builder()
                .nombrePlaces(3).statut(com.api.enums.StatutReservation.CONFIRMEE).build());
        requestDTO.setPlacesProposees(2);
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        assertThrows(BusinessException.class, () -> service.updateTrajet(1L, requestDTO));
        verify(trajetRepository, never()).save(any());
        assertEquals(3, trajet.getPlacesProposees());
    }

    @Test
    void updateTrajet_CanReduceToExactlyReservedSeats() {
        trajet.setPlacesProposees(4);
        trajet.getReservations().add(com.api.entity.Reservation.builder()
                .nombrePlaces(3).statut(com.api.enums.StatutReservation.CONFIRMEE).build());
        requestDTO.setPlacesProposees(3);
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(trajetRepository.save(trajet)).thenReturn(trajet);
        when(mapper.toResponseDTO(trajet)).thenReturn(responseDTO);
        assertSame(responseDTO, service.updateTrajet(1L, requestDTO));
        assertEquals(0, trajet.getPlacesRestantes());
    }


    @Test
    void updateTrajet_WrongOwnerThrowsForbiddenException() {
        vehicule.setConducteur(Conducteur.builder().id(2L).build());
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        assertThrows(ForbiddenException.class, () -> service.updateTrajet(1L, requestDTO));
        verify(trajetRepository, never()).save(any());
    }

    @Test
    void updateTrajet_ExcessCapacityThrowsBadRequestException() {
        requestDTO.setPlacesProposees(5);
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        assertThrows(BadRequestException.class, () -> service.updateTrajet(1L, requestDTO));
        verify(trajetRepository, never()).save(any());
    }


    @Test
    void terminerTrajet_BeforeDepartureIsRejected() {
        trajet.setDateDepart(LocalDateTime.now().plusDays(1));
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));

        assertThrows(BusinessException.class, () -> service.terminerTrajet(1L));

        assertEquals(StatutTrajet.PUBLIE, trajet.getStatut());
        verify(trajetRepository, never()).save(any());
        verifyNoInteractions(mapper);
    }

    @Test
    void terminerTrajet_AfterDepartureIsAllowed() {
        trajet.setDateDepart(LocalDateTime.now().minusHours(1));
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(trajetRepository.save(trajet)).thenReturn(trajet);
        when(mapper.toResponseDTO(trajet)).thenReturn(responseDTO);

        assertSame(responseDTO, service.terminerTrajet(1L));

        assertEquals(StatutTrajet.TERMINE, trajet.getStatut());
        verify(trajetRepository).save(trajet);
    }

    @Test
    void terminerTrajet_MissingTripIsRejected() {
        assertThrows(ResourceNotFoundException.class, () -> service.terminerTrajet(99L));
        verify(trajetRepository, never()).save(any());
    }

    @Test
    void annulerTrajet_CancelsConfirmedReservationsAndKeepsExistingCancelledOnes() {
        Reservation confirmed = Reservation.builder().nombrePlaces(2)
                .statut(StatutReservation.CONFIRMEE).build();
        Reservation cancelled = Reservation.builder().nombrePlaces(1)
                .statut(StatutReservation.ANNULEE).build();
        Reservation secondConfirmed = Reservation.builder().nombrePlaces(1)
                .statut(StatutReservation.CONFIRMEE).build();
        trajet.getReservations().add(confirmed);
        trajet.getReservations().add(cancelled);
        trajet.getReservations().add(secondConfirmed);
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));

        service.annulerTrajet(1L);

        assertEquals(StatutTrajet.ANNULE, trajet.getStatut());
        assertEquals(StatutReservation.ANNULEE, confirmed.getStatut());
        assertEquals(StatutReservation.ANNULEE, secondConfirmed.getStatut());
        assertEquals(StatutReservation.ANNULEE, cancelled.getStatut());
        assertEquals(3, trajet.getReservations().size());
        assertEquals(3, trajet.getPlacesRestantes());
        verify(trajetRepository).save(trajet);
    }

    @Test
    void annulerTrajet_WithoutReservationsIsAllowed() {
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        service.annulerTrajet(1L);
        assertEquals(StatutTrajet.ANNULE, trajet.getStatut());
        verify(trajetRepository).save(trajet);
    }

    @Test
    void annulerTrajet_MissingTripIsRejected() {
        assertThrows(ResourceNotFoundException.class, () -> service.annulerTrajet(99L));
        verify(trajetRepository, never()).save(any());
    }

    private List<TrajetPointArretRequestDTO> arretsValides() {
        List<TrajetPointArretRequestDTO> arrets = new ArrayList<>();
        arrets.add(TrajetPointArretRequestDTO.builder().ville("Casablanca").ordre(0)
                .heurePassage(LocalDateTime.now().plusDays(2)).prixDepuisDepart(BigDecimal.ZERO).build());
        arrets.add(TrajetPointArretRequestDTO.builder().ville("Settat").ordre(1)
                .heurePassage(LocalDateTime.now().plusDays(2).plusHours(1))
                .prixDepuisDepart(new BigDecimal("50.00")).build());
        return arrets;
    }

    private void stubPublication() {
        when(conducteurRepository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(mapper.toEntity(requestDTO)).thenReturn(trajet);
        when(trajetRepository.save(trajet)).thenReturn(trajet);
        when(mapper.toResponseDTO(trajet)).thenReturn(responseDTO);
    }

    @Test
    void saveTrajet_DerivesMissingFieldsAndLinksStops() {
        List<TrajetPointArretRequestDTO> arrets = arretsValides();
        requestDTO.setPointsArret(arrets);
        requestDTO.setVilleDepart(null);
        requestDTO.setVilleArrivee(null);
        requestDTO.setDateDepart(null);
        stubPublication();
        assertSame(responseDTO, service.saveTrajet(requestDTO));
        assertEquals("Casablanca", requestDTO.getVilleDepart());
        assertEquals("Settat", requestDTO.getVilleArrivee());
        assertEquals(arrets.get(0).getHeurePassage(), requestDTO.getDateDepart());
        assertEquals(2, trajet.getPointsArret().size());
        assertSame(trajet, trajet.getPointsArret().get(0).getTrajet());
        assertSame(trajet, trajet.getPointsArret().get(1).getTrajet());
        assertEquals(new BigDecimal("50.00"), trajet.getPointsArret().get(1).getPrixDepuisDepart());
        verify(trajetRepository).save(trajet);
    }

    @Test
    void saveTrajet_PreservesProvidedFieldsWithStops() {
        requestDTO.setPointsArret(arretsValides());
        LocalDateTime date = requestDTO.getDateDepart();
        stubPublication();
        service.saveTrajet(requestDTO);
        assertEquals("Casablanca", requestDTO.getVilleDepart());
        assertEquals("Rabat", requestDTO.getVilleArrivee());
        assertEquals(date, requestDTO.getDateDepart());
    }

    @Test
    void saveTrajet_EmptyStopListKeepsLegacyFormat() {
        requestDTO.setPointsArret(new ArrayList<>());
        stubPublication();
        service.saveTrajet(requestDTO);
        assertTrue(trajet.getPointsArret().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"single", "start", "gap", "timeEqual", "timeBack", "priceEqual",
            "priceBack", "negative", "nullStop", "nullTime", "blankCity", "past"})
    void saveTrajet_RejectsInvalidStops(String cas) {
        List<TrajetPointArretRequestDTO> arrets = arretsValides();
        if (cas.equals("single")) arrets.remove(1);
        if (cas.equals("start")) arrets.get(0).setOrdre(1);
        if (cas.equals("gap")) arrets.get(1).setOrdre(2);
        if (cas.equals("timeEqual")) arrets.get(1).setHeurePassage(arrets.get(0).getHeurePassage());
        if (cas.equals("timeBack")) arrets.get(1).setHeurePassage(arrets.get(0).getHeurePassage().minusHours(1));
        if (cas.equals("priceEqual")) arrets.get(1).setPrixDepuisDepart(BigDecimal.ZERO);
        if (cas.equals("priceBack")) arrets.get(0).setPrixDepuisDepart(new BigDecimal("60"));
        if (cas.equals("negative")) arrets.get(0).setPrixDepuisDepart(new BigDecimal("-1"));
        if (cas.equals("nullStop")) arrets.set(0, null);
        if (cas.equals("nullTime")) arrets.get(0).setHeurePassage(null);
        if (cas.equals("blankCity")) arrets.get(0).setVille(" ");
        if (cas.equals("past")) arrets.get(0).setHeurePassage(LocalDateTime.now().minusDays(1));
        requestDTO.setPointsArret(arrets);
        requestDTO.setDateDepart(null);
        assertThrows(BadRequestException.class, () -> service.saveTrajet(requestDTO));
        verifyNoInteractions(trajetRepository, mapper);
    }

    @Test
    void saveTrajet_MissingDetailsWithoutStopsIsRejected() {
        requestDTO.setVilleDepart(null);
        assertThrows(BadRequestException.class, () -> service.saveTrajet(requestDTO));
        verifyNoInteractions(trajetRepository, mapper);
    }

    @Test
    void rechercherTrajets_MapsMatchingTrips() {
        when(trajetRepository.rechercherParSegment("Casablanca", "Settat")).thenReturn(List.of(trajet));
        when(mapper.toResponseDTO(trajet)).thenReturn(responseDTO);
        assertEquals(List.of(responseDTO), service.rechercherTrajets("Casablanca", "Settat"));
    }

    @Test
    void rechercherTrajets_NoMatchReturnsEmptyList() {
        when(trajetRepository.rechercherParSegment("Settat", "Casablanca")).thenReturn(List.of());
        assertTrue(service.rechercherTrajets("Settat", "Casablanca").isEmpty());
    }
}
