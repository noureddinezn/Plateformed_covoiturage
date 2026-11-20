package com.api.service;

import com.api.dto.ReservationRequestDTO;
import com.api.dto.response.ReservationResponseDTO;
import java.util.List;

public interface ReservationService {
    ReservationResponseDTO saveReservation(ReservationRequestDTO requestDTO);
    ReservationResponseDTO getReservationById(Long id);
    List<ReservationResponseDTO> getAllReservations();
    ReservationResponseDTO updateReservation(Long id, ReservationRequestDTO requestDTO);
    void deleteReservation(Long id);
    ReservationResponseDTO annulerReservation(Long id);
}