package com.api.controller;

import com.api.dto.TrajetRequestDTO;
import com.api.dto.TrajetResponseDTO;
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

}
