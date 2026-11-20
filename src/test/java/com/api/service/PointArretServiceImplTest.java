package com.api.service;

import com.api.dto.request.PointArretRequestDTO;
import com.api.dto.response.PointArretResponseDTO;
import com.api.entity.PointArret;
import com.api.entity.Trajet;
import com.api.exception.ResourceNotFoundException;
import com.api.mapper.PointArretMapper;
import com.api.repository.PointArretRepository;
import com.api.repository.TrajetRepository;
import com.api.service.impl.PointArretServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointArretServiceImplTest {

    @Mock
    private PointArretRepository pointArretRepository;

    @Mock
    private TrajetRepository trajetRepository;

    @Mock
    private PointArretMapper mapper;

    @InjectMocks
    private PointArretServiceImpl service;

    private PointArretRequestDTO requestDTO;
    private Trajet trajet;
    private PointArret pointArret;
    private PointArretResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        trajet = Trajet.builder().id(1L).build();

        requestDTO = PointArretRequestDTO.builder()
                .trajetId(1L)
                .ville("Rabat")
                .ordre(1)
                .heurePassage(LocalDateTime.now().plusHours(1))
                .prixDepuisDepart(new BigDecimal("40.00"))
                .build();

        pointArret = PointArret.builder()
                .id(1L)
                .trajet(trajet)
                .ville("Rabat")
                .ordre(1)
                .heurePassage(LocalDateTime.now().plusHours(1))
                .prixDepuisDepart(new BigDecimal("40.00"))
                .build();

        responseDTO = PointArretResponseDTO.builder()
                .id(1L)
                .trajetId(1L)
                .ville("Rabat")
                .ordre(1)
                .prixDepuisDepart(new BigDecimal("40.00"))
                .build();
    }

    @Test
    void savePointArret_Success() {
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(mapper.toEntity(requestDTO)).thenReturn(pointArret);
        when(pointArretRepository.save(any(PointArret.class))).thenReturn(pointArret);
        when(mapper.toResponseDTO(pointArret)).thenReturn(responseDTO);

        PointArretResponseDTO result = service.savePointArret(requestDTO);

        assertNotNull(result);
        assertEquals("Rabat", result.getVille());
        assertEquals(new BigDecimal("40.00"), result.getPrixDepuisDepart());
        verify(pointArretRepository, times(1)).save(any(PointArret.class));
    }

    @Test
    void savePointArret_TrajetNotFound_ThrowsResourceNotFoundException() {
        when(trajetRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.savePointArret(requestDTO));
        verify(pointArretRepository, never()).save(any(PointArret.class));
    }

    @Test
    void getPointsArretByTrajetId_Success() {
        when(trajetRepository.existsById(1L)).thenReturn(true);
        when(pointArretRepository.findByTrajetIdOrderByOrdreAsc(1L)).thenReturn(List.of(pointArret));
        when(mapper.toResponseDTO(pointArret)).thenReturn(responseDTO);

        List<PointArretResponseDTO> result = service.getPointsArretByTrajetId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getOrdre());
        verify(pointArretRepository, times(1)).findByTrajetIdOrderByOrdreAsc(1L);
    }
}