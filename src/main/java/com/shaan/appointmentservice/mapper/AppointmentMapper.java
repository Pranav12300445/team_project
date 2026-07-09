package com.shaan.appointmentservice.mapper;

import com.shaan.appointmentservice.dto.AppointmentRequest;
import com.shaan.appointmentservice.dto.AppointmentResponse;
import com.shaan.appointmentservice.entity.Appointment;
import com.shaan.appointmentservice.enums.AppointmentStatus;

import java.time.LocalDateTime;

public class AppointmentMapper {

    // Request DTO → Entity
    public static Appointment toEntity(AppointmentRequest request) {

        Appointment appointment = new Appointment();

        appointment.setDoctorId(request.getDoctorId());
        appointment.setPatientId(request.getPatientId());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setReason(request.getReason());

        // Backend decides these values
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());

        return appointment;
    }

    // Entity → Response DTO
    public static AppointmentResponse toResponse(Appointment appointment) {

        AppointmentResponse response = new AppointmentResponse();

        response.setAppointmentId(appointment.getAppointmentId());
        response.setDoctorId(appointment.getDoctorId());
        response.setPatientId(appointment.getPatientId());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setAppointmentTime(appointment.getAppointmentTime());
        response.setReason(appointment.getReason());
        response.setStatus(appointment.getStatus());

        return response;
    }
}