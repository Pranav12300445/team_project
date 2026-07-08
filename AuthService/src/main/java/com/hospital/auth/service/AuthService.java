package com.hospital.auth.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.hospital.auth.dto.AuthResponse;
import com.hospital.auth.dto.LoginRequest;
import com.hospital.auth.dto.RegisterRequest;
import com.hospital.auth.entity.Role;
import com.hospital.auth.entity.User;
import com.hospital.auth.repository.UserRepository;
import com.hospital.auth.util.JwtUtil;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RestClient restClient;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            RestClient.Builder restClientBuilder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.restClient = restClientBuilder.build();
    }

    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        // Default role
        Role role = request.getRole();

        if (role == null) {
            role = Role.PATIENT;
        }

        // Only allow Patient and Doctor registration
        if (role != Role.PATIENT && role != Role.DOCTOR) {
            throw new IllegalArgumentException("Only PATIENT and DOCTOR registration is allowed.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(true)
                .build();

        userRepository.save(user);

        // Propagate registration to the appropriate downstream service
        if (role == Role.DOCTOR) {
            registerWithDoctorService(user);
        } else if (role == Role.PATIENT) {
            registerWithPatientService(user);
        }

        return role + " Registered Successfully";
    }

    private void registerWithDoctorService(User user) {
        try {
            restClient.post()
                    .uri("http://DOCTOR-SERVICE/doctors/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "userId", user.getId(),
                            "name", user.getUsername(),
                            "email", user.getEmail()))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Doctor profile created for user: {}", user.getEmail());
        } catch (Exception e) {
            log.warn("Failed to create doctor profile for user: {}. Reason: {}",
                    user.getEmail(), e.getMessage());
        }
    }

    private void registerWithPatientService(User user) {
        try {
            restClient.post()
                    .uri("http://PATIENT-SERVICE/patients/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "userId", user.getId(),
                            "name", user.getUsername(),
                            "email", user.getEmail()))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Patient profile created for user: {}", user.getEmail());
        } catch (Exception e) {
            log.warn("Failed to create patient profile for user: {}. Reason: {}",
                    user.getEmail(), e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        String token = jwtUtil.generateToken(user);

        return new AuthResponse(token);
    }
}