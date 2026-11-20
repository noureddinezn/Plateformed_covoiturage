package com.api.controller;

import com.api.exception.GlobalExceptionHandler;
import com.api.exception.BusinessException;
import com.api.exception.ResourceNotFoundException;
import com.api.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReservationControllerTest {
    private ReservationService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(ReservationService.class);
        mvc = MockMvcBuilders.standaloneSetup(new ReservationController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void invalidBodyReturns400WithoutCallingService() throws Exception {
        mvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/reservations"));
        verifyNoInteractions(service);
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void invalidIdReturns400() throws Exception {
        mvc.perform(get("/api/reservations/abc")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void missingReservationReturns404() throws Exception {
        when(service.getReservationById(99L)).thenThrow(new ResourceNotFoundException("Introuvable"));
        mvc.perform(get("/api/reservations/99")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Introuvable"));
    }

    @Test
    void alreadyCancelledReturns409() throws Exception {
        when(service.annulerReservation(1L)).thenThrow(new BusinessException("Deja annulee"));
        mvc.perform(patch("/api/reservations/1/annuler")).andExpect(status().isConflict());
    }

    @Test
    void databaseIntegrityViolationReturns409() throws Exception {
        when(service.getReservationById(1L)).thenThrow(
                new org.springframework.dao.DataIntegrityViolationException("duplicate"));
        mvc.perform(get("/api/reservations/1")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/reservations/1"));
    }

    @Test
    void optimisticLockConflictReturns409() throws Exception {
        when(service.getReservationById(1L)).thenThrow(
                new org.springframework.orm.ObjectOptimisticLockingFailureException("Trajet", 1L));
        mvc.perform(get("/api/reservations/1")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }


    @Test
    void annulerReservation_Returns204WithoutBody() throws Exception {
        mvc.perform(patch("/api/reservations/1/annuler"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(service).annulerReservation(1L);
    }

    @Test
    void invalidStopOrderReturns400() throws Exception {
        when(service.saveReservation(any())).thenThrow(
                new com.api.exception.BadRequestException("Ordre invalide"));
        mvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passagerId\":1,\"trajetId\":1,\"pointMonteeId\":2,\"pointDescenteId\":1,\"nombrePlaces\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Ordre invalide"));
    }

}
