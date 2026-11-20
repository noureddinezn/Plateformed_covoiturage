package com.api.controller;

import com.api.dto.request.PointArretRequestDTO;
import com.api.dto.response.PointArretResponseDTO;
import com.api.service.PointArretService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points-arret")
@RequiredArgsConstructor
public class PointArretController {

    private final PointArretService service;

    @PostMapping
    public ResponseEntity<PointArretResponseDTO> createPointArret(@Valid @RequestBody PointArretRequestDTO requestDTO) {
        return new ResponseEntity<>(service.savePointArret(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PointArretResponseDTO> getPointArret(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPointArretById(id));
    }

    @GetMapping("/trajet/{trajetId}")
    public ResponseEntity<List<PointArretResponseDTO>> getPointsArretByTrajetId(@PathVariable Long trajetId) {
        return ResponseEntity.ok(service.getPointsArretByTrajetId(trajetId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PointArretResponseDTO> updatePointArret(@PathVariable Long id, @Valid @RequestBody PointArretRequestDTO requestDTO) {
        return ResponseEntity.ok(service.updatePointArret(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePointArret(@PathVariable Long id) {
        service.deletePointArret(id);
        return ResponseEntity.noContent().build();
    }
}