package com.healthcare.dto;

public class PatientVisitResponse {

    private String firstName;
    private String lastName;
    private java.util.List<LastVisitResponse> lastVisits;

    public PatientVisitResponse() {}

    public PatientVisitResponse(String firstName, String lastName, java.util.List<LastVisitResponse> lastVisits) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.lastVisits = lastVisits;
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public java.util.List<LastVisitResponse> getLastVisits() {
        return lastVisits;
    }

    public void setLastVisits(java.util.List<LastVisitResponse> lastVisits) {
        this.lastVisits = lastVisits;
    }
}