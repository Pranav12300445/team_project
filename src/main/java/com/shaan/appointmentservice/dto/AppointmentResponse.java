package com.shaan.appointmentservice.dto;

import com.shaan.appointmentservice.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long appointmentId;

    private Long doctorId;

    private Long patientId;

    private LocalDate appointmentDate;

    private LocalTime appointmentTime;

    private String reason;

    private AppointmentStatus status;

}