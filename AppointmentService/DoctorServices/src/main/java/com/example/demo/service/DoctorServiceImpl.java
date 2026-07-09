package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Doctor;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repo.DoctorRepository;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository repository;

    public DoctorServiceImpl(DoctorRepository repository) {
        this.repository = repository;
    }

    @Override
    public Doctor createDoctor(Doctor doctor) {
        return repository.save(doctor);
    }

    @Override
    public Doctor getDoctorById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with id: " + id));
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return repository.findAll();
    }

    @Override
    public Doctor updateDoctor(Long id, Doctor doctor) {

        Doctor existingDoctor = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with id: " + id));

        existingDoctor.setName(doctor.getName());
        existingDoctor.setSpecialization(
                doctor.getSpecialization());
        existingDoctor.setExperience(
                doctor.getExperience());
        existingDoctor.setConsultationFee(
                doctor.getConsultationFee());
        existingDoctor.setPhone(
                doctor.getPhone());
        existingDoctor.setEmail(
                doctor.getEmail());
        existingDoctor.setAvailable(
                doctor.getAvailable());

        return repository.save(existingDoctor);
    }

    @Override
    public void deleteDoctor(Long id) {

        Doctor doctor = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with id: " + id));

        repository.delete(doctor);
    }

    @Override
    public Doctor registerDoctor(Long userId, String name, String email) {

        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Doctor with email " + email + " already exists.");
        }

        Doctor doctor = new Doctor();
        doctor.setUserId(userId);
        doctor.setName(name);
        doctor.setEmail(email);
        doctor.setAvailable(true);

        return repository.save(doctor);
    }
}
