package com.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRequestDTO {

    @NotNull(message = "L'ID du passager est obligatoire")
    private Long passagerId;

    @NotNull(message = "L'ID du trajet est obligatoire")
    private Long trajetId;

    @NotNull(message = "L'ID du point de montee est obligatoire")
    private Long pointMonteeId;

    @NotNull(message = "L'ID du point de descente est obligatoire")
    private Long pointDescenteId;

    @NotNull(message = "Le nombre de places est obligatoire")
    @Min(value = 1, message = "Il faut reserver au moins 1 place")
    private Integer nombrePlaces;
}