package com.healthcare.controller;

import com.healthcare.dto.*;
import com.healthcare.entity.Visit;
import com.healthcare.service.PatientService;
import com.healthcare.service.VisitService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/visits")
@CrossOrigin(origins = "*")
public class VisitController {

    private final VisitService visitService;
    private final PatientService patientService;

    @Autowired
    public VisitController(VisitService visitService, PatientService patientService) {
        this.visitService = visitService;
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<Visit> createVisit(@Valid @RequestBody CreateVisitRequest request) {
        Visit visit = visitService.createVisit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(visit);
    }

    @GetMapping("/patients")
    public ResponseEntity<PatientsListResponse> getPatientsList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String doctorIds) {

        List<Long> doctorIdList = null;
        if (doctorIds != null && !doctorIds.trim().isEmpty()) {
            doctorIdList = Arrays.stream(doctorIds.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }

        PatientsListResponse response = patientService.getPatientsList(page, size, search, doctorIdList);
        return ResponseEntity.ok(response);
    }
}