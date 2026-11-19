package com.api.service;

import com.api.dto.request.ConducteurRequestDTO;
import com.api.dto.response.ConducteurResponseDTO;
import com.api.entity.Conducteur;
import com.api.exception.BusinessException;
import com.api.mapper.ConducteurMapper;
import com.api.repository.ConducteurRepository;
import com.api.service.impl.ConducteurServiceImpl;
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
class ConducteurServiceImplTest {

    @Mock
    private ConducteurRepository repository;

    @Mock
    private ConducteurMapper mapper;

    @InjectMocks
    private ConducteurServiceImpl service;

    private ConducteurRequestDTO requestDTO;
    private Conducteur conducteur;
    private ConducteurResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = ConducteurRequestDTO.builder()
                .nom("Alami")
                .prenom("Ahmed")
                .email("ahmed@email.com")
                .telephone("0600112233")
                .numeroPermis("AB12345")
                .build();

        conducteur = Conducteur.builder()
                .id(1L)
                .nom("Alami")
                .prenom("Ahmed")
                .email("ahmed@email.com")
                .telephone("0600112233")
                .numeroPermis("AB12345")
                .build();

        responseDTO = ConducteurResponseDTO.builder()
                .id(1L)
                .nom("Alami")
                .prenom("Ahmed")
                .email("ahmed@email.com")
                .telephone("0600112233")
                .numeroPermis("AB12345")
                .build();
    }

    @Test
    void saveConducteur_Success() {
        when(repository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(repository.existsByNumeroPermis(requestDTO.getNumeroPermis())).thenReturn(false);
        when(mapper.toEntity(requestDTO)).thenReturn(conducteur);
        when(repository.save(conducteur)).thenReturn(conducteur);
        when(mapper.toResponseDTO(conducteur)).thenReturn(responseDTO);

        ConducteurResponseDTO result = service.saveConducteur(requestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ahmed@email.com", result.getEmail());
        verify(repository, times(1)).save(any(Conducteur.class));
    }

    @Test
    void saveConducteur_EmailExists_ThrowsBusinessException() {
        when(repository.existsByEmail(requestDTO.getEmail())).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.saveConducteur(requestDTO));

        verify(repository, never()).save(any(Conducteur.class));
    }

    @Test
    void getConducteurById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(mapper.toResponseDTO(conducteur)).thenReturn(responseDTO);

        ConducteurResponseDTO result = service.getConducteurById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void saveConducteur_DuplicateTelephoneIsRejected() {
        when(repository.existsByTelephone(requestDTO.getTelephone())).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.saveConducteur(requestDTO));
        verify(repository, never()).save(any());
    }

    @Test
    void updateConducteur_DuplicateTelephoneIsRejected() {
        when(repository.findById(1L)).thenReturn(Optional.of(conducteur));
        requestDTO.setTelephone("0699999999");
        when(repository.existsByTelephone(requestDTO.getTelephone())).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.updateConducteur(1L, requestDTO));
        verify(repository, never()).save(any());
        assertEquals("0600112233", conducteur.getTelephone());
    }

    @Test
    void updateConducteur_UnchangedTelephoneIsAccepted() {
        when(repository.findById(1L)).thenReturn(Optional.of(conducteur));
        when(repository.save(conducteur)).thenReturn(conducteur);
        when(mapper.toResponseDTO(conducteur)).thenReturn(responseDTO);
        assertSame(responseDTO, service.updateConducteur(1L, requestDTO));
        verify(repository, never()).existsByTelephone(any());
    }

}
