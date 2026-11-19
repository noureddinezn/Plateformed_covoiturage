package com.api.service;

import com.api.dto.request.PassagerRequestDTO;
import com.api.dto.response.PassagerResponseDTO;
import java.util.List;

public interface PassagerService {
    PassagerResponseDTO savePassager(PassagerRequestDTO requestDTO);
    PassagerResponseDTO getPassagerById(Long id);
    List<PassagerResponseDTO> getAllPassagers();
    PassagerResponseDTO updatePassager(Long id, PassagerRequestDTO requestDTO);
    void deletePassager(Long id);
}