package com.api.mapper;

import com.api.dto.response.TrajetResponseDTO;
import com.api.entity.Reservation;
import com.api.entity.Trajet;
import com.api.enums.StatutReservation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrajetMapperTest {
    private final TrajetMapper mapper = Mappers.getMapper(TrajetMapper.class);

    @ParameterizedTest
    @ValueSource(ints = {0, 2, 4})
    void responseContainsDynamicRemainingSeats(int confirmedSeats) {
        Trajet trajet = Trajet.builder().placesProposees(4).build();
        trajet.getReservations().add(Reservation.builder().nombrePlaces(confirmedSeats)
                .statut(StatutReservation.CONFIRMEE).build());
        trajet.getReservations().add(Reservation.builder().nombrePlaces(3)
                .statut(StatutReservation.ANNULEE).build());

        TrajetResponseDTO response = mapper.toResponseDTO(trajet);

        assertEquals(4 - confirmedSeats, response.getPlacesRestantes());
        assertEquals(4 - confirmedSeats,
                new ObjectMapper().valueToTree(response).get("placesRestantes").asInt());
    }
}
