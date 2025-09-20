package com.healthcare.service;

import com.healthcare.dto.CreateVisitRequest;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.Patient;
import com.healthcare.entity.Visit;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PatientRepository;
import com.healthcare.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private VisitService visitService;

    private Doctor testDoctor;
    private Patient testPatient;
    private CreateVisitRequest validRequest;

    @BeforeEach
    void setUp() {
        testDoctor = new Doctor("John", "Smith", "America/New_York");
        testDoctor.setId(1L);

        testPatient = new Patient("Jane", "Doe");
        testPatient.setId(1L);

        validRequest = new CreateVisitRequest();
        validRequest.setStart("2024-01-15T10:00:00");
        validRequest.setEnd("2024-01-15T11:00:00");
        validRequest.setPatientId(1L);
        validRequest.setDoctorId(1L);
    }

    @Test
    void createVisit_ValidRequest_ReturnsVisit() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(visitRepository.existsByDoctorIdAndDateTimeConflict(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> {
            Visit visit = invocation.getArgument(0);
            visit.setId(1L);
            return visit;
        });

        // Act
        Visit result = visitService.createVisit(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(testPatient, result.getPatient());
        assertEquals(testDoctor, result.getDoctor());

        verify(patientRepository, times(1)).findById(1L);
        verify(doctorRepository, times(1)).findById(1L);
        verify(visitRepository, times(1)).existsByDoctorIdAndDateTimeConflict(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(visitRepository, times(1)).save(any(Visit.class));
    }

    @Test
    void createVisit_PatientNotFound_ThrowsException() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> visitService.createVisit(validRequest));

        assertEquals("Patient not found with ID: 1", exception.getMessage());
        verify(patientRepository, times(1)).findById(1L);
        verify(doctorRepository, never()).findById(any());
        verify(visitRepository, never()).save(any(Visit.class));
    }

    @Test
    void createVisit_DoctorNotFound_ThrowsException() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> visitService.createVisit(validRequest));

        assertEquals("Doctor not found with ID: 1", exception.getMessage());
        verify(patientRepository, times(1)).findById(1L);
        verify(doctorRepository, times(1)).findById(1L);
        verify(visitRepository, never()).save(any(Visit.class));
    }

    @Test
    void createVisit_ConflictingVisit_ThrowsException() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(visitRepository.existsByDoctorIdAndDateTimeConflict(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> visitService.createVisit(validRequest));

        assertEquals("Doctor has conflicting visit at this time", exception.getMessage());
        verify(patientRepository, times(1)).findById(1L);
        verify(doctorRepository, times(1)).findById(1L);
        verify(visitRepository, times(1)).existsByDoctorIdAndDateTimeConflict(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(visitRepository, never()).save(any(Visit.class));
    }

    @Test
    void createVisit_InvalidTimeRange_ThrowsException() {
        // Arrange
        CreateVisitRequest invalidRequest = new CreateVisitRequest();
        invalidRequest.setStart("2024-01-15T11:00:00"); // Start after end
        invalidRequest.setEnd("2024-01-15T10:00:00");
        invalidRequest.setPatientId(1L);
        invalidRequest.setDoctorId(1L);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> visitService.createVisit(invalidRequest));

        assertEquals("Start time cannot be after end time", exception.getMessage());
        verify(visitRepository, never()).save(any(Visit.class));
    }

    @Test
    void createVisit_DifferentTimezone_ConvertsCorrectly() {
        // Arrange
        Doctor doctorInDifferentTimezone = new Doctor("Emily", "Johnson", "America/Los_Angeles");
        doctorInDifferentTimezone.setId(2L);

        CreateVisitRequest requestInDifferentTimezone = new CreateVisitRequest();
        requestInDifferentTimezone.setStart("2024-01-15T10:00:00");
        requestInDifferentTimezone.setEnd("2024-01-15T11:00:00");
        requestInDifferentTimezone.setPatientId(1L);
        requestInDifferentTimezone.setDoctorId(2L);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctorInDifferentTimezone));
        when(visitRepository.existsByDoctorIdAndDateTimeConflict(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> {
            Visit visit = invocation.getArgument(0);
            visit.setId(1L);
            return visit;
        });

        // Act
        Visit result = visitService.createVisit(requestInDifferentTimezone);

        // Assert
        assertNotNull(result);
        verify(visitRepository, times(1)).save(any(Visit.class));
    }
}
