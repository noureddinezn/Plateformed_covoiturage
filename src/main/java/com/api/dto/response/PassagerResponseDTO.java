package com.api.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassagerResponseDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
}