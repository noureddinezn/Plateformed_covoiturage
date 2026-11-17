package com.api.controller;

import com.api.dto.request.VehiculeRequestDTO;
import com.api.dto.response.VehiculeResponseDTO;
import com.api.service.VehiculeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
public class VehiculeController {

    private final VehiculeService service;

    @PostMapping
    public ResponseEntity<VehiculeResponseDTO> createVehicule(@Valid @RequestBody VehiculeRequestDTO requestDTO) {
        return new ResponseEntity<>(service.saveVehicule(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculeResponseDTO> getVehicule(@PathVariable Long id) {
        return ResponseEntity.ok(service.getVehiculeById(id));
    }

    @GetMapping
    public ResponseEntity<List<VehiculeResponseDTO>> getAllVehicules() {
        return ResponseEntity.ok(service.getAllVehicules());
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculeResponseDTO> updateVehicule(@PathVariable Long id, @Valid @RequestBody VehiculeRequestDTO requestDTO) {
        return ResponseEntity.ok(service.updateVehicule(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicule(@PathVariable Long id) {
        service.deleteVehicule(id);
        return ResponseEntity.noContent().build();
    }
}