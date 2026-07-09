package com.shaan.appointmentservice.client;

import com.shaan.appointmentservice.dto.DoctorDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "DOCTOR-SERVICE")
public interface DoctorClient {

    @GetMapping("/doctors/{id}")
    DoctorDTO getDoctorById(@PathVariable Long id);

}