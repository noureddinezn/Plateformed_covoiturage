package com.api.service.impl;

import com.api.dto.request.PointArretRequestDTO;
import com.api.dto.response.PointArretResponseDTO;
import com.api.entity.PointArret;
import com.api.entity.Trajet;
import com.api.exception.ResourceNotFoundException;
import com.api.mapper.PointArretMapper;
import com.api.repository.PointArretRepository;
import com.api.repository.TrajetRepository;
import com.api.service.PointArretService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointArretServiceImpl implements PointArretService {

    private final PointArretRepository pointArretRepository;
    private final TrajetRepository trajetRepository;
    private final PointArretMapper mapper;

    @Override
    @Transactional
    public PointArretResponseDTO savePointArret(PointArretRequestDTO requestDTO) {
        Trajet trajet = trajetRepository.findById(requestDTO.getTrajetId())
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable avec l'ID : " + requestDTO.getTrajetId()));

        PointArret pointArret = mapper.toEntity(requestDTO);
        pointArret.setTrajet(trajet);

        PointArret savedPointArret = pointArretRepository.save(pointArret);
        return mapper.toResponseDTO(savedPointArret);
    }

    @Override
    public PointArretResponseDTO getPointArretById(Long id) {
        PointArret pointArret = pointArretRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PointArret introuvable avec l'ID : " + id));
        return mapper.toResponseDTO(pointArret);
    }

    @Override
    public List<PointArretResponseDTO> getPointsArretByTrajetId(Long trajetId) {
        if (!trajetRepository.existsById(trajetId)) {
            throw new ResourceNotFoundException("Trajet introuvable avec l'ID : " + trajetId);
        }
        return pointArretRepository.findByTrajetIdOrderByOrdreAsc(trajetId).stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PointArretResponseDTO updatePointArret(Long id, PointArretRequestDTO requestDTO) {
        PointArret pointArret = pointArretRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PointArret introuvable avec l'ID : " + id));

        Trajet trajet = trajetRepository.findById(requestDTO.getTrajetId())
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable avec l'ID : " + requestDTO.getTrajetId()));

        pointArret.setTrajet(trajet);
        pointArret.setVille(requestDTO.getVille());
        pointArret.setOrdre(requestDTO.getOrdre());
        pointArret.setHeurePassage(requestDTO.getHeurePassage());
        pointArret.setPrixDepuisDepart(requestDTO.getPrixDepuisDepart());

        PointArret updatedPointArret = pointArretRepository.save(pointArret);
        return mapper.toResponseDTO(updatedPointArret);
    }

    @Override
    @Transactional
    public void deletePointArret(Long id) {
        if (!pointArretRepository.existsById(id)) {
            throw new ResourceNotFoundException("PointArret introuvable avec l'ID : " + id);
        }
        pointArretRepository.deleteById(id);
    }
}