package com.api.mapper;

import com.api.dto.TrajetRequestDTO;
import com.api.dto.TrajetResponseDTO;
import com.api.entity.Trajet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrajetMapper {

    @Mapping(target = "conducteur.id", source = "conducteurId")
    @Mapping(target = "vehicule.id", source = "vehiculeId")
    Trajet toEntity(TrajetRequestDTO requestDTO);

    @Mapping(target = "conducteurId", source = "conducteur.id")
    @Mapping(target = "vehiculeId", source = "vehicule.id")
    TrajetResponseDTO toResponseDTO(Trajet trajet);
}