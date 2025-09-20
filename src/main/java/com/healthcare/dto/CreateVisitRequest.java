package com.healthcare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateVisitRequest {

    @NotBlank(message = "Start time is required")
    private String start;

    @NotBlank(message = "End time is required")
    private String end;

    @NotNull(message = "Patient ID is required")
    @Positive(message = "Patient ID must be positive")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    @Positive(message = "Doctor ID must be positive")
    private Long doctorId;

    public CreateVisitRequest() {}

    public CreateVisitRequest(String start, String end, Long patientId, Long doctorId) {
        this.start = start;
        this.end = end;
        this.patientId = patientId;
        this.doctorId = doctorId;
    }

    // Getters and Setters
    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
}