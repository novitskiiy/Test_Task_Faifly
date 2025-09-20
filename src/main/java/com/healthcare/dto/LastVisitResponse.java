package com.healthcare.dto;

public class LastVisitResponse {

    private String start;
    private String end;
    private DoctorResponse doctor;

    public LastVisitResponse() {}

    public LastVisitResponse(String start, String end, DoctorResponse doctor) {
        this.start = start;
        this.end = end;
        this.doctor = doctor;
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

    public DoctorResponse getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorResponse doctor) {
        this.doctor = doctor;
    }
}