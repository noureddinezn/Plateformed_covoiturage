package com.api.service;

import com.api.dto.request.TrajetRequestDTO;
import com.api.dto.response.TrajetResponseDTO;
import java.util.List;

public interface TrajetService {
    TrajetResponseDTO saveTrajet(TrajetRequestDTO requestDTO);
    TrajetResponseDTO getTrajetById(Long id);
    List<TrajetResponseDTO> getAllTrajets();
    TrajetResponseDTO updateTrajet(Long id, TrajetRequestDTO requestDTO);
    void deleteTrajet(Long id);
    TrajetResponseDTO terminerTrajet(Long id);
    void annulerTrajet(Long id);
    List<TrajetResponseDTO> rechercherTrajets(String villeDepart, String villeArrivee);
}