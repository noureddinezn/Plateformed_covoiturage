package com.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculeRequestDTO {

    @NotBlank(message = "La marque est obligatoire")
    private String marque;

    @NotBlank(message = "Le modele est obligatoire")
    private String modele;

    @NotBlank(message = "L'immatriculation est obligatoire")
    private String immatriculation;

    @NotBlank(message = "La couleur est obligatoire")
    private String couleur;

    @NotNull(message = "La capacite est obligatoire")
    @Min(value = 1, message = "La capacite doit etre d'au moins 1")
    private Integer capacite;

    @NotNull(message = "L'ID du conducteur est obligatoire")
    private Long conducteurId;
}