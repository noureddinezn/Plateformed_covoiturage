package com.api.controller;

import com.api.dto.request.PassagerRequestDTO;
import com.api.dto.response.PassagerResponseDTO;
import com.api.service.PassagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passagers")
@RequiredArgsConstructor
public class PassagerController {

    private final PassagerService service;

    @PostMapping
    public ResponseEntity<PassagerResponseDTO> createPassager(@Valid @RequestBody PassagerRequestDTO requestDTO) {
        return new ResponseEntity<>(service.savePassager(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassagerResponseDTO> getPassager(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPassagerById(id));
    }

    @GetMapping
    public ResponseEntity<List<PassagerResponseDTO>> getAllPassagers() {
        return ResponseEntity.ok(service.getAllPassagers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PassagerResponseDTO> updatePassager(@PathVariable Long id, @Valid @RequestBody PassagerRequestDTO requestDTO) {
        return ResponseEntity.ok(service.updatePassager(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePassager(@PathVariable Long id) {
        service.deletePassager(id);
        return ResponseEntity.noContent().build();
    }
}