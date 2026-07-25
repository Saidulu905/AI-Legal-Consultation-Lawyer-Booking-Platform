package com.legal.platform.repository;

import com.legal.platform.model.LawyerProfile;
import com.legal.platform.model.User;
import com.legal.platform.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface LawyerProfileRepository extends JpaRepository<LawyerProfile, Long> {
    Optional<LawyerProfile> findByUser(User user);
    List<LawyerProfile> findByIsApproved(boolean isApproved);
    List<LawyerProfile> findByCategoryAndIsApproved(Category category, boolean isApproved);
    
    @Query("SELECT l FROM LawyerProfile l WHERE l.isApproved = true AND " +
           "(LOWER(l.specialization) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.user.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.bio) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<LawyerProfile> searchLawyers(@Param("query") String query);
}
