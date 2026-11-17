package com.api.service.impl;

import com.api.dto.TrajetRequestDTO;
import com.api.dto.TrajetResponseDTO;
import com.api.entity.Conducteur;
import com.api.entity.Trajet;
import com.api.entity.Vehicule;
import com.api.enums.StatutTrajet;
import com.api.exception.BusinessException;
import com.api.exception.ResourceNotFoundException;
import com.api.mapper.TrajetMapper;
import com.api.repository.ConducteurRepository;
import com.api.repository.TrajetRepository;
import com.api.repository.VehiculeRepository;
import com.api.service.TrajetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrajetServiceImpl implements TrajetService {

    private final TrajetRepository trajetRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TrajetMapper mapper;

    @Override
    @Transactional
    public TrajetResponseDTO saveTrajet(TrajetRequestDTO requestDTO) {
        Conducteur conducteur = conducteurRepository.findById(requestDTO.getConducteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable avec l'ID : " + requestDTO.getConducteurId()));

        Vehicule vehicule = vehiculeRepository.findById(requestDTO.getVehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule introuvable avec l'ID : " + requestDTO.getVehiculeId()));

        if (!vehicule.getConducteur().getId().equals(conducteur.getId())) {
            throw new BusinessException("Ce vehicule n'appartient pas au conducteur specifie.");
        }

        if (requestDTO.getPlacesProposees() > vehicule.getCapacite()) {
            throw new BusinessException("Le nombre de places proposees depasse la capacite du vehicule.");
        }

        Trajet trajet = mapper.toEntity(requestDTO);
        trajet.setConducteur(conducteur);
        trajet.setVehicule(vehicule);
        trajet.setStatut(StatutTrajet.PUBLIE);

        Trajet savedTrajet = trajetRepository.save(trajet);
        return mapper.toResponseDTO(savedTrajet);
    }

    @Override
    public TrajetResponseDTO getTrajetById(Long id) {
        Trajet trajet = trajetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable avec l'ID : " + id));
        return mapper.toResponseDTO(trajet);
    }

    @Override
    public List<TrajetResponseDTO> getAllTrajets() {
        return trajetRepository.findAll().stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TrajetResponseDTO updateTrajet(Long id, TrajetRequestDTO requestDTO) {
        Trajet trajet = trajetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable avec l'ID : " + id));

        Conducteur conducteur = conducteurRepository.findById(requestDTO.getConducteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Conducteur introuvable avec l'ID : " + requestDTO.getConducteurId()));

        Vehicule vehicule = vehiculeRepository.findById(requestDTO.getVehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule introuvable avec l'ID : " + requestDTO.getVehiculeId()));

        if (!vehicule.getConducteur().getId().equals(conducteur.getId())) {
            throw new BusinessException("Ce vehicule n'appartient pas au conducteur specifie.");
        }

        if (requestDTO.getPlacesProposees() > vehicule.getCapacite()) {
            throw new BusinessException("Le nombre de places proposees depasse la capacite du vehicule.");
        }

        trajet.setConducteur(conducteur);
        trajet.setVehicule(vehicule);
        trajet.setVilleDepart(requestDTO.getVilleDepart());
        trajet.setVilleArrivee(requestDTO.getVilleArrivee());
        trajet.setDateDepart(requestDTO.getDateDepart());
        trajet.setPlacesProposees(requestDTO.getPlacesProposees());

        Trajet updatedTrajet = trajetRepository.save(trajet);
        return mapper.toResponseDTO(updatedTrajet);
    }

    @Override
    @Transactional
    public void deleteTrajet(Long id) {
        if (!trajetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trajet introuvable avec l'ID : " + id);
        }
        trajetRepository.deleteById(id);
    }
}