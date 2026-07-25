package com.legal.platform.dto;

import com.legal.platform.model.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private Role role;
    
    // Lawyer profile parameters if registering as LAWYER
    private String specialization;
    private String bio;
    private Integer experienceYears;
    private Double hourlyRate;
    private Long categoryId;
}
