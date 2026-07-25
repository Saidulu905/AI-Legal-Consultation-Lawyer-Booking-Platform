package com.legal.platform.repository;

import com.legal.platform.model.Appointment;
import com.legal.platform.model.User;
import com.legal.platform.model.LawyerProfile;
import com.legal.platform.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByClientOrderByAppointmentDateDescStartTimeDesc(User client);
    List<Appointment> findByLawyerOrderByAppointmentDateDescStartTimeDesc(LawyerProfile lawyer);
    List<Appointment> findByLawyerAndAppointmentDate(LawyerProfile lawyer, LocalDate date);
    List<Appointment> findByLawyerAndAppointmentDateAndStatus(LawyerProfile lawyer, LocalDate date, AppointmentStatus status);
    
    boolean existsByLawyerAndAppointmentDateAndStartTimeAndStatusNot(
        LawyerProfile lawyer, 
        LocalDate date, 
        LocalTime startTime, 
        AppointmentStatus status
    );
}
