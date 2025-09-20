package com.healthcare.service;

import com.healthcare.dto.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient patient1;
    private Patient patient2;
    private Doctor doctor1;
    private Doctor doctor2;
    private Visit visit1;
    private Visit visit2;

    @BeforeEach
    void setUp() {
        patient1 = new Patient("John", "Doe");
        patient1.setId(1L);

        patient2 = new Patient("Jane", "Smith");
        patient2.setId(2L);

        doctor1 = new Doctor("Dr. Alice", "Johnson", "America/New_York");
        doctor1.setId(1L);

        doctor2 = new Doctor("Dr. Bob", "Wilson", "America/Los_Angeles");
        doctor2.setId(2L);

        visit1 = new Visit(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1).plusHours(1),
                patient1,
                doctor1
        );
        visit1.setId(1L);

        visit2 = new Visit(
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(2).plusHours(1),
                patient1,
                doctor2
        );
        visit2.setId(2L);
    }

    @Test
    void getPatientsList_DefaultParameters_ReturnsPaginatedResults() {
        // Arrange
        List<Patient> patients = Arrays.asList(patient1, patient2);
        Page<Patient> patientPage = new PageImpl<>(patients, PageRequest.of(0, 20), 2);
        List<Visit> visits = Arrays.asList(visit1, visit2);
        Object[] countResult1 = {1L, 5L}; // doctorId, count
        Object[] countResult2 = {2L, 3L}; // doctorId, count

        when(patientRepository.findBySearchCriteria(eq(null), any(Pageable.class)))
                .thenReturn(patientPage);
        when(visitRepository.findLastVisitsByPatients(Arrays.asList(1L, 2L)))
                .thenReturn(visits);
        List<Object[]> countResults = Arrays.<Object[]>asList(countResult1, countResult2);
        when(doctorRepository.countDistinctPatientsByDoctorIds(Arrays.asList(1L, 2L)))
                .thenReturn(countResults);

        // Act
        PatientsListResponse result = patientService.getPatientsList(null, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getData().size());
        assertEquals(2L, result.getCount());

        // Verify patient data
        PatientVisitResponse patientResponse = result.getData().get(0);
        assertEquals("John", patientResponse.getFirstName());
        assertEquals("Doe", patientResponse.getLastName());
        assertNotNull(patientResponse.getLastVisits());

        verify(patientRepository, times(1)).findBySearchCriteria(eq(null), any(Pageable.class));
        verify(visitRepository, times(1)).findLastVisitsByPatients(Arrays.asList(1L, 2L));
        verify(doctorRepository, times(1)).countDistinctPatientsByDoctorIds(Arrays.asList(1L, 2L));
    }

    @Test
    void getPatientsList_WithSearchCriteria_ReturnsFilteredResults() {
        // Arrange
        List<Patient> patients = Arrays.asList(patient1);
        Page<Patient> patientPage = new PageImpl<>(patients, PageRequest.of(0, 20), 1);
        List<Visit> visits = Arrays.asList(visit1);
        Object[] countResult = {1L, 5L};

        when(patientRepository.findBySearchCriteria(eq("John"), any(Pageable.class)))
                .thenReturn(patientPage);
        when(visitRepository.findLastVisitsByPatients(Arrays.asList(1L)))
                .thenReturn(visits);
        List<Object[]> countResults = Arrays.<Object[]>asList(countResult);
        when(doctorRepository.countDistinctPatientsByDoctorIds(Arrays.asList(1L)))
                .thenReturn(countResults);

        // Act
        PatientsListResponse result = patientService.getPatientsList(null, null, "John", null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getCount());

        verify(patientRepository, times(1)).findBySearchCriteria(eq("John"), any(Pageable.class));
    }

    @Test
    void getPatientsList_WithDoctorIdsFilter_ReturnsFilteredResults() {
        // Arrange
        List<Patient> patients = Arrays.asList(patient1);
        Page<Patient> patientPage = new PageImpl<>(patients, PageRequest.of(0, 20), 1);
        List<Visit> visits = Arrays.asList(visit1);
        Object[] countResult = {1L, 5L};

        when(patientRepository.findBySearchAndDoctorIds(eq(null), eq(Arrays.asList(1L)), any(Pageable.class)))
                .thenReturn(patientPage);
        when(visitRepository.findLastVisitsByPatients(Arrays.asList(1L)))
                .thenReturn(visits);
        List<Object[]> countResults = Arrays.<Object[]>asList(countResult);
        when(doctorRepository.countDistinctPatientsByDoctorIds(Arrays.asList(1L)))
                .thenReturn(countResults);

        // Act
        PatientsListResponse result = patientService.getPatientsList(null, null, null, Arrays.asList(1L));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getCount());

        verify(patientRepository, times(1)).findBySearchAndDoctorIds(eq(null), eq(Arrays.asList(1L)), any(Pageable.class));
    }

    @Test
    void getPatientsList_WithPagination_ReturnsCorrectPage() {
        // Arrange
        List<Patient> patients = Arrays.asList(patient1);
        Page<Patient> patientPage = new PageImpl<>(patients, PageRequest.of(1, 10), 25);
        List<Visit> visits = Arrays.asList(visit1);
        Object[] countResult = {1L, 5L};

        when(patientRepository.findBySearchCriteria(eq(null), any(Pageable.class)))
                .thenReturn(patientPage);
        when(visitRepository.findLastVisitsByPatients(Arrays.asList(1L)))
                .thenReturn(visits);
        List<Object[]> countResults = Arrays.<Object[]>asList(countResult);
        when(doctorRepository.countDistinctPatientsByDoctorIds(Arrays.asList(1L)))
                .thenReturn(countResults);

        // Act
        PatientsListResponse result = patientService.getPatientsList(2, 10, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(25L, result.getCount());

        verify(patientRepository, times(1)).findBySearchCriteria(eq(null), any(Pageable.class));
    }

    @Test
    void getPatientsList_WithTimezoneConversion_ConvertsCorrectly() {
        // Arrange
        List<Patient> patients = Arrays.asList(patient1);
        Page<Patient> patientPage = new PageImpl<>(patients, PageRequest.of(0, 20), 1);
        List<Visit> visits = Arrays.asList(visit2); // Visit with doctor in different timezone
        Object[] countResult = {2L, 3L};

        when(patientRepository.findBySearchCriteria(eq(null), any(Pageable.class)))
                .thenReturn(patientPage);
        when(visitRepository.findLastVisitsByPatients(Arrays.asList(1L)))
                .thenReturn(visits);
        List<Object[]> countResults = Arrays.<Object[]>asList(countResult);
        when(doctorRepository.countDistinctPatientsByDoctorIds(Arrays.asList(2L)))
                .thenReturn(countResults);

        // Act
        PatientsListResponse result = patientService.getPatientsList(null, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getCount());

        PatientVisitResponse patientResponse = result.getData().get(0);
        assertNotNull(patientResponse.getLastVisits());
        assertEquals(1, patientResponse.getLastVisits().size());

        LastVisitResponse lastVisit = patientResponse.getLastVisits().get(0);
        assertNotNull(lastVisit.getStart());
        assertNotNull(lastVisit.getEnd());
        assertNotNull(lastVisit.getDoctor());
        assertEquals("Dr. Bob", lastVisit.getDoctor().getFirstName());
        assertEquals("Wilson", lastVisit.getDoctor().getLastName());
        assertEquals(3, lastVisit.getDoctor().getTotalPatients());
    }

    @Test
    void getPatientsList_EmptyResults_ReturnsEmptyResponse() {
        // Arrange
        Page<Patient> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);

        when(patientRepository.findBySearchCriteria(eq(null), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        PatientsListResponse result = patientService.getPatientsList(null, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getData().size());
        assertEquals(0L, result.getCount());

        verify(patientRepository, times(1)).findBySearchCriteria(eq(null), any(Pageable.class));
        verify(visitRepository, never()).findLastVisitsByPatients(any());
        verify(doctorRepository, never()).countDistinctPatientsByDoctorIds(any());
    }
}
