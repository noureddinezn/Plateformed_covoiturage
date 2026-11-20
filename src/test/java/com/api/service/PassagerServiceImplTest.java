package com.api.service;

import com.api.dto.request.PassagerRequestDTO;
import com.api.dto.response.PassagerResponseDTO;
import com.api.entity.Passager;
import com.api.exception.BusinessException;
import com.api.exception.ResourceNotFoundException;
import com.api.mapper.PassagerMapper;
import com.api.repository.PassagerRepository;
import com.api.service.impl.PassagerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PassagerServiceImplTest {
    @Mock private PassagerRepository repository;
    @Mock private PassagerMapper mapper;
    @InjectMocks private PassagerServiceImpl service;

    @Test
    void createPassengerReturnsPersistedResponse() {
        PassagerRequestDTO request = PassagerRequestDTO.builder().email("sara@example.com").build();
        Passager entity = Passager.builder().email(request.getEmail()).build();
        Passager persisted = Passager.builder().id(7L).email(request.getEmail()).build();
        PassagerResponseDTO response = PassagerResponseDTO.builder().id(7L).build();
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(persisted);
        when(mapper.toResponseDTO(persisted)).thenReturn(response);
        assertSame(response, service.savePassager(request));
        verify(repository).existsByEmail(request.getEmail());
        verify(repository).save(entity);
    }

    @Test
    void duplicateEmailPreventsPassengerCreation() {
        PassagerRequestDTO request = PassagerRequestDTO.builder().email("used@example.com").build();
        when(repository.existsByEmail(request.getEmail())).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.savePassager(request));
        verify(repository, never()).save(any());
        verifyNoInteractions(mapper);
    }

    @Test
    void missingPassengerLookupDoesNotMap() {
        assertThrows(ResourceNotFoundException.class, () -> service.getPassagerById(99L));
        verifyNoInteractions(mapper);
    }

    @Test
    void duplicateNewEmailLeavesPassengerUnchanged() {
        Passager entity = Passager.builder().id(1L).email("old@example.com").nom("Old").build();
        PassagerRequestDTO request = PassagerRequestDTO.builder().email("used@example.com").nom("New").build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.existsByEmail(request.getEmail())).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.updatePassager(1L, request));
        assertEquals("old@example.com", entity.getEmail());
        assertEquals("Old", entity.getNom());
        verify(repository, never()).save(any());
    }

    @Test
    void unchangedEmailAllowsProfileUpdate() {
        Passager entity = Passager.builder().id(1L).email("sara@example.com").build();
        PassagerRequestDTO request = PassagerRequestDTO.builder().email(entity.getEmail())
                .nom("Alami").prenom("Sara").telephone("0612345678").build();
        PassagerResponseDTO response = PassagerResponseDTO.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponseDTO(entity)).thenReturn(response);
        assertSame(response, service.updatePassager(1L, request));
        assertAll(
                () -> assertEquals("Alami", entity.getNom()),
                () -> assertEquals("Sara", entity.getPrenom()),
                () -> assertEquals("0612345678", entity.getTelephone()),
                () -> assertEquals("sara@example.com", entity.getEmail()));
        verify(repository, never()).existsByEmail(any());
    }

    @Test
    void passengerListPreservesMappedOrder() {
        Passager first = Passager.builder().id(7L).build();
        Passager second = Passager.builder().id(3L).build();
        PassagerResponseDTO firstResponse = PassagerResponseDTO.builder().id(7L).build();
        PassagerResponseDTO secondResponse = PassagerResponseDTO.builder().id(3L).build();
        when(repository.findAll()).thenReturn(List.of(first, second));
        when(mapper.toResponseDTO(first)).thenReturn(firstResponse);
        when(mapper.toResponseDTO(second)).thenReturn(secondResponse);
        assertEquals(List.of(firstResponse, secondResponse), service.getAllPassagers());
    }

    @Test
    void missingPassengerCannotBeDeleted() {
        assertThrows(ResourceNotFoundException.class, () -> service.deletePassager(99L));
        verify(repository, never()).deleteById(any());
    }
}
