package com.api.service.impl;

import com.api.dto.request.VehiculeRequestDTO;
import com.api.dto.response.VehiculeResponseDTO;
import com.api.entity.Conducteur;
import com.api.entity.Vehicule;
import com.api.exception.BusinessException;
import com.api.exception.ResourceNotFoundException;
import com.api.mapper.VehiculeMapper;
import com.api.repository.ConducteurRepository;
import com.api.repository.VehiculeRepository;
import com.api.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeMapper mapper;

    @Override
    @Transactional
    public VehiculeResponseDTO saveVehicule(VehiculeRequestDTO requestDTO) {
        if (vehiculeRepository.existsByImmatriculation(requestDTO.getImmatriculation())) {
            throw new BusinessException("L'immatriculation est deja utilisee.");
        }

        Conducteur conducteur = conducteurRepository.findById(requestDTO.getConducteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable avec l'ID : " + requestDTO.getConducteurId()));

        Vehicule vehicule = mapper.toEntity(requestDTO);
        vehicule.setConducteur(conducteur);

        Vehicule savedVehicule = vehiculeRepository.save(vehicule);
        return mapper.toResponseDTO(savedVehicule);
    }

}
