package com.api.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointArretResponseDTO {
    private Long id;
    private Long trajetId;
    private String ville;
    private Integer ordre;
    private LocalDateTime heurePassage;
    private BigDecimal prixDepuisDepart;
}
