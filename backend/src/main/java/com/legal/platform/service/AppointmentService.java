package com.legal.platform.service;

import com.legal.platform.dto.AppointmentRequest;
import com.legal.platform.dto.AppointmentResponse;
import com.legal.platform.model.AppointmentStatus;

import java.util.List;

public interface AppointmentService {
    AppointmentResponse bookAppointment(Long clientId, AppointmentRequest request);
    List<AppointmentResponse> getClientAppointments(Long clientId);
    List<AppointmentResponse> getLawyerAppointments(Long lawyerId);
    AppointmentResponse updateAppointmentStatus(Long userId, Long appointmentId, AppointmentStatus status);
    
    // Stats for Dashboards
    double getLawyerEarnings(Long lawyerId);
    long getActiveAppointmentsCount();
    long getCompletedAppointmentsCount();
    double getTotalPlatformRevenue();
}
