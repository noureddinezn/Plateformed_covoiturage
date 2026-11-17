package com.api.service.impl;

import com.api.dto.ConducteurRequestDTO;
import com.api.dto.ConducteurResponseDTO;
import com.api.entity.Conducteur;
import com.api.exception.BusinessException;
import com.api.exception.ResourceNotFoundException;
import com.api.mapper.ConducteurMapper;
import com.api.repository.ConducteurRepository;
import com.api.service.ConducteurService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConducteurServiceImpl implements ConducteurService {

    private final ConducteurRepository repository;
    private final ConducteurMapper mapper;

    @Override
    @Transactional
    public ConducteurResponseDTO saveConducteur(ConducteurRequestDTO requestDTO) {
        if (repository.existsByEmail(requestDTO.getEmail())) {
            throw new BusinessException("L'email est deja utilise.");
        }
        if (repository.existsByNumeroPermis(requestDTO.getNumeroPermis())) {
            throw new BusinessException("Le numero de permis est deja utilise.");
        }

        Conducteur conducteur = mapper.toEntity(requestDTO);
        Conducteur savedConducteur = repository.save(conducteur);

        return mapper.toResponseDTO(savedConducteur);
    }

    @Override
    public ConducteurResponseDTO getConducteurById(Long id) {
        Conducteur conducteur = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable avec l'ID : " + id));
        return mapper.toResponseDTO(conducteur);
    }

}
