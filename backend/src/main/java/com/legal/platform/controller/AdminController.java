package com.legal.platform.controller;

import com.legal.platform.dto.LawyerDTO;
import com.legal.platform.model.*;
import com.legal.platform.repository.*;
import com.legal.platform.service.AppointmentService;
import com.legal.platform.service.LawyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final LawyerProfileRepository lawyerProfileRepository;
    private final AuditLogRepository auditLogRepository;
    private final LawyerService lawyerService;
    private final AppointmentService appointmentService;
    private final ReviewRepository reviewRepository;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        // Return all users (excluding passwords in real production, but fine for local admin)
        List<User> users = userRepository.findAll().stream()
                .peek(u -> u.setPassword("[PROTECTED]"))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/lawyers/pending")
    public ResponseEntity<List<LawyerDTO>> getPendingLawyers() {
        List<LawyerDTO> pending = lawyerProfileRepository.findByIsApproved(false).stream()
                .map(this::convertToLawyerDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/lawyers/{id}/approve")
    public ResponseEntity<LawyerDTO> approveLawyer(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        boolean approve = body.getOrDefault("approve", true);
        return ResponseEntity.ok(lawyerService.approveLawyer(id, approve));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPlatformStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalUsers", userRepository.count());
        stats.put("totalClients", userRepository.countByRole(Role.CLIENT));
        stats.put("totalLawyers", userRepository.countByRole(Role.LAWYER));
        stats.put("pendingLawyers", lawyerProfileRepository.findByIsApproved(false).size());
        
        stats.put("totalAppointments", appointmentService.getActiveAppointmentsCount());
        stats.put("completedAppointments", appointmentService.getCompletedAppointmentsCount());
        stats.put("totalRevenue", appointmentService.getTotalPlatformRevenue());
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/audit")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByCreatedAtDesc());
    }

    private LawyerDTO convertToLawyerDTO(LawyerProfile profile) {
        double avgRating = reviewRepository.getAverageRatingForLawyer(profile);
        return LawyerDTO.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .name(profile.getUser().getName())
                .email(profile.getUser().getEmail())
                .specialization(profile.getSpecialization())
                .bio(profile.getBio())
                .experienceYears(profile.getExperienceYears())
                .hourlyRate(profile.getHourlyRate())
                .categoryId(profile.getCategory() != null ? profile.getCategory().getId() : null)
                .categoryName(profile.getCategory() != null ? profile.getCategory().getName() : "General")
                .isApproved(profile.isApproved())
                .averageRating(avgRating)
                .build();
    }
}
