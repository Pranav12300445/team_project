package com.shaan.appointmentservice.dto;

import lombok.Data;

@Data
public class PatientDTO {

    private Long patientId;
    private String patientName;
    private int age;

}