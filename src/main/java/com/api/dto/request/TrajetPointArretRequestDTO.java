package com.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrajetPointArretRequestDTO {
    @NotBlank
    private String ville;
    @NotNull
    @Min(0)
    private Integer ordre;
    @NotNull
    private LocalDateTime heurePassage;
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal prixDepuisDepart;
}
