package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Logical Foreign Key → users.id (AuthService)
     * Links this doctor profile to the authenticated user account.
     * Maintained via inter-service REST call during registration.
     */
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    private String name;
    private String specialization;
    private Integer experience;
    private Double consultationFee;
    private String phone;

    @Column(unique = true)
    private String email;
    private Boolean available;
}