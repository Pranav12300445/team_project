package com.example.demo.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Patient;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repo.PatientRepository;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository repository;

    public PatientServiceImpl(PatientRepository repository) {
        this.repository = repository;
    }

    @Override
    public Patient createPatient(Patient patient) {
        return repository.save(patient);
    }

    @Override
    public Patient getPatientById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Patient not found with id: " + id));
    }

    @Override
    public List<Patient> getAllPatients() {
        return repository.findAll();
    }

    @Override
    public Patient updatePatient(Long id, Patient patient) {

        Patient existingPatient = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Patient not found with id: " + id));

        existingPatient.setName(patient.getName());
        existingPatient.setGender(patient.getGender());
        existingPatient.setAge(patient.getAge());
        existingPatient.setPhone(patient.getPhone());
        existingPatient.setEmail(patient.getEmail());
        existingPatient.setAddress(patient.getAddress());
        existingPatient.setBloodGroup(patient.getBloodGroup());
        existingPatient.setDateOfBirth(patient.getDateOfBirth());

        return repository.save(existingPatient);
    }

    @Override
    public void deletePatient(Long id) {

        Patient patient = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Patient not found with id: " + id));

        repository.delete(patient);
    }

    @Override
    public Patient registerPatient(Long userId, String name, String email) {

        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Patient with email " + email + " already exists.");
        }

        Patient newPatient = new Patient();
        newPatient.setUserId(userId);
        newPatient.setName(name);
        newPatient.setEmail(email);

        return repository.save(newPatient);
    }
}
