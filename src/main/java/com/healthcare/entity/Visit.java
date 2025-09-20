package com.healthcare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "visits")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Start date time is required")
    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @NotNull(message = "End date time is required")
    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    public Visit() {}

    public Visit(LocalDateTime startDateTime, LocalDateTime endDateTime, Patient patient, Doctor doctor) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.patient = patient;
        this.doctor = doctor;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    @Override
    public String toString() {
        return "Visit{" +
                "id=" + id +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", patient=" + patient +
                ", doctor=" + doctor +
                '}';
    }
}