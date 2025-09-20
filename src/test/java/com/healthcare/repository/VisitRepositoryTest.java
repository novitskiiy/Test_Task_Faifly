package com.healthcare.repository;

import com.healthcare.entity.Doctor;
import com.healthcare.entity.Patient;
import com.healthcare.entity.Visit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class VisitRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VisitRepository visitRepository;

    private Doctor doctor1;
    private Doctor doctor2;
    private Patient patient1;
    private Patient patient2;
    private Visit visit1;
    private Visit visit2;
    private Visit visit3;

    @BeforeEach
    void setUp() {
        doctor1 = new Doctor("John", "Smith", "America/New_York");
        doctor2 = new Doctor("Jane", "Doe", "America/Los_Angeles");
        patient1 = new Patient("Alice", "Johnson");
        patient2 = new Patient("Bob", "Wilson");

        entityManager.persistAndFlush(doctor1);
        entityManager.persistAndFlush(doctor2);
        entityManager.persistAndFlush(patient1);
        entityManager.persistAndFlush(patient2);

        visit1 = new Visit(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1).plusHours(1),
                patient1,
                doctor1
        );

        visit2 = new Visit(
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(2).plusHours(1),
                patient1,
                doctor2
        );

        visit3 = new Visit(
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(3).plusHours(1),
                patient2,
                doctor1
        );

        entityManager.persistAndFlush(visit1);
        entityManager.persistAndFlush(visit2);
        entityManager.persistAndFlush(visit3);
    }

    @Test
    void findLastVisitsByPatient_ReturnsAllVisitsForPatient() {
        // Act
        List<Visit> visits = visitRepository.findLastVisitsByPatient(patient1.getId());

        // Assert
        assertEquals(2, visits.size());
        assertTrue(visits.stream().allMatch(v -> v.getPatient().getId().equals(patient1.getId())));
    }

    @Test
    void findLastVisitsByPatients_ReturnsVisitsForMultiplePatients() {
        // Act
        List<Visit> visits = visitRepository.findLastVisitsByPatients(
                List.of(patient1.getId(), patient2.getId())
        );

        // Assert
        assertEquals(3, visits.size());
        assertTrue(visits.stream().anyMatch(v -> v.getPatient().getId().equals(patient1.getId())));
        assertTrue(visits.stream().anyMatch(v -> v.getPatient().getId().equals(patient2.getId())));
    }

    @Test
    void findLastVisitByPatientAndDoctor_ReturnsSpecificVisit() {
        // Act
        List<Visit> visits = visitRepository.findLastVisitByPatientAndDoctor(
                patient1.getId(), doctor1.getId()
        );

        // Assert
        assertEquals(1, visits.size());
        assertEquals(patient1.getId(), visits.get(0).getPatient().getId());
        assertEquals(doctor1.getId(), visits.get(0).getDoctor().getId());
    }

    @Test
    void existsByDoctorIdAndDateTimeConflict_NoConflict_ReturnsFalse() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(1);

        // Act
        boolean hasConflict = visitRepository.existsByDoctorIdAndDateTimeConflict(
                doctor1.getId(), startTime, endTime
        );

        // Assert
        assertFalse(hasConflict);
    }

    @Test
    void existsByDoctorIdAndDateTimeConflict_ExactConflict_ReturnsTrue() {
        // Arrange - Using existing visit times
        LocalDateTime startTime = visit1.getStartDateTime();
        LocalDateTime endTime = visit1.getEndDateTime();

        // Act
        boolean hasConflict = visitRepository.existsByDoctorIdAndDateTimeConflict(
                doctor1.getId(), startTime, endTime
        );

        // Assert
        assertTrue(hasConflict);
    }

    @Test
    void existsByDoctorIdAndDateTimeConflict_OverlappingConflict_ReturnsTrue() {
        // Arrange - Overlapping time range
        LocalDateTime startTime = visit1.getStartDateTime().minusMinutes(30);
        LocalDateTime endTime = visit1.getStartDateTime().plusMinutes(30);

        // Act
        boolean hasConflict = visitRepository.existsByDoctorIdAndDateTimeConflict(
                doctor1.getId(), startTime, endTime
        );

        // Assert
        assertTrue(hasConflict);
    }

    @Test
    void existsByDoctorIdAndDateTimeConflict_ContainsExisting_ReturnsTrue() {
        // Arrange - New visit contains existing visit
        LocalDateTime startTime = visit1.getStartDateTime().minusMinutes(30);
        LocalDateTime endTime = visit1.getEndDateTime().plusMinutes(30);

        // Act
        boolean hasConflict = visitRepository.existsByDoctorIdAndDateTimeConflict(
                doctor1.getId(), startTime, endTime
        );

        // Assert
        assertTrue(hasConflict);
    }

    @Test
    void existsByDoctorIdAndDateTimeConflict_ContainedByExisting_ReturnsTrue() {
        // Arrange - New visit is contained by existing visit
        LocalDateTime startTime = visit1.getStartDateTime().plusMinutes(15);
        LocalDateTime endTime = visit1.getEndDateTime().minusMinutes(15);

        // Act
        boolean hasConflict = visitRepository.existsByDoctorIdAndDateTimeConflict(
                doctor1.getId(), startTime, endTime
        );

        // Assert
        assertTrue(hasConflict);
    }

    @Test
    void findLastVisitsByPatient_ReturnsVisitsInDescendingOrder() {
        // Act
        List<Visit> visits = visitRepository.findLastVisitsByPatient(patient1.getId());

        // Assert
        assertEquals(2, visits.size());
        // Should be ordered by startDateTime DESC (newest first)
        assertTrue(visits.get(0).getStartDateTime().isAfter(visits.get(1).getStartDateTime()));
    }

    @Test
    void findLastVisitsByPatients_WithFetchJoins_LoadsAssociatedEntities() {
        // Act
        List<Visit> visits = visitRepository.findLastVisitsByPatients(
                List.of(patient1.getId(), patient2.getId())
        );

        // Assert
        assertNotNull(visits);
        assertFalse(visits.isEmpty());
        
        // Verify that associated entities are loaded (no lazy loading exceptions)
        for (Visit visit : visits) {
            assertNotNull(visit.getPatient().getFirstName());
            assertNotNull(visit.getDoctor().getFirstName());
        }
    }
}
