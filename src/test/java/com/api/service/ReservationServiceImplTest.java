package com.api.service;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;
import com.api.mapper.ReservationMapper;
import com.api.repository.PassagerRepository;
import com.api.repository.PointArretRepository;
import com.api.repository.ReservationRepository;
import com.api.repository.TrajetRepository;
import com.api.service.impl.ReservationServiceImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private PassagerRepository passagerRepository;
    @Mock
    private TrajetRepository trajetRepository;
    @Mock
    private PointArretRepository pointArretRepository;
    @Mock
    private ReservationMapper mapper;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ReservationServiceImpl service;

    private ReservationRequestDTO requestDTO;
    private Passager passager;
    private Trajet trajet;
    private PointArret pointMontee;
    private PointArret pointDescente;
    private Reservation reservation;
    private ReservationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(service, "entityManager", entityManager);
        passager = Passager.builder().id(1L).build();
        trajet = Trajet.builder().id(1L).dateDepart(LocalDateTime.now().plusDays(2)).placesProposees(4).reservations(new ArrayList<>()).statut(StatutTrajet.PUBLIE).build();

        pointMontee = PointArret.builder()
                .id(1L).trajet(trajet).ordre(1).prixDepuisDepart(new BigDecimal("0.00")).build();

        pointDescente = PointArret.builder()
                .id(2L).trajet(trajet).ordre(2).prixDepuisDepart(new BigDecimal("50.00")).build(); // 50 درهم

        requestDTO = ReservationRequestDTO.builder()
                .passagerId(1L).trajetId(1L)
                .pointMonteeId(1L).pointDescenteId(2L)
                .nombrePlaces(2)
                .build();

        reservation = Reservation.builder()
                .id(1L).passager(passager).trajet(trajet)
                .pointMontee(pointMontee).pointDescente(pointDescente)
                .nombrePlaces(2).statut(StatutReservation.CONFIRMEE)
                .montantTotal(new BigDecimal("100.00"))
                .build();

        responseDTO = ReservationResponseDTO.builder()
                .id(1L).passagerId(1L).trajetId(1L)
                .pointMonteeId(1L).pointDescenteId(2L)
                .nombrePlaces(2).statut(StatutReservation.CONFIRMEE)
                .montantTotal(new BigDecimal("100.00"))
                .build();
    }

    @Test
    void saveReservation_Success_CalculatesMontantTotal() {
        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(pointArretRepository.findById(1L)).thenReturn(Optional.of(pointMontee));
        when(pointArretRepository.findById(2L)).thenReturn(Optional.of(pointDescente));

        when(mapper.toEntity(requestDTO)).thenReturn(reservation);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(mapper.toResponseDTO(reservation)).thenReturn(responseDTO);

        reservation.setMontantTotal(null);
        reservation.setStatut(null);
        ReservationResponseDTO result = service.saveReservation(requestDTO);
        assertEquals(new BigDecimal("100.00"), reservation.getMontantTotal());
        assertEquals(StatutReservation.CONFIRMEE, reservation.getStatut());
        assertNotNull(reservation.getDateReservation());
        verify(entityManager).lock(trajet, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getMontantTotal());
        assertEquals(StatutReservation.CONFIRMEE, result.getStatut());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void saveReservation_OrdreInvalide_ThrowsBadRequestException() {
        pointMontee.setOrdre(3);

        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(pointArretRepository.findById(1L)).thenReturn(Optional.of(pointMontee));
        when(pointArretRepository.findById(2L)).thenReturn(Optional.of(pointDescente));

        assertThrows(BadRequestException.class, () -> service.saveReservation(requestDTO));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    private void stubReferences() {
        when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(pointArretRepository.findById(1L)).thenReturn(Optional.of(pointMontee));
        when(pointArretRepository.findById(2L)).thenReturn(Optional.of(pointDescente));
    }

    private void stubSave() {
        when(reservationRepository.save(reservation)).thenReturn(reservation);
        when(mapper.toResponseDTO(reservation)).thenReturn(responseDTO);
    }

    @Test
    void saveReservation_LastSeats_MarksTripComplete() {
        stubReferences();
        stubSave();
        requestDTO.setNombrePlaces(4);
        when(mapper.toEntity(requestDTO)).thenReturn(reservation);
        service.saveReservation(requestDTO);
        assertEquals(StatutTrajet.COMPLET, trajet.getStatut());
        assertEquals(new BigDecimal("200.00"), reservation.getMontantTotal());
        verify(trajetRepository).save(trajet);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void saveReservation_InvalidSeatCount(int count) {
        requestDTO.setNombrePlaces(count);
        assertThrows(BusinessException.class, () -> service.saveReservation(requestDTO));
        verifyNoInteractions(reservationRepository, mapper);
    }

    @Test
    void saveReservation_NullSeatCount() {
        requestDTO.setNombrePlaces(null);
        assertThrows(BusinessException.class, () -> service.saveReservation(requestDTO));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void saveReservation_MissingReference(int missing) {
        if (missing > 0) when(passagerRepository.findById(1L)).thenReturn(Optional.of(passager));
        if (missing > 1) when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        if (missing > 2) when(pointArretRepository.findById(1L)).thenReturn(Optional.of(pointMontee));
        assertThrows(ResourceNotFoundException.class, () -> service.saveReservation(requestDTO));
        verifyNoInteractions(reservationRepository, mapper);
    }

    @ParameterizedTest
    @EnumSource(value = StatutTrajet.class, names = {"COMPLET", "TERMINE", "ANNULE"})
    void saveReservation_UnavailableTrip(StatutTrajet statut) {
        stubReferences();
        trajet.setStatut(statut);
        assertThrows(BusinessException.class, () -> service.saveReservation(requestDTO));
        verifyNoInteractions(reservationRepository, mapper);
    }

    @Test
    void saveReservation_NotEnoughSeats() {
        stubReferences();
        trajet.getReservations().add(Reservation.builder().nombrePlaces(3).statut(StatutReservation.CONFIRMEE).build());
        assertThrows(BusinessException.class, () -> service.saveReservation(requestDTO));
        verifyNoInteractions(reservationRepository, mapper);
    }

    @Test
    void saveReservation_CancelledBookingsDoNotConsumeSeats() {
        stubReferences();
        stubSave();
        trajet.getReservations().add(Reservation.builder().nombrePlaces(4).statut(StatutReservation.ANNULEE).build());
        when(mapper.toEntity(requestDTO)).thenReturn(reservation);
        service.saveReservation(requestDTO);
        assertEquals(StatutTrajet.PUBLIE, trajet.getStatut());
    }

    @Test
    void saveReservation_WrongTrip() {
        stubReferences();
        pointMontee.setTrajet(Trajet.builder().id(2L).build());
        assertThrows(BusinessException.class, () -> service.saveReservation(requestDTO));
        verifyNoInteractions(reservationRepository, mapper);
    }

    @Test
    void saveReservation_SameStopOrder() {
        stubReferences();
        pointDescente.setOrdre(1);
        assertThrows(BadRequestException.class, () -> service.saveReservation(requestDTO));
    }

    @Test
    void saveReservation_NegativeFare() {
        stubReferences();
        pointMontee.setPrixDepuisDepart(new BigDecimal("60.00"));
        assertThrows(BusinessException.class, () -> service.saveReservation(requestDTO));
        verifyNoInteractions(reservationRepository, mapper);
    }

    @Test
    void updateReservation_RecalculatesFareAndKeepsId() {
        stubReferences();
        stubSave();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        trajet.getReservations().add(reservation);
        requestDTO.setNombrePlaces(3);
        assertSame(responseDTO, service.updateReservation(1L, requestDTO));
        assertEquals(1L, reservation.getId());
        assertEquals(3, reservation.getNombrePlaces());
        assertEquals(new BigDecimal("150.00"), reservation.getMontantTotal());
    }

    @Test
    void updateReservation_FullTripCanReduceOwnSeats() {
        stubReferences();
        stubSave();
        reservation.setNombrePlaces(4);
        trajet.getReservations().add(reservation);
        trajet.setStatut(StatutTrajet.COMPLET);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        service.updateReservation(1L, requestDTO);
        assertEquals(StatutTrajet.PUBLIE, trajet.getStatut());
        assertEquals(2, trajet.getPlacesRestantes());
    }

    @Test
    void updateReservation_RejectsInsufficientSeats() {
        stubReferences();
        trajet.getReservations().add(reservation);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        requestDTO.setNombrePlaces(5);
        assertThrows(BusinessException.class, () -> service.updateReservation(1L, requestDTO));
        verify(reservationRepository, never()).save(any());
        assertEquals(2, reservation.getNombrePlaces());
    }

    @Test
    void updateReservation_CancelledIsRejected() {
        reservation.setStatut(StatutReservation.ANNULEE);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        assertThrows(BusinessException.class, () -> service.updateReservation(1L, requestDTO));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void annulerReservation_ReopensFullTrip() {
        stubSave();
        reservation.setNombrePlaces(4);
        trajet.getReservations().add(reservation);
        trajet.setStatut(StatutTrajet.COMPLET);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        assertSame(responseDTO, service.annulerReservation(1L));
        assertEquals(StatutReservation.ANNULEE, reservation.getStatut());
        assertEquals(StatutTrajet.PUBLIE, trajet.getStatut());
        assertEquals(4, trajet.getPlacesRestantes());
    }

    @Test
    void annulerReservation_AlreadyCancelled() {
        reservation.setStatut(StatutReservation.ANNULEE);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        assertThrows(BusinessException.class, () -> service.annulerReservation(1L));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void deleteReservation_ReopensFullTrip() {
        reservation.setNombrePlaces(4);
        trajet.getReservations().add(reservation);
        trajet.setStatut(StatutTrajet.COMPLET);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        service.deleteReservation(1L);
        verify(reservationRepository).deleteById(1L);
        assertEquals(StatutTrajet.PUBLIE, trajet.getStatut());
    }

    @Test
    void getReservationById_Success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(mapper.toResponseDTO(reservation)).thenReturn(responseDTO);
        assertSame(responseDTO, service.getReservationById(1L));
    }

    @Test
    void getAllReservations_MapsResults() {
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));
        when(mapper.toResponseDTO(reservation)).thenReturn(responseDTO);
        assertEquals(List.of(responseDTO), service.getAllReservations());
    }

    @Test
    void missingReservation_AllOperationsRejectIt() {
        assertThrows(ResourceNotFoundException.class, () -> service.getReservationById(9L));
        assertThrows(ResourceNotFoundException.class, () -> service.updateReservation(9L, requestDTO));
        assertThrows(ResourceNotFoundException.class, () -> service.annulerReservation(9L));
        assertThrows(ResourceNotFoundException.class, () -> service.deleteReservation(9L));
        verify(reservationRepository, never()).save(any());
        verify(reservationRepository, never()).deleteById(any());
    }

    @Test
    void updateReservation_InvalidOrderThrowsBadRequestException() {
        stubReferences();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        pointMontee.setOrdre(3);
        assertThrows(BadRequestException.class, () -> service.updateReservation(1L, requestDTO));
        verify(reservationRepository, never()).save(any());
    }


    @Test
    void saveReservation_ConfirmedDuplicateIsRejected() {
        stubReferences();
        when(reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(
                1L, 1L, StatutReservation.CONFIRMEE)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.saveReservation(requestDTO));

        assertEquals("Ce passager a deja une reservation confirmee sur ce trajet.", exception.getMessage());
        verify(reservationRepository, never()).save(any());
        verifyNoInteractions(mapper);
    }

    @Test
    void saveReservation_OnlyChecksConfirmedBookings() {
        stubReferences();
        stubSave();
        trajet.getReservations().add(Reservation.builder().passager(passager).trajet(trajet)
                .nombrePlaces(2).statut(StatutReservation.ANNULEE).build());
        when(mapper.toEntity(requestDTO)).thenReturn(reservation);

        assertSame(responseDTO, service.saveReservation(requestDTO));

        verify(reservationRepository).existsByPassagerIdAndTrajetIdAndStatut(
                1L, 1L, StatutReservation.CONFIRMEE);
        verify(reservationRepository).save(reservation);
    }

    @ParameterizedTest
    @ValueSource(ints = {23, 0, -1})
    void annulerReservation_LessThan24HoursIsRejected(int hours) {
        trajet.setDateDepart(LocalDateTime.now().plusHours(hours));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.annulerReservation(1L));

        assertEquals("L'annulation doit etre effectuee au moins 24 heures avant le depart.", exception.getMessage());
        assertEquals(StatutReservation.CONFIRMEE, reservation.getStatut());
        verify(reservationRepository, never()).save(any());
        verify(trajetRepository, never()).save(any());
    }

    @Test
    void annulerReservation_MoreThan24HoursIsAllowed() {
        trajet.setDateDepart(LocalDateTime.now().plusHours(25));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        stubSave();

        assertSame(responseDTO, service.annulerReservation(1L));

        assertEquals(StatutReservation.ANNULEE, reservation.getStatut());
        verify(reservationRepository).save(reservation);
    }
}
