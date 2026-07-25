package com.legal.platform.repository;

import com.legal.platform.model.Payment;
import com.legal.platform.model.Appointment;
import com.legal.platform.model.LawyerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByAppointment(Appointment appointment);
    
    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payment p WHERE p.appointment.lawyer = :lawyer AND p.status = 'PAID'")
    double calculateLawyerEarnings(@Param("lawyer") LawyerProfile lawyer);
}
