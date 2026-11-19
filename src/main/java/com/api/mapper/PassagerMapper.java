package com.api.mapper;

import com.api.dto.request.PassagerRequestDTO;
import com.api.dto.response.PassagerResponseDTO;
import com.api.entity.Passager;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PassagerMapper {
    Passager toEntity(PassagerRequestDTO requestDTO);
    PassagerResponseDTO toResponseDTO(Passager passager);
}