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

}
