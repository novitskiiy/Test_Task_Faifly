package com.healthcare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.dto.CreateVisitRequest;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.Patient;
import com.healthcare.entity.Visit;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PatientRepository;
import com.healthcare.repository.VisitRepository;
import com.healthcare.service.PatientService;
import com.healthcare.service.VisitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VisitController.class)
class VisitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VisitService visitService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private VisitRepository visitRepository;

    @MockBean
    private PatientRepository patientRepository;

    @MockBean
    private DoctorRepository doctorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Doctor testDoctor;
    private Patient testPatient;
    private Visit testVisit;
    private CreateVisitRequest validRequest;

    @BeforeEach
    void setUp() {
        testDoctor = new Doctor("John", "Smith", "America/New_York");
        testDoctor.setId(1L);

        testPatient = new Patient("Jane", "Doe");
        testPatient.setId(1L);

        testVisit = new Visit(
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                testPatient,
                testDoctor
        );
        testVisit.setId(1L);

        validRequest = new CreateVisitRequest();
        validRequest.setStart("2024-01-15T10:00:00");
        validRequest.setEnd("2024-01-15T11:00:00");
        validRequest.setPatientId(1L);
        validRequest.setDoctorId(1L);
    }

    @Test
    void createVisit_ValidRequest_ReturnsCreatedVisit() throws Exception {
        when(visitService.createVisit(any(CreateVisitRequest.class))).thenReturn(testVisit);

        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patient.firstName").value("Jane"))
                .andExpect(jsonPath("$.doctor.firstName").value("John"));

        verify(visitService, times(1)).createVisit(any(CreateVisitRequest.class));
    }

    @Test
    void createVisit_InvalidRequest_ReturnsBadRequest() throws Exception {
        CreateVisitRequest invalidRequest = new CreateVisitRequest();
        invalidRequest.setStart(""); // Invalid empty start time
        invalidRequest.setEnd("2024-01-15T11:00:00");
        invalidRequest.setPatientId(1L);
        invalidRequest.setDoctorId(1L);

        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));

        verify(visitService, never()).createVisit(any(CreateVisitRequest.class));
    }

    @Test
    void createVisit_MissingFields_ReturnsBadRequest() throws Exception {
        CreateVisitRequest incompleteRequest = new CreateVisitRequest();
        // Missing required fields

        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incompleteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));

        verify(visitService, never()).createVisit(any(CreateVisitRequest.class));
    }

    @Test
    void createVisit_ServiceThrowsException_ReturnsBadRequest() throws Exception {
        when(visitService.createVisit(any(CreateVisitRequest.class)))
                .thenThrow(new IllegalArgumentException("Doctor has conflicting visit at this time"));

        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Doctor has conflicting visit at this time"));

        verify(visitService, times(1)).createVisit(any(CreateVisitRequest.class));
    }
}
