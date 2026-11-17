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

}
