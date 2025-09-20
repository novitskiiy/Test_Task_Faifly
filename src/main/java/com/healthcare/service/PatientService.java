package com.healthcare.service;

import com.healthcare.dto.*;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.Patient;
import com.healthcare.entity.Visit;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PatientRepository;
import com.healthcare.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository,
                          VisitRepository visitRepository,
                          DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.visitRepository = visitRepository;
        this.doctorRepository = doctorRepository;
    }

    public PatientsListResponse getPatientsList(Integer page, Integer size, String search, List<Long> doctorIds) {
        // Set default values
        int pageNumber = (page != null && page > 0) ? page - 1 : 0;
        int pageSize = (size != null && size > 0) ? size : 20;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Patient> patientsPage;

        // Get patients based on filters
        if (doctorIds != null && !doctorIds.isEmpty()) {
            patientsPage = patientRepository.findBySearchAndDoctorIds(search, doctorIds, pageable);
        } else {
            patientsPage = patientRepository.findBySearchCriteria(search, pageable);
        }

        List<Patient> patients = patientsPage.getContent();
        List<Long> patientIds = patients.stream()
                .map(Patient::getId)
                .collect(Collectors.toList());

        // Get all visits for these patients
        List<Visit> allVisits = new ArrayList<>();
        if (!patientIds.isEmpty()) {
            allVisits = visitRepository.findLastVisitsByPatients(patientIds);
        }

        // Group visits by patient and doctor
        Map<Long, Map<Long, Visit>> patientDoctorVisits = allVisits.stream()
                .collect(Collectors.groupingBy(
                        visit -> visit.getPatient().getId(),
                        Collectors.toMap(
                                visit -> visit.getDoctor().getId(),
                                visit -> visit,
                                (existing, replacement) ->
                                        existing.getStartDateTime().isAfter(replacement.getStartDateTime()) ?
                                                existing : replacement
                        )
                ));

        // Get doctor total patients count
        Set<Long> allDoctorIds = allVisits.stream()
                .map(visit -> visit.getDoctor().getId())
                .collect(Collectors.toSet());

        Map<Long, Long> doctorPatientCounts = new HashMap<>();
        if (!allDoctorIds.isEmpty()) {
            List<Object[]> counts = doctorRepository.countDistinctPatientsByDoctorIds(new ArrayList<>(allDoctorIds));
            for (Object[] count : counts) {
                doctorPatientCounts.put((Long) count[0], (Long) count[1]);
            }
        }

        // Build response
        List<PatientVisitResponse> responseData = patients.stream()
                .map(patient -> {
                    Map<Long, Visit> doctorVisits = patientDoctorVisits.getOrDefault(patient.getId(), new HashMap<>());

                    List<LastVisitResponse> lastVisits = doctorVisits.values().stream()
                            .sorted((v1, v2) -> v2.getStartDateTime().compareTo(v1.getStartDateTime()))
                            .map(visit -> {
                                Doctor doctor = visit.getDoctor();

                                // Convert datetime to doctor's timezone
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                                ZoneId doctorTimezone = ZoneId.of(doctor.getTimezone());

                                String startFormatted = visit.getStartDateTime()
                                        .atZone(ZoneId.systemDefault())
                                        .withZoneSameInstant(doctorTimezone)
                                        .format(formatter);

                                String endFormatted = visit.getEndDateTime()
                                        .atZone(ZoneId.systemDefault())
                                        .withZoneSameInstant(doctorTimezone)
                                        .format(formatter);

                                DoctorResponse doctorResponse = new DoctorResponse(
                                        doctor.getFirstName(),
                                        doctor.getLastName(),
                                        doctorPatientCounts.getOrDefault(doctor.getId(), 0L).intValue()
                                );

                                return new LastVisitResponse(startFormatted, endFormatted, doctorResponse);
                            })
                            .collect(Collectors.toList());

                    return new PatientVisitResponse(patient.getFirstName(), patient.getLastName(), lastVisits);
                })
                .collect(Collectors.toList());

        return new PatientsListResponse(responseData, patientsPage.getTotalElements());
    }
}