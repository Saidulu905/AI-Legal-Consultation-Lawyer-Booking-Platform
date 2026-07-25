package com.legal.platform.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LawyerDTO {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String specialization;
    private String bio;
    private int experienceYears;
    private double hourlyRate;
    private Long categoryId;
    private String categoryName;
    private boolean isApproved;
    private double averageRating;
}
