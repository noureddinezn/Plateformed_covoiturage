package com.api.service.impl;

import com.api.dto.request.PassagerRequestDTO;
import com.api.dto.response.PassagerResponseDTO;
import com.api.entity.Passager;
import com.api.exception.BusinessException;
import com.api.exception.ResourceNotFoundException;
import com.api.mapper.PassagerMapper;
import com.api.repository.PassagerRepository;
import com.api.service.PassagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PassagerServiceImpl implements PassagerService {

    private final PassagerRepository repository;
    private final PassagerMapper mapper;

    @Override
    @Transactional
    public PassagerResponseDTO savePassager(PassagerRequestDTO requestDTO) {
        if (repository.existsByEmail(requestDTO.getEmail())) {
            throw new BusinessException("L'email est deja utilise.");
        }

        Passager passager = mapper.toEntity(requestDTO);
        Passager savedPassager = repository.save(passager);
        return mapper.toResponseDTO(savedPassager);
    }

    @Override
    public PassagerResponseDTO getPassagerById(Long id) {
        Passager passager = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passager introuvable avec l'ID : " + id));
        return mapper.toResponseDTO(passager);
    }

    @Override
    public List<PassagerResponseDTO> getAllPassagers() {
        return repository.findAll().stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PassagerResponseDTO updatePassager(Long id, PassagerRequestDTO requestDTO) {
        Passager passager = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passager introuvable avec l'ID : " + id));

        if (!passager.getEmail().equals(requestDTO.getEmail()) && repository.existsByEmail(requestDTO.getEmail())) {
            throw new BusinessException("L'email est deja utilise.");
        }

        passager.setNom(requestDTO.getNom());
        passager.setPrenom(requestDTO.getPrenom());
        passager.setEmail(requestDTO.getEmail());
        passager.setTelephone(requestDTO.getTelephone());

        Passager updatedPassager = repository.save(passager);
        return mapper.toResponseDTO(updatedPassager);
    }

    @Override
    @Transactional
    public void deletePassager(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Passager introuvable avec l'ID : " + id);
        }
        repository.deleteById(id);
    }
}