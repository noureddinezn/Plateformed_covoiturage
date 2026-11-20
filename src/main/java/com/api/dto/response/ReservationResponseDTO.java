package com.api.dto.response;

import com.api.enums.StatutReservation;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponseDTO {
    private Long id;
    private Long passagerId;
    private Long trajetId;
    private Long pointMonteeId;
    private Long pointDescenteId;
    private Integer nombrePlaces;
    private BigDecimal montantTotal;
    private LocalDateTime dateReservation;
    private StatutReservation statut;
}