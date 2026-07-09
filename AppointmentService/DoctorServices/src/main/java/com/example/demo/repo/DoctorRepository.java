package com.example.demo.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.*;

@Repository
public interface DoctorRepository
        extends JpaRepository<Doctor, Long> {

    boolean existsByEmail(String email);

    Optional<Doctor> findByUserId(Long userId);
}