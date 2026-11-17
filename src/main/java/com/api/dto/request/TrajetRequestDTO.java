package com.api.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

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

    @NotBlank(message = "La ville de depart est obligatoire")
    private String villeDepart;

    @NotBlank(message = "La ville d'arrivee est obligatoire")
    private String villeArrivee;

    @NotNull(message = "La date de depart est obligatoire")
    @FutureOrPresent(message = "La date de depart doit etre dans le present ou le futur")
    private LocalDateTime dateDepart;

    @NotNull(message = "Le nombre de places est obligatoire")
    @Min(value = 1, message = "Il faut proposer au moins 1 place")
    private Integer placesProposees;
}