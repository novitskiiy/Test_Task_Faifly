package com.healthcare.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.dto.CreateVisitRequest;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.Patient;
import com.healthcare.entity.Visit;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PatientRepository;
import com.healthcare.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class VisitTrackingIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private VisitRepository visitRepository;

    private MockMvc mockMvc;
    private Doctor testDoctor;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Create test data
        testDoctor = new Doctor("Dr. Test", "Doctor", "America/New_York");
        testDoctor = doctorRepository.save(testDoctor);

        testPatient = new Patient("Test", "Patient");
        testPatient = patientRepository.save(testPatient);
    }

    @Test
    void createVisit_ValidRequest_ReturnsCreatedVisit() throws Exception {
        // Arrange
        CreateVisitRequest request = new CreateVisitRequest();
        request.setStart("2024-01-15T10:00:00");
        request.setEnd("2024-01-15T11:00:00");
        request.setPatientId(testPatient.getId());
        request.setDoctorId(testDoctor.getId());

        // Act & Assert
        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.patient.firstName").value("Test"))
                .andExpect(jsonPath("$.doctor.firstName").value("Dr. Test"));

        // Verify visit was saved
        assertEquals(1L, visitRepository.count());
    }

    @Test
    void createVisit_ConflictingTime_ReturnsBadRequest() throws Exception {
        // Arrange - Create first visit through API
        CreateVisitRequest firstRequest = new CreateVisitRequest();
        firstRequest.setStart("2024-01-15T10:00:00");
        firstRequest.setEnd("2024-01-15T11:00:00");
        firstRequest.setPatientId(testPatient.getId());
        firstRequest.setDoctorId(testDoctor.getId());

        // Create first visit
        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Create conflicting visit request
        CreateVisitRequest request = new CreateVisitRequest();
        request.setStart("2024-01-15T10:30:00"); // Overlaps with existing visit
        request.setEnd("2024-01-15T11:30:00");
        request.setPatientId(testPatient.getId());
        request.setDoctorId(testDoctor.getId());

        // Act & Assert
        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Doctor has conflicting visit at this time"));

        // Verify only one visit exists
        assertEquals(1L, visitRepository.count());
    }

    @Test
    void getPatientsList_NoVisits_ReturnsEmptyList() throws Exception {
        // Arrange - Clear all data to ensure no patients exist
        visitRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/api/visits/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void getPatientsList_WithVisits_ReturnsPatientWithVisits() throws Exception {
        // Arrange - Create visits
        Visit visit1 = new Visit(
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 11, 0),
                testPatient,
                testDoctor
        );
        visitRepository.save(visit1);

        // Act & Assert
        mockMvc.perform(get("/api/visits/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.data[0].firstName").value("Test"))
                .andExpect(jsonPath("$.data[0].lastName").value("Patient"))
                .andExpect(jsonPath("$.data[0].lastVisits").isArray())
                .andExpect(jsonPath("$.data[0].lastVisits.length()").value(1))
                .andExpect(jsonPath("$.data[0].lastVisits[0].doctor.firstName").value("Dr. Test"))
                .andExpect(jsonPath("$.data[0].lastVisits[0].doctor.totalPatients").value(1));
    }

    @Test
    void getPatientsList_WithSearch_ReturnsFilteredResults() throws Exception {
        // Arrange - Create another patient
        Patient anotherPatient = new Patient("Another", "Person");
        anotherPatient = patientRepository.save(anotherPatient);

        // Create visits for both patients
        Visit visit1 = new Visit(
                LocalDateTime.of(2024, 1, 15, 10, 0),
                LocalDateTime.of(2024, 1, 15, 11, 0),
                testPatient,
                testDoctor
        );
        visitRepository.save(visit1);

        Visit visit2 = new Visit(
                LocalDateTime.of(2024, 1, 16, 10, 0),
                LocalDateTime.of(2024, 1, 16, 11, 0),
                anotherPatient,
                testDoctor
        );
        visitRepository.save(visit2);

        // Act & Assert - Search for "Test"
        mockMvc.perform(get("/api/visits/patients")
                        .param("search", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].firstName").value("Test"))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void getPatientsList_WithPagination_ReturnsCorrectPage() throws Exception {
        // Arrange - Create multiple patients
        for (int i = 0; i < 25; i++) {
            Patient patient = new Patient("Patient" + i, "LastName" + i);
            patientRepository.save(patient);
        }

        // Act & Assert - First page with 10 items
        mockMvc.perform(get("/api/visits/patients")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(10))
                .andExpect(jsonPath("$.count").value(26)); // 25 new + 1 existing
    }

    @Test
    void createVisit_InvalidRequest_ReturnsValidationError() throws Exception {
        // Arrange
        CreateVisitRequest invalidRequest = new CreateVisitRequest();
        invalidRequest.setStart(""); // Invalid empty start time
        invalidRequest.setEnd("2024-01-15T11:00:00");
        invalidRequest.setPatientId(testPatient.getId());
        invalidRequest.setDoctorId(testDoctor.getId());

        // Act & Assert
        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.start").exists());
    }

    @Test
    void createVisit_NonExistentPatient_ReturnsBadRequest() throws Exception {
        // Arrange
        CreateVisitRequest request = new CreateVisitRequest();
        request.setStart("2024-01-15T10:00:00");
        request.setEnd("2024-01-15T11:00:00");
        request.setPatientId(999L); // Non-existent patient
        request.setDoctorId(testDoctor.getId());

        // Act & Assert
        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Patient not found with ID: 999"));
    }

    @Test
    void createVisit_NonExistentDoctor_ReturnsBadRequest() throws Exception {
        // Arrange
        CreateVisitRequest request = new CreateVisitRequest();
        request.setStart("2024-01-15T10:00:00");
        request.setEnd("2024-01-15T11:00:00");
        request.setPatientId(testPatient.getId());
        request.setDoctorId(999L); // Non-existent doctor

        // Act & Assert
        mockMvc.perform(post("/api/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Doctor not found with ID: 999"));
    }
}
