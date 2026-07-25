package com.legal.platform.controller;

import com.legal.platform.dto.AppointmentRequest;
import com.legal.platform.dto.AppointmentResponse;
import com.legal.platform.model.AppointmentStatus;
import com.legal.platform.model.User;
import com.legal.platform.model.LawyerProfile;
import com.legal.platform.repository.UserRepository;
import com.legal.platform.repository.LawyerProfileRepository;
import com.legal.platform.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;
    private final LawyerProfileRepository lawyerProfileRepository;

    @PostMapping
    public ResponseEntity<AppointmentResponse> bookAppointment(@RequestBody AppointmentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(appointmentService.bookAppointment(client.getId(), request));
    }

    @GetMapping("/client")
    public ResponseEntity<List<AppointmentResponse>> getClientAppointments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(appointmentService.getClientAppointments(client.getId()));
    }

    @GetMapping("/lawyer")
    public ResponseEntity<List<AppointmentResponse>> getLawyerAppointments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LawyerProfile profile = lawyerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Lawyer profile not found"));

        return ResponseEntity.ok(appointmentService.getLawyerAppointments(profile.getId()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AppointmentStatus status = AppointmentStatus.valueOf(body.get("status").toUpperCase());

        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(actor.getId(), id, status));
    }

    @GetMapping("/lawyer/earnings")
    public ResponseEntity<Double> getLawyerEarnings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LawyerProfile profile = lawyerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Lawyer profile not found"));

        return ResponseEntity.ok(appointmentService.getLawyerEarnings(profile.getId()));
    }
}
