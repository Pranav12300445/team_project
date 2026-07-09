package com.shaan.appointmentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PatientDTO {

    @JsonProperty("id")
    private Long patientId;

    @JsonProperty("name")
    private String patientName;

    private int age;

}