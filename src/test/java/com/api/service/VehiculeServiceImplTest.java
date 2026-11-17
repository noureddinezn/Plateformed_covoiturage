package com.api.service;

import com.api.dto.request.VehiculeRequestDTO;
import com.api.dto.response.VehiculeResponseDTO;
import com.api.entity.Conducteur;
import com.api.entity.Vehicule;
import com.api.exception.BusinessException;
import com.api.exception.ResourceNotFoundException;
import com.api.mapper.VehiculeMapper;
import com.api.repository.ConducteurRepository;
import com.api.repository.VehiculeRepository;
import com.api.service.impl.VehiculeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculeServiceImplTest {

    @Mock
    private VehiculeRepository vehiculeRepository;

    @Mock
    private ConducteurRepository conducteurRepository;

    @Mock
    private VehiculeMapper mapper;

    @InjectMocks
    private VehiculeServiceImpl service;

    private VehiculeRequestDTO requestDTO;
    private Vehicule vehicule;
    private Conducteur conducteur;
    private VehiculeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        conducteur = Conducteur.builder()
                .id(1L)
                .nom("Alami")
                .prenom("Ahmed")
                .email("ahmed@email.com")
                .telephone("0600112233")
                .numeroPermis("AB12345")
                .build();

        requestDTO = VehiculeRequestDTO.builder()
                .marque("Dacia")
                .modele("Logan")
                .immatriculation("12345-A-6")
                .couleur("Blanc")
                .capacite(4)
                .conducteurId(1L)
                .build();

        vehicule = Vehicule.builder()
                .id(1L)
                .marque("Dacia")
                .modele("Logan")
                .immatriculation("12345-A-6")
                .couleur("Blanc")
                .capacite(4)
                .build();

        responseDTO = VehiculeResponseDTO.builder()
                .id(1L)
                .marque("Dacia")
                .modele("Logan")
                .immatriculation("12345-A-6")
                .couleur("Blanc")
                .capacite(4)
                .conducteurId(1L)
                .build();
    }

    @Test
    void saveVehicule_Success() {
        when(vehiculeRepository.existsByImmatriculation(requestDTO.getImmatriculation())).thenReturn(false);
        when(conducteurRepository.findById(requestDTO.getConducteurId())).thenReturn(Optional.of(conducteur));
        when(mapper.toEntity(requestDTO)).thenReturn(vehicule);
        when(vehiculeRepository.save(vehicule)).thenReturn(vehicule);
        when(mapper.toResponseDTO(vehicule)).thenReturn(responseDTO);

        VehiculeResponseDTO result = service.saveVehicule(requestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("12345-A-6", result.getImmatriculation());
        assertSame(conducteur, vehicule.getConducteur());
        verify(vehiculeRepository, times(1)).save(any(Vehicule.class));
    }

    @Test
    void saveVehicule_ImmatriculationExists_ThrowsBusinessException() {
        when(vehiculeRepository.existsByImmatriculation(requestDTO.getImmatriculation())).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.saveVehicule(requestDTO));

        verify(vehiculeRepository, never()).save(any(Vehicule.class));
    }

    @Test
    void saveVehicule_ConducteurNotFound_ThrowsResourceNotFoundException() {
        when(vehiculeRepository.existsByImmatriculation(requestDTO.getImmatriculation())).thenReturn(false);
        when(conducteurRepository.findById(requestDTO.getConducteurId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.saveVehicule(requestDTO));

        verify(vehiculeRepository, never()).save(any(Vehicule.class));
    }

    @Test
    void getVehiculeById_Success() {
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(mapper.toResponseDTO(vehicule)).thenReturn(responseDTO);

        VehiculeResponseDTO result = service.getVehiculeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(vehiculeRepository, times(1)).findById(1L);
    }
}