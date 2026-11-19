package com.api.mapper;

import com.api.dto.request.ConducteurRequestDTO;
import com.api.dto.response.ConducteurResponseDTO;
import com.api.entity.Conducteur;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConducteurMapper {

    Conducteur toEntity(ConducteurRequestDTO requestDTO);

    ConducteurResponseDTO toResponseDTO(Conducteur conducteur);
}