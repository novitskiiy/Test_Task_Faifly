package com.healthcare.dto;

public class DoctorResponse {

    private String firstName;
    private String lastName;
    private Integer totalPatients;

    public DoctorResponse() {}

    public DoctorResponse(String firstName, String lastName, Integer totalPatients) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.totalPatients = totalPatients;
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

    public Integer getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(Integer totalPatients) {
        this.totalPatients = totalPatients;
    }
}