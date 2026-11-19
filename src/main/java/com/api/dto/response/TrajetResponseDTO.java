package com.api.dto.response;

import com.api.enums.StatutTrajet;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrajetResponseDTO {
    private Long id;
    private Long conducteurId;
    private Long vehiculeId;
    private String villeDepart;
    private String villeArrivee;
    private LocalDateTime dateDepart;
    private Integer placesProposees;
    private Integer placesRestantes;
    private StatutTrajet statut;
}