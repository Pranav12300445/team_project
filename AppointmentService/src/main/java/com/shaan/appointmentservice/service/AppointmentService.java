package com.shaan.appointmentservice.service;

import com.shaan.appointmentservice.dto.AppointmentRequest;
import com.shaan.appointmentservice.dto.AppointmentResponse;

import java.util.List;

public interface AppointmentService {

    AppointmentResponse createAppointment(AppointmentRequest request);

    AppointmentResponse getAppointmentById(Long id);

    List<AppointmentResponse> getAllAppointments();

    AppointmentResponse updateAppointment(Long id,
                                          AppointmentRequest request);

    void deleteAppointment(Long id);

    AppointmentResponse updateAppointmentStatus(Long id, com.shaan.appointmentservice.enums.AppointmentStatus status);
}