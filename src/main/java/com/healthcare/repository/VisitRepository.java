package com.healthcare.repository;

import com.healthcare.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query("SELECT v FROM Visit v " +
            "INNER JOIN FETCH v.patient p " +
            "INNER JOIN FETCH v.doctor d " +
            "WHERE p.id = :patientId AND v.doctor.id = :doctorId " +
            "ORDER BY v.startDateTime DESC")
    List<Visit> findLastVisitByPatientAndDoctor(@Param("patientId") Long patientId,
                                                @Param("doctorId") Long doctorId);

    @Query("SELECT v FROM Visit v " +
            "INNER JOIN FETCH v.patient p " +
            "INNER JOIN FETCH v.doctor d " +
            "WHERE p.id = :patientId " +
            "ORDER BY v.startDateTime DESC")
    List<Visit> findLastVisitsByPatient(@Param("patientId") Long patientId);

    @Query("SELECT v FROM Visit v " +
            "INNER JOIN FETCH v.patient p " +
            "INNER JOIN FETCH v.doctor d " +
            "WHERE p.id IN :patientIds " +
            "ORDER BY v.startDateTime DESC")
    List<Visit> findLastVisitsByPatients(@Param("patientIds") List<Long> patientIds);

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Visit v " +
            "WHERE v.doctor.id = :doctorId " +
            "AND ((v.startDateTime <= :startDateTime AND v.endDateTime > :startDateTime) OR " +
            "     (v.startDateTime < :endDateTime AND v.endDateTime >= :endDateTime) OR " +
            "     (v.startDateTime >= :startDateTime AND v.endDateTime <= :endDateTime))")
    boolean existsByDoctorIdAndDateTimeConflict(@Param("doctorId") Long doctorId,
                                                @Param("startDateTime") LocalDateTime startDateTime,
                                                @Param("endDateTime") LocalDateTime endDateTime);
}