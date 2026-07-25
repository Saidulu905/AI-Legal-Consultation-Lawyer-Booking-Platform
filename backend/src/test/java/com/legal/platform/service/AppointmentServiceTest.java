package com.legal.platform.service;

import com.legal.platform.dto.AppointmentRequest;
import com.legal.platform.dto.AppointmentResponse;
import com.legal.platform.model.*;
import com.legal.platform.repository.*;
import com.legal.platform.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LawyerProfileRepository lawyerProfileRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private User clientUser;
    private User lawyerUser;
    private LawyerProfile lawyerProfile;
    private AppointmentRequest appointmentRequest;

    @BeforeEach
    void setUp() {
        clientUser = User.builder().id(1L).name("John Doe").email("client@legalplatform.com").role(Role.CLIENT).build();
        lawyerUser = User.builder().id(2L).name("Sarah Jenkins").email("lawyer@legalplatform.com").role(Role.LAWYER).build();
        
        lawyerProfile = LawyerProfile.builder()
                .id(1L)
                .user(lawyerUser)
                .isApproved(true)
                .hourlyRate(150.0)
                .build();

        appointmentRequest = new AppointmentRequest();
        appointmentRequest.setLawyerId(1L);
        appointmentRequest.setAppointmentDate(LocalDate.now().plusDays(2));
        appointmentRequest.setStartTime(LocalTime.of(10, 0));
        appointmentRequest.setEndTime(LocalTime.of(11, 0));
        appointmentRequest.setNotes("First consultation on labor dispute.");
    }

    @Test
    void bookAppointment_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(clientUser));
        when(lawyerProfileRepository.findById(1L)).thenReturn(Optional.of(lawyerProfile));
        when(appointmentRepository.existsByLawyerAndAppointmentDateAndStartTimeAndStatusNot(
                any(), any(), any(), eq(AppointmentStatus.REJECTED))).thenReturn(false);

        Appointment savedAppointment = Appointment.builder()
                .id(100L)
                .client(clientUser)
                .lawyer(lawyerProfile)
                .appointmentDate(appointmentRequest.getAppointmentDate())
                .startTime(appointmentRequest.getStartTime())
                .endTime(appointmentRequest.getEndTime())
                .status(AppointmentStatus.PENDING)
                .build();

        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        AppointmentResponse response = appointmentService.bookAppointment(1L, appointmentRequest);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("PENDING", response.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void bookAppointment_Conflict_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(clientUser));
        when(lawyerProfileRepository.findById(1L)).thenReturn(Optional.of(lawyerProfile));
        when(appointmentRepository.existsByLawyerAndAppointmentDateAndStartTimeAndStatusNot(
                any(), any(), any(), eq(AppointmentStatus.REJECTED))).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.bookAppointment(1L, appointmentRequest);
        });

        assertEquals("This time slot is already booked or pending review.", exception.getMessage());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void bookAppointment_LawyerNotApproved_ThrowsException() {
        lawyerProfile.setApproved(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(clientUser));
        when(lawyerProfileRepository.findById(1L)).thenReturn(Optional.of(lawyerProfile));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.bookAppointment(1L, appointmentRequest);
        });

        assertEquals("This lawyer profile is currently pending verification.", exception.getMessage());
    }
}
