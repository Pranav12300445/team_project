package com.shaan.appointmentservice.controller;

import com.shaan.appointmentservice.dto.AppointmentRequest;
import com.shaan.appointmentservice.dto.AppointmentResponse;
import com.shaan.appointmentservice.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Valid
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // Create Appointment
    @PostMapping
    @Valid

    public ResponseEntity<AppointmentResponse> createAppointment(
            @RequestBody AppointmentRequest request) {

        AppointmentResponse response =
                appointmentService.createAppointment(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Get Appointment by ID
    @GetMapping("/{id}")
    @Valid

    public ResponseEntity<AppointmentResponse> getAppointmentById(
            @PathVariable Long id) {

        AppointmentResponse response =
                appointmentService.getAppointmentById(id);

        return ResponseEntity.ok(response);
    }

    // Get All Appointments
    @GetMapping
    @Valid

    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {

        List<AppointmentResponse> appointments =
                appointmentService.getAllAppointments();

        return ResponseEntity.ok(appointments);
    }

    // Update Appointment
    @PutMapping("/{id}")
    @Valid

    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable Long id,
            @RequestBody AppointmentRequest request) {

        AppointmentResponse response =
                appointmentService.updateAppointment(id, request);

        return ResponseEntity.ok(response);
    }

    // Delete Appointment
    @DeleteMapping("/{id}")
    @Valid

    public ResponseEntity<String> deleteAppointment(
            @PathVariable Long id) {

        appointmentService.deleteAppointment(id);

        return ResponseEntity.ok("Appointment Deleted Successfully");
    }
}