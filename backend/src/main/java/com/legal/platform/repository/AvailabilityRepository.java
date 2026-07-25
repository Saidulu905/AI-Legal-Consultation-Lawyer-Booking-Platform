package com.legal.platform.repository;

import com.legal.platform.model.Availability;
import com.legal.platform.model.LawyerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByLawyer(LawyerProfile lawyer);
    List<Availability> findByLawyerAndDayOfWeek(LawyerProfile lawyer, String dayOfWeek);
    List<Availability> findByLawyerAndDayOfWeekAndIsAvailable(LawyerProfile lawyer, String dayOfWeek, boolean isAvailable);
}
