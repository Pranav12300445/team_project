package com.shaan.appointmentservice.dto;

import lombok.Data;

@Data
public class DoctorDTO {

    private Long doctorId;
    private String doctorName;
    private String specialization;
    private boolean available;

}