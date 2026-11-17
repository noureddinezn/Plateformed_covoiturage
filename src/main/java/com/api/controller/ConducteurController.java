package com.api.controller;

import com.api.dto.ConducteurRequestDTO;
import com.api.dto.ConducteurResponseDTO;
import com.api.service.ConducteurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conducteurs")
@RequiredArgsConstructor
public class ConducteurController {

    private final ConducteurService service;

    @PostMapping
    public ResponseEntity<ConducteurResponseDTO> createConducteur(@Valid @RequestBody ConducteurRequestDTO requestDTO) {
        return new ResponseEntity<>(service.saveConducteur(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConducteurResponseDTO> getConducteur(@PathVariable Long id) {
        return ResponseEntity.ok(service.getConducteurById(id));
    }

    @GetMapping
    public ResponseEntity<List<ConducteurResponseDTO>> getAllConducteurs() {
        return ResponseEntity.ok(service.getAllConducteurs());
    }
    @PutMapping("/{id}")
    public ResponseEntity<ConducteurResponseDTO> updateConducteur(@PathVariable Long id, @Valid @RequestBody ConducteurRequestDTO requestDTO) {
        return ResponseEntity.ok(service.updateConducteur(id, requestDTO));
    }

}
