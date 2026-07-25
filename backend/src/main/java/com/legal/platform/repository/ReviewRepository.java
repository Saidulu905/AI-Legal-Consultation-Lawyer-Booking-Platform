package com.legal.platform.repository;

import com.legal.platform.model.Review;
import com.legal.platform.model.LawyerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByLawyerOrderByCreatedAtDesc(LawyerProfile lawyer);
    
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.lawyer = :lawyer")
    double getAverageRatingForLawyer(@Param("lawyer") LawyerProfile lawyer);
}
