package com.shaan.appointmentservice.service;

import com.shaan.appointmentservice.dto.AppointmentRequest;
import com.shaan.appointmentservice.dto.AppointmentResponse;
import com.shaan.appointmentservice.entity.Appointment;
import com.shaan.appointmentservice.exception.AppointmentNotFoundException;
import com.shaan.appointmentservice.mapper.AppointmentMapper;
import com.shaan.appointmentservice.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository repository;

    public AppointmentServiceImpl(AppointmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public AppointmentResponse createAppointment(AppointmentRequest request) {

        Appointment appointment = AppointmentMapper.toEntity(request);

        Appointment savedAppointment = repository.save(appointment);

        return AppointmentMapper.toResponse(savedAppointment);
    }

    @Override
    public AppointmentResponse getAppointmentById(Long id) {

        Appointment appointment = repository.findById(id)
                .orElseThrow(() ->
                        new AppointmentNotFoundException(
                                "Appointment with ID " + id + " not found"));

        return AppointmentMapper.toResponse(appointment);
    }

    @Override
    public List<AppointmentResponse> getAllAppointments() {

        return repository.findAll()
                .stream()
                .map(AppointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentResponse updateAppointment(Long id,
                                                 AppointmentRequest request) {

        Appointment appointment = repository.findById(id)
                .orElseThrow(() ->
                        new AppointmentNotFoundException(
                                "Appointment with ID " + id + " not found"));

        appointment.setDoctorId(request.getDoctorId());
        appointment.setPatientId(request.getPatientId());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setReason(request.getReason());

        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment updatedAppointment = repository.save(appointment);

        return AppointmentMapper.toResponse(updatedAppointment);
    }

    @Override
    public void deleteAppointment(Long id) {

        Appointment appointment = repository.findById(id)
                .orElseThrow(() ->
                        new AppointmentNotFoundException(
                                "Appointment with ID " + id + " not found"));

        repository.delete(appointment);
    }

    @Override
    public AppointmentResponse updateAppointmentStatus(Long id, com.shaan.appointmentservice.enums.AppointmentStatus status) {
        Appointment appointment = repository.findById(id)
                .orElseThrow(() ->
                        new AppointmentNotFoundException(
                                "Appointment with ID " + id + " not found"));

        appointment.setStatus(status);
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment updatedAppointment = repository.save(appointment);

        return AppointmentMapper.toResponse(updatedAppointment);
    }
}