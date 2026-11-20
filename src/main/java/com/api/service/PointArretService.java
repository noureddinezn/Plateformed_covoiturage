package com.api.service;

import com.api.dto.request.PointArretRequestDTO;
import com.api.dto.response.PointArretResponseDTO;
import java.util.List;

public interface PointArretService {
    PointArretResponseDTO savePointArret(PointArretRequestDTO requestDTO);
    PointArretResponseDTO getPointArretById(Long id);
    List<PointArretResponseDTO> getPointsArretByTrajetId(Long trajetId);
    PointArretResponseDTO updatePointArret(Long id, PointArretRequestDTO requestDTO);
    void deletePointArret(Long id);
}