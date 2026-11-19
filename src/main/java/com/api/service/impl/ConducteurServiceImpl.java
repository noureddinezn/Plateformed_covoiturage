package com.api.service.impl;

import com.api.dto.request.ConducteurRequestDTO;
import com.api.dto.response.ConducteurResponseDTO;
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

        if (repository.existsByTelephone(requestDTO.getTelephone())) {
            throw new BusinessException("Le telephone est deja utilise.");
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

    @Override
    public List<ConducteurResponseDTO> getAllConducteurs() {
        return repository.findAll().stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public ConducteurResponseDTO updateConducteur(Long id, ConducteurRequestDTO requestDTO) {
        Conducteur conducteur = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable avec l'ID : " + id));

        if (!conducteur.getEmail().equals(requestDTO.getEmail()) && repository.existsByEmail(requestDTO.getEmail())) {
            throw new BusinessException("L'email est deja utilise.");
        }
        if (!conducteur.getNumeroPermis().equals(requestDTO.getNumeroPermis()) && repository.existsByNumeroPermis(requestDTO.getNumeroPermis())) {
            throw new BusinessException("Le numero de permis est deja utilise.");
        }

        if (!conducteur.getTelephone().equals(requestDTO.getTelephone())
                && repository.existsByTelephone(requestDTO.getTelephone())) {
            throw new BusinessException("Le telephone est deja utilise.");
        }

        conducteur.setNom(requestDTO.getNom());
        conducteur.setPrenom(requestDTO.getPrenom());
        conducteur.setEmail(requestDTO.getEmail());
        conducteur.setTelephone(requestDTO.getTelephone());
        conducteur.setNumeroPermis(requestDTO.getNumeroPermis());

        Conducteur updatedConducteur = repository.save(conducteur);
        return mapper.toResponseDTO(updatedConducteur);
    }

    @Override
    @Transactional
    public void deleteConducteur(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Conducteur introuvable avec l'ID : " + id);
        }
        repository.deleteById(id);
    }
}