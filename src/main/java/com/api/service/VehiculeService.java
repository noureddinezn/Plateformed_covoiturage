package com.api.service;

import com.api.dto.request.VehiculeRequestDTO;
import com.api.dto.response.VehiculeResponseDTO;
import java.util.List;

public interface VehiculeService {
    VehiculeResponseDTO saveVehicule(VehiculeRequestDTO requestDTO);
    VehiculeResponseDTO getVehiculeById(Long id);
    List<VehiculeResponseDTO> getAllVehicules();
    VehiculeResponseDTO updateVehicule(Long id, VehiculeRequestDTO requestDTO);
    void deleteVehicule(Long id);
}