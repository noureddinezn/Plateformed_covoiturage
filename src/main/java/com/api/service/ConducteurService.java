package com.api.service;

import com.api.dto.request.ConducteurRequestDTO;
import com.api.dto.response.ConducteurResponseDTO;
import java.util.List;

public interface ConducteurService {
    ConducteurResponseDTO saveConducteur(ConducteurRequestDTO requestDTO);
    ConducteurResponseDTO getConducteurById(Long id);
    List<ConducteurResponseDTO> getAllConducteurs();
    ConducteurResponseDTO updateConducteur(Long id, ConducteurRequestDTO requestDTO);
    void deleteConducteur(Long id);
}