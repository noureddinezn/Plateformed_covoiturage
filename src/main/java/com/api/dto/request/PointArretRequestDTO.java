package com.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointArretRequestDTO {

    @NotNull(message = "L'ID du trajet est obligatoire")
    private Long trajetId;

    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    @NotNull(message = "L'ordre de l'arret est obligatoire")
    @Min(value = 1, message = "L'ordre doit etre superieur ou egal a 1")
    private Integer ordre;

    @NotNull(message = "L'heure de passage est obligatoire")
    private LocalDateTime heurePassage;

    @NotNull(message = "Le prix depuis le depart est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le prix ne peut pas etre negatif")
    private BigDecimal prixDepuisDepart;
}