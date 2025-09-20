package com.healthcare.service;

import com.healthcare.dto.CreateVisitRequest;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.Patient;
import com.healthcare.entity.Visit;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PatientRepository;
import com.healthcare.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@Transactional
public class VisitService {

    private final VisitRepository visitRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Autowired
    public VisitService(VisitRepository visitRepository,
                        PatientRepository patientRepository,
                        DoctorRepository doctorRepository) {
        this.visitRepository = visitRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public Visit createVisit(CreateVisitRequest request) {
        // Validate patient exists
        Optional<Patient> patientOpt = patientRepository.findById(request.getPatientId());
        if (patientOpt.isEmpty()) {
            throw new IllegalArgumentException("Patient not found with ID: " + request.getPatientId());
        }

        // Validate doctor exists
        Optional<Doctor> doctorOpt = doctorRepository.findById(request.getDoctorId());
        if (doctorOpt.isEmpty()) {
            throw new IllegalArgumentException("Doctor not found with ID: " + request.getDoctorId());
        }

        Patient patient = patientOpt.get();
        Doctor doctor = doctorOpt.get();

        // Parse datetime strings in doctor's timezone
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        ZoneId doctorTimezone = ZoneId.of(doctor.getTimezone());

        LocalDateTime startDateTime = LocalDateTime.parse(request.getStart(), formatter)
                .atZone(doctorTimezone)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();

        LocalDateTime endDateTime = LocalDateTime.parse(request.getEnd(), formatter)
                .atZone(doctorTimezone)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();

        // Validate time range
        if (startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }

        // Check for conflicts
        boolean hasConflict = visitRepository.existsByDoctorIdAndDateTimeConflict(
                request.getDoctorId(), startDateTime, endDateTime);

        if (hasConflict) {
            throw new IllegalArgumentException("Doctor has conflicting visit at this time");
        }

        // Create and save visit
        Visit visit = new Visit(startDateTime, endDateTime, patient, doctor);
        return visitRepository.save(visit);
    }
}