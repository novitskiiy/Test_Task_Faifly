package com.healthcare.repository;

import com.healthcare.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("SELECT p FROM Patient p WHERE " +
            "(:search IS NULL OR " +
            "LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Patient> findBySearchCriteria(@Param("search") String search, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Patient p " +
            "INNER JOIN p.visits v " +
            "WHERE (:search IS NULL OR " +
            "LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND v.doctor.id IN :doctorIds")
    Page<Patient> findBySearchAndDoctorIds(@Param("search") String search,
                                           @Param("doctorIds") List<Long> doctorIds,
                                           Pageable pageable);
}