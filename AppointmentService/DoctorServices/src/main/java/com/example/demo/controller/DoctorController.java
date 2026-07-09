package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.DoctorRegisterRequest;
import com.example.demo.entity.Doctor;
import com.example.demo.service.DoctorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/doctors")
@Tag(name = "Doctor Management", description = "APIs for managing the hospital's doctor directory — add staff, update schedules, track availability")
public class DoctorController {

    private final DoctorService service;

    public DoctorController(DoctorService service) {
        this.service = service;
    }

    @Operation(summary = "Register a doctor from auth service",
               description = "Internal endpoint called by AuthService during registration. Creates a doctor profile with basic info (name, email). The doctor can update their full profile later.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Doctor profile created successfully"),
        @ApiResponse(responseCode = "400", description = "Doctor with this email already exists")
    })
    @PostMapping("/register")
    public Doctor registerDoctor(@RequestBody DoctorRegisterRequest request) {
        return service.registerDoctor(
                request.getUserId(),
                request.getName(),
                request.getEmail());
    }

    @Operation(summary = "Add a doctor to the hospital staff",
               description = "Adds a new doctor to the hospital directory with their specialization, years of experience, consultation fee, contact details, and current availability status. Used by hospital admins to onboard new doctors.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Doctor added to the hospital staff successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public Doctor createDoctor(@RequestBody Doctor doctor) {
        return service.createDoctor(doctor);
    }

    @Operation(summary = "Look up a doctor's profile",
               description = "Fetches a specific doctor's full profile including their specialization, experience, consultation fee, and availability. Useful for patients searching for a doctor or admins verifying details.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Doctor profile found"),
        @ApiResponse(responseCode = "404", description = "No doctor exists with the given ID")
    })
    @GetMapping("/{id}")
    public Doctor getDoctor(
            @Parameter(description = "Unique ID of the doctor", example = "1")
            @PathVariable Long id) {
        return service.getDoctorById(id);
    }

    @Operation(summary = "List all doctors in the hospital",
               description = "Returns the complete hospital doctor directory with all profiles. Can be used to display available doctors to patients or for admin dashboards.")
    @ApiResponse(responseCode = "200", description = "Complete list of all hospital doctors")
    @GetMapping
    public List<Doctor> getAllDoctors() {
        return service.getAllDoctors();
    }

    @Operation(summary = "Update a doctor's profile",
               description = "Modifies an existing doctor's information — update specialization, change consultation fee, update contact details, or toggle availability status (e.g., when a doctor goes on leave).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Doctor profile updated successfully"),
        @ApiResponse(responseCode = "404", description = "No doctor exists with the given ID")
    })
    @PutMapping("/{id}")
    public Doctor updateDoctor(
            @Parameter(description = "Unique ID of the doctor to update", example = "1")
            @PathVariable Long id,
            @RequestBody Doctor doctor) {
        return service.updateDoctor(id, doctor);
    }

    @Operation(summary = "Remove a doctor from the hospital",
               description = "Permanently removes a doctor from the hospital directory. Used when a doctor resigns or is no longer part of the hospital staff.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Doctor removed from hospital successfully"),
        @ApiResponse(responseCode = "404", description = "No doctor exists with the given ID")
    })
    @DeleteMapping("/{id}")
    public String deleteDoctor(
            @Parameter(description = "Unique ID of the doctor to remove", example = "1")
            @PathVariable Long id) {
        service.deleteDoctor(id);
        return "Doctor deleted successfully";
    }
}
