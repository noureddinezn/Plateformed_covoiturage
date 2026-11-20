package com.api.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.Valid;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrajetRequestDTO {

    @NotNull(message = "L'ID du conducteur est obligatoire")
    private Long conducteurId;

    @NotNull(message = "L'ID du vehicule est obligatoire")
    private Long vehiculeId;

    private String villeDepart;

    private String villeArrivee;

    @FutureOrPresent(message = "La date de depart doit etre dans le present ou le futur")
    private LocalDateTime dateDepart;

    @NotNull(message = "Le nombre de places est obligatoire")
    @Min(value = 1, message = "Il faut proposer au moins 1 place")
    private Integer placesProposees;
    @Valid
    private List<TrajetPointArretRequestDTO> pointsArret;
}
