package com.api.controller;

import com.api.dto.request.TrajetRequestDTO;
import com.api.dto.response.TrajetResponseDTO;
import com.api.service.TrajetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trajets")
@RequiredArgsConstructor
public class TrajetController {

    private final TrajetService service;

    @PostMapping
    public ResponseEntity<TrajetResponseDTO> createTrajet(@Valid @RequestBody TrajetRequestDTO requestDTO) {
        return new ResponseEntity<>(service.saveTrajet(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrajetResponseDTO> getTrajet(@PathVariable Long id) {
        return ResponseEntity.ok(service.getTrajetById(id));
    }

    @GetMapping
    public ResponseEntity<List<TrajetResponseDTO>> getAllTrajets() {
        return ResponseEntity.ok(service.getAllTrajets());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrajetResponseDTO> updateTrajet(@PathVariable Long id, @Valid @RequestBody TrajetRequestDTO requestDTO) {
        return ResponseEntity.ok(service.updateTrajet(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrajet(@PathVariable Long id) {
        service.deleteTrajet(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/terminer")
    public ResponseEntity<TrajetResponseDTO> terminerTrajet(@PathVariable Long id) {
        return ResponseEntity.ok(service.terminerTrajet(id));
    }

    @PatchMapping("/{id}/annuler")
    public ResponseEntity<Void> annulerTrajet(@PathVariable Long id) {
        service.annulerTrajet(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<TrajetResponseDTO>> rechercherTrajets(
            @RequestParam("villeDepart") String villeDepart,
            @RequestParam("villeArrivee") String villeArrivee) {
        return ResponseEntity.ok(service.rechercherTrajets(villeDepart, villeArrivee));
    }
}
