package com.api.controller;

import com.api.exception.BadRequestException;
import com.api.exception.ForbiddenException;
import com.api.exception.GlobalExceptionHandler;
import com.api.service.TrajetService;
import com.api.dto.response.TrajetResponseDTO;
import com.api.enums.StatutTrajet;
import com.api.exception.BusinessException;
import com.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrajetControllerTest {
    private TrajetService service;
    private MockMvc mvc;
    private final String body = "{\"conducteurId\":1,\"vehiculeId\":1,\"villeDepart\":\"Rabat\","
            + "\"villeArrivee\":\"Fes\",\"dateDepart\":\"2099-01-01T10:00:00\",\"placesProposees\":4}";

    @BeforeEach
    void setUp() {
        service = mock(TrajetService.class);
        mvc = MockMvcBuilders.standaloneSetup(new TrajetController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void wrongOwnerReturns403() throws Exception {
        when(service.saveTrajet(any())).thenThrow(new ForbiddenException("Vehicule non autorise"));
        mvc.perform(post("/api/trajets").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void excessCapacityReturns400() throws Exception {
        when(service.saveTrajet(any())).thenThrow(new BadRequestException("Capacite depassee"));
        mvc.perform(post("/api/trajets").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void terminerTrajet_Returns200WithUpdatedStatus() throws Exception {
        when(service.terminerTrajet(1L)).thenReturn(TrajetResponseDTO.builder()
                .id(1L).statut(StatutTrajet.TERMINE).build());
        mvc.perform(patch("/api/trajets/1/terminer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TERMINE"));
        verify(service).terminerTrajet(1L);
    }

    @Test
    void terminerTrajet_BeforeDepartureReturns409() throws Exception {
        when(service.terminerTrajet(1L)).thenThrow(new BusinessException("Depart futur"));
        mvc.perform(patch("/api/trajets/1/terminer"))
                .andExpect(status().isConflict());
    }

    @Test
    void annulerTrajet_Returns204WithoutBody() throws Exception {
        mvc.perform(patch("/api/trajets/1/annuler"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(service).annulerTrajet(1L);
    }

    @Test
    void missingTrip_Returns404ForBothOperations() throws Exception {
        when(service.terminerTrajet(99L)).thenThrow(new ResourceNotFoundException("Introuvable"));
        doThrow(new ResourceNotFoundException("Introuvable")).when(service).annulerTrajet(99L);
        mvc.perform(patch("/api/trajets/99/terminer")).andExpect(status().isNotFound());
        mvc.perform(patch("/api/trajets/99/annuler")).andExpect(status().isNotFound());
    }

    @Test
    void rechercheReverseDirectionReturns200AndEmptyArray() throws Exception {
        when(service.rechercherTrajets("Settat", "Casablanca")).thenReturn(java.util.List.of());
        mvc.perform(get("/api/trajets/recherche")
                        .param("villeDepart", "Settat").param("villeArrivee", "Casablanca"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(service).rechercherTrajets("Settat", "Casablanca");
    }

    @Test
    void rechercheReturnsMatchingTrips() throws Exception {
        when(service.rechercherTrajets("Casablanca", "Settat")).thenReturn(java.util.List.of(
                TrajetResponseDTO.builder().id(1L).statut(StatutTrajet.PUBLIE).build()));
        mvc.perform(get("/api/trajets/recherche")
                        .param("villeDepart", "Casablanca").param("villeArrivee", "Settat"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void publicationAcceptsStopsWithoutLegacyDetailsOrTripId() throws Exception {
        when(service.saveTrajet(any())).thenReturn(TrajetResponseDTO.builder().id(1L).build());
        String publication = "{\"conducteurId\":1,\"vehiculeId\":1,\"placesProposees\":4,\"pointsArret\":["
                + "{\"ville\":\"Casablanca\",\"ordre\":0,\"heurePassage\":\"2099-01-01T10:00:00\",\"prixDepuisDepart\":0},"
                + "{\"ville\":\"Settat\",\"ordre\":1,\"heurePassage\":\"2099-01-01T11:00:00\",\"prixDepuisDepart\":50}]}";
        mvc.perform(post("/api/trajets").contentType(MediaType.APPLICATION_JSON).content(publication))
                .andExpect(status().isCreated());
        verify(service).saveTrajet(any());
    }

    @Test
    void publicationRejectsInvalidNestedStop() throws Exception {
        String publication = "{\"conducteurId\":1,\"vehiculeId\":1,\"placesProposees\":4,\"pointsArret\":["
                + "{\"ville\":\"\",\"ordre\":0,\"heurePassage\":\"2099-01-01T10:00:00\",\"prixDepuisDepart\":0}]}";
        mvc.perform(post("/api/trajets").contentType(MediaType.APPLICATION_JSON).content(publication))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
