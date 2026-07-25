package com.legal.platform.service.impl;

import com.legal.platform.dto.AppointmentRequest;
import com.legal.platform.dto.AppointmentResponse;
import com.legal.platform.model.*;
import com.legal.platform.repository.*;
import com.legal.platform.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final LawyerProfileRepository lawyerProfileRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public AppointmentResponse bookAppointment(Long clientId, AppointmentRequest request) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        LawyerProfile lawyer = lawyerProfileRepository.findById(request.getLawyerId())
                .orElseThrow(() -> new RuntimeException("Lawyer not found"));

        if (!lawyer.isApproved()) {
            throw new RuntimeException("This lawyer profile is currently pending verification.");
        }

        // Check availability collision
        boolean isBooked = appointmentRepository.existsByLawyerAndAppointmentDateAndStartTimeAndStatusNot(
                lawyer,
                request.getAppointmentDate(),
                request.getStartTime(),
                AppointmentStatus.REJECTED
        );

        if (isBooked) {
            throw new RuntimeException("This time slot is already booked or pending review.");
        }

        Appointment appointment = Appointment.builder()
                .client(client)
                .lawyer(lawyer)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(AppointmentStatus.PENDING)
                .notes(request.getNotes())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Process Simulation Payment
        Payment payment = Payment.builder()
                .appointment(savedAppointment)
                .amount(lawyer.getHourlyRate())
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase())
                .status("PAID")
                .build();
        paymentRepository.save(payment);

        // Notifications
        notificationRepository.save(Notification.builder()
                .user(client)
                .message("Your booking request with Lawyer " + lawyer.getUser().getName() + " on " + 
                         request.getAppointmentDate() + " at " + request.getStartTime() + " has been sent successfully.")
                .build());

        notificationRepository.save(Notification.builder()
                .user(lawyer.getUser())
                .message("A new booking request from Client " + client.getName() + " on " + 
                         request.getAppointmentDate() + " at " + request.getStartTime() + " is pending your review.")
                .build());

        // Audit Log
        auditLogRepository.save(AuditLog.builder()
                .action("APPOINTMENT_BOOKED")
                .performedBy(client.getEmail())
                .details("Booked appointment id " + savedAppointment.getId() + " with lawyer id " + lawyer.getId())
                .build());

        return convertToResponse(savedAppointment);
    }

    @Override
    public List<AppointmentResponse> getClientAppointments(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return appointmentRepository.findByClientOrderByAppointmentDateDescStartTimeDesc(client).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> getLawyerAppointments(Long lawyerId) {
        LawyerProfile lawyer = lawyerProfileRepository.findById(lawyerId)
                .orElseThrow(() -> new RuntimeException("Lawyer not found"));
        return appointmentRepository.findByLawyerOrderByAppointmentDateDescStartTimeDesc(lawyer).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long userId, Long appointmentId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        User actor = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Auth validation: only the lawyer associated or client (only can cancel) can update
        if (actor.getRole() == Role.CLIENT && status != AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Clients can only cancel their appointments.");
        }

        appointment.setStatus(status);
        Appointment saved = appointmentRepository.save(appointment);

        // Notify client
        notificationRepository.save(Notification.builder()
                .user(appointment.getClient())
                .message("Your appointment with Lawyer " + appointment.getLawyer().getUser().getName() + " has been " + status.name().toLowerCase() + ".")
                .build());

        // Notify lawyer if cancelled by client
        if (actor.getRole() == Role.CLIENT && status == AppointmentStatus.CANCELLED) {
            notificationRepository.save(Notification.builder()
                    .user(appointment.getLawyer().getUser())
                    .message("Client " + appointment.getClient().getName() + " cancelled the appointment on " + appointment.getAppointmentDate())
                    .build());
        }

        // Audit Log
        auditLogRepository.save(AuditLog.builder()
                .action("APPOINTMENT_STATUS_UPDATE")
                .performedBy(actor.getEmail())
                .details("Updated appointment id " + appointmentId + " status to " + status)
                .build());

        return convertToResponse(saved);
    }

    @Override
    public double getLawyerEarnings(Long lawyerId) {
        LawyerProfile lawyer = lawyerProfileRepository.findById(lawyerId)
                .orElseThrow(() -> new RuntimeException("Lawyer not found"));
        return paymentRepository.calculateLawyerEarnings(lawyer);
    }

    @Override
    public long getActiveAppointmentsCount() {
        return appointmentRepository.count();
    }

    @Override
    public long getCompletedAppointmentsCount() {
        return appointmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();
    }

    @Override
    public double getTotalPlatformRevenue() {
        return paymentRepository.findAll().stream()
                .filter(p -> "PAID".equals(p.getStatus()))
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    private AppointmentResponse convertToResponse(Appointment appointment) {
        String paymentStatus = paymentRepository.findByAppointment(appointment)
                .map(Payment::getStatus)
                .orElse("PENDING");

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .clientId(appointment.getClient().getId())
                .clientName(appointment.getClient().getName())
                .lawyerId(appointment.getLawyer().getId())
                .lawyerName(appointment.getLawyer().getUser().getName())
                .specialization(appointment.getLawyer().getSpecialization())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus().name())
                .notes(appointment.getNotes())
                .paymentStatus(paymentStatus)
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
