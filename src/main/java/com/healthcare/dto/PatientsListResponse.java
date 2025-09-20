package com.healthcare.dto;

import java.util.List;

public class PatientsListResponse {

    private List<PatientVisitResponse> data;
    private Long count;

    public PatientsListResponse() {}

    public PatientsListResponse(List<PatientVisitResponse> data, Long count) {
        this.data = data;
        this.count = count;
    }

    // Getters and Setters
    public List<PatientVisitResponse> getData() {
        return data;
    }

    public void setData(List<PatientVisitResponse> data) {
        this.data = data;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}