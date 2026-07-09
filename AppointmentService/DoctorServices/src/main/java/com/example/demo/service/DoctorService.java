package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Doctor;

public interface DoctorService {

    Doctor createDoctor(Doctor doctor);

    Doctor getDoctorById(Long id);

    List<Doctor> getAllDoctors();

    Doctor updateDoctor(Long id, Doctor doctor);

    void deleteDoctor(Long id);

    Doctor registerDoctor(Long userId, String name, String email);
}
