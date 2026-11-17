package com.api.service;

import com.api.dto.TrajetRequestDTO;
import com.api.dto.TrajetResponseDTO;
import java.util.List;

public interface TrajetService {
    TrajetResponseDTO saveTrajet(TrajetRequestDTO requestDTO);
    TrajetResponseDTO getTrajetById(Long id);
    List<TrajetResponseDTO> getAllTrajets();
    TrajetResponseDTO updateTrajet(Long id, TrajetRequestDTO requestDTO);
    void deleteTrajet(Long id);
}