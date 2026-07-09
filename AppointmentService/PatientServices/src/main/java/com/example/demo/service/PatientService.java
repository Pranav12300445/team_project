package com.example.demo.service;


import java.util.List;

import com.example.demo.entity.*;

public interface PatientService {

    Patient createPatient(Patient patient);

    Patient getPatientById(Long id);

    List<Patient> getAllPatients();

    Patient updatePatient(Long id, Patient patient);

    void deletePatient(Long id);

    Patient registerPatient(Long userId, String name, String email);
}