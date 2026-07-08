package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Patient;
import com.example.demo.service.PatientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patient Management", description = "APIs for managing patient medical records — admissions, record lookups, and profile updates")
public class PatientController {

    private final PatientService service;

    public PatientController(PatientService service) {
        this.service = service;
    }

    @Operation(summary = "Admit a new patient",
               description = "Creates a new patient medical record when a patient visits the hospital for the first time. Captures personal details like name, gender, age, contact information, address, blood group, and date of birth for hospital records.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Patient admitted and record created successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public Patient createPatient(@RequestBody Patient patient) {
        return service.createPatient(patient);
    }

    @Operation(summary = "Look up a patient's medical record",
               description = "Retrieves a patient's complete medical record by their ID. Used by doctors to review patient details before consultation, or by reception staff to verify patient identity.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Patient record found"),
        @ApiResponse(responseCode = "404", description = "No patient record exists with the given ID")
    })
    @GetMapping("/{id}")
    public Patient getPatient(
            @Parameter(description = "Unique ID of the patient record", example = "1")
            @PathVariable Long id) {
        return service.getPatientById(id);
    }

    @Operation(summary = "List all patient records",
               description = "Returns all patient records in the hospital system. Used by admin dashboards for patient management, or by doctors to browse patient lists.")
    @ApiResponse(responseCode = "200", description = "Complete list of all patient records")
    @GetMapping
    public List<Patient> getAllPatients() {
        return service.getAllPatients();
    }

    @Operation(summary = "Update a patient's record",
               description = "Updates an existing patient's medical record — correct contact details, update address after relocation, change emergency contact info, or update medical details like blood group.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Patient record updated successfully"),
        @ApiResponse(responseCode = "404", description = "No patient record exists with the given ID")
    })
    @PutMapping("/{id}")
    public Patient updatePatient(
            @Parameter(description = "Unique ID of the patient record to update", example = "1")
            @PathVariable Long id,
            @RequestBody Patient patient) {
        return service.updatePatient(id, patient);
    }

    @Operation(summary = "Discharge / remove a patient record",
               description = "Permanently removes a patient's record from the hospital system. Used when a patient is discharged and requests record deletion, or for data cleanup purposes.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Patient record removed successfully"),
        @ApiResponse(responseCode = "404", description = "No patient record exists with the given ID")
    })
    @DeleteMapping("/{id}")
    public String deletePatient(
            @Parameter(description = "Unique ID of the patient record to remove", example = "1")
            @PathVariable Long id) {
        service.deletePatient(id);
        return "Patient deleted successfully";
    }
}