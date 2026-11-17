package com.api.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculeResponseDTO {
    private Long id;
    private String marque;
    private String modele;
    private String immatriculation;
    private String couleur;
    private Integer capacite;
    private Long conducteurId;
}