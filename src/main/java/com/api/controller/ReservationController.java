package com.api.controller;

import com.api.dto.ReservationRequestDTO;
import com.api.dto.response.ReservationResponseDTO;
import com.api.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createReservation(@Valid @RequestBody ReservationRequestDTO requestDTO) {
        return new ResponseEntity<>(service.saveReservation(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(service.getReservationById(id));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservations() {
        return ResponseEntity.ok(service.getAllReservations());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> updateReservation(@PathVariable Long id, @Valid @RequestBody ReservationRequestDTO requestDTO) {
        return ResponseEntity.ok(service.updateReservation(id, requestDTO));
    }

    @PatchMapping("/{id}/annuler")
    public ResponseEntity<Void> annulerReservation(@PathVariable Long id) {
        service.annulerReservation(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        service.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}