package com.healthcare.repository;

import com.healthcare.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @Query("SELECT COUNT(DISTINCT v.patient.id) FROM Visit v WHERE v.doctor.id = :doctorId")
    Long countDistinctPatientsByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT v.doctor.id, COUNT(DISTINCT v.patient.id) FROM Visit v WHERE v.doctor.id IN :doctorIds GROUP BY v.doctor.id")
    List<Object[]> countDistinctPatientsByDoctorIds(@Param("doctorIds") List<Long> doctorIds);

}