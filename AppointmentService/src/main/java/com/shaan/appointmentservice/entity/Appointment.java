package com.shaan.appointmentservice.entity;


import com.shaan.appointmentservice.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "idx_appointment_doctor_id", columnList = "doctor_id"),
    @Index(name = "idx_appointment_patient_id", columnList = "patient_id")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    /**
     * Logical Foreign Key → doctors.id (DoctorService)
     * References the doctor assigned to this appointment.
     */
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    /**
     * Logical Foreign Key → patients.id (PatientService)
     * References the patient who booked this appointment.
     */
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String reason;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
