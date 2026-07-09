package com.shaan.appointmentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DoctorDTO {

    @JsonProperty("id")
    private Long doctorId;

    @JsonProperty("name")
    private String doctorName;

    private String specialization;
    private boolean available;

}