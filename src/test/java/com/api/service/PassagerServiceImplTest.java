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
}
