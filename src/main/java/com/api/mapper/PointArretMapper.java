package com.api.mapper;

import com.api.dto.request.PointArretRequestDTO;
import com.api.dto.response.PointArretResponseDTO;
import com.api.entity.PointArret;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PointArretMapper {

    @Mapping(target = "trajet.id", source = "trajetId")
    PointArret toEntity(PointArretRequestDTO requestDTO);

    @Mapping(target = "trajetId", source = "trajet.id")
    PointArretResponseDTO toResponseDTO(PointArret pointArret);
}