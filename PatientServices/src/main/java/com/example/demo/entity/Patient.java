package com.example.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Logical Foreign Key → users.id (AuthService)
     * Links this patient record to the authenticated user account.
     * Maintained via inter-service REST call during registration.
     */
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    private String name;
    private String gender;
    private Integer age;
    private String phone;

    @Column(unique = true)
    private String email;
    private String address;
    private String bloodGroup;
    private LocalDate dateOfBirth;
}