package com.api.mapper;

import com.api.dto.request.VehiculeRequestDTO;
import com.api.dto.response.VehiculeResponseDTO;
import com.api.entity.Vehicule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehiculeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conducteur.id", source = "conducteurId")
    Vehicule toEntity(VehiculeRequestDTO requestDTO);

    @Mapping(target = "conducteurId", source = "conducteur.id")
    VehiculeResponseDTO toResponseDTO(Vehicule vehicule);
}