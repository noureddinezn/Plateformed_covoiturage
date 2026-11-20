package com.api.mapper;

import com.api.dto.ReservationRequestDTO;
import com.api.dto.response.ReservationResponseDTO;
import com.api.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "passager.id", source = "passagerId")
    @Mapping(target = "trajet.id", source = "trajetId")
    @Mapping(target = "pointMontee.id", source = "pointMonteeId")
    @Mapping(target = "pointDescente.id", source = "pointDescenteId")
    Reservation toEntity(ReservationRequestDTO requestDTO);

    @Mapping(target = "passagerId", source = "passager.id")
    @Mapping(target = "trajetId", source = "trajet.id")
    @Mapping(target = "pointMonteeId", source = "pointMontee.id")
    @Mapping(target = "pointDescenteId", source = "pointDescente.id")
    ReservationResponseDTO toResponseDTO(Reservation reservation);
}