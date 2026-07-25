package com.legal.platform.service.impl;

import com.legal.platform.dto.LawyerDTO;
import com.legal.platform.dto.ReviewDTO;
import com.legal.platform.model.*;
import com.legal.platform.repository.*;
import com.legal.platform.service.LawyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LawyerServiceImpl implements LawyerService {

    private final LawyerProfileRepository lawyerProfileRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    public List<LawyerDTO> getAllLawyers(String searchQuery, Long categoryId) {
        List<LawyerProfile> profiles;
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            profiles = lawyerProfileRepository.searchLawyers(searchQuery);
        } else {
            profiles = lawyerProfileRepository.findAll();
        }

        if (categoryId != null) {
            profiles = profiles.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        return profiles.stream()
                .map(this::convertToLawyerDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LawyerDTO getLawyerById(Long id) {
        LawyerProfile profile = lawyerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lawyer not found"));
        return convertToLawyerDTO(profile);
    }

    @Override
    @Transactional
    public LawyerDTO approveLawyer(Long id, boolean approve) {
        LawyerProfile profile = lawyerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lawyer not found"));
        
        profile.setApproved(approve);
        LawyerProfile saved = lawyerProfileRepository.save(profile);

        auditLogRepository.save(AuditLog.builder()
                .action(approve ? "LAWYER_APPROVED" : "LAWYER_REJECTED")
                .performedBy("ADMIN")
                .details("Approved status set to " + approve + " for lawyer profile " + id)
                .build());

        return convertToLawyerDTO(saved);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new RuntimeException("Category already exists");
        }
        return categoryRepository.save(category);
    }

    @Override
    public List<Availability> getAvailability(Long lawyerId) {
        LawyerProfile lawyer = lawyerProfileRepository.findById(lawyerId)
                .orElseThrow(() -> new RuntimeException("Lawyer not found"));
        return availabilityRepository.findByLawyer(lawyer);
    }

    @Override
    @Transactional
    public List<Availability> setAvailability(Long lawyerId, List<Availability> availabilityList) {
        LawyerProfile lawyer = lawyerProfileRepository.findById(lawyerId)
                .orElseThrow(() -> new RuntimeException("Lawyer not found"));

        // Delete existing availability slots for this lawyer
        List<Availability> oldAvailabilities = availabilityRepository.findByLawyer(lawyer);
        availabilityRepository.deleteAll(oldAvailabilities);

        // Save new ones
        for (Availability av : availabilityList) {
            av.setLawyer(lawyer);
        }
        return availabilityRepository.saveAll(availabilityList);
    }

    @Override
    public List<String> getAvailableSlots(Long lawyerId, String date) {
        LawyerProfile lawyer = lawyerProfileRepository.findById(lawyerId)
                .orElseThrow(() -> new RuntimeException("Lawyer not found"));

        LocalDate localDate = LocalDate.parse(date);
        String dayOfWeekStr = localDate.getDayOfWeek().name(); // MONDAY
        String formattedDay = dayOfWeekStr.substring(0, 1) + dayOfWeekStr.substring(1).toLowerCase(); // Monday

        List<Availability> availabilities = availabilityRepository.findByLawyerAndDayOfWeekAndIsAvailable(lawyer, formattedDay, true);
        List<String> availableSlots = new ArrayList<>();

        for (Availability av : availabilities) {
            LocalTime tempTime = av.getStartTime();
            while (tempTime.isBefore(av.getEndTime())) {
                LocalTime nextTime = tempTime.plusHours(1);
                if (nextTime.isAfter(av.getEndTime())) {
                    break;
                }

                // Check if this specific slot is already booked
                boolean isBooked = appointmentRepository.existsByLawyerAndAppointmentDateAndStartTimeAndStatusNot(
                        lawyer,
                        localDate,
                        tempTime,
                        AppointmentStatus.REJECTED
                );

                if (!isBooked) {
                    availableSlots.add(tempTime.toString() + "-" + nextTime.toString());
                }

                tempTime = nextTime;
            }
        }

        return availableSlots;
    }

    @Override
    @Transactional
    public ReviewDTO addReview(Long clientId, Long lawyerId, int rating, String comment) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        LawyerProfile lawyer = lawyerProfileRepository.findById(lawyerId)
                .orElseThrow(() -> new RuntimeException("Lawyer not found"));

        Review review = Review.builder()
                .client(client)
                .lawyer(lawyer)
                .rating(rating)
                .comment(comment)
                .build();

        Review savedReview = reviewRepository.save(review);
        
        // Audit log
        auditLogRepository.save(AuditLog.builder()
                .action("REVIEW_ADDED")
                .performedBy(client.getEmail())
                .details("Added review for lawyer profile id " + lawyerId + " with rating " + rating)
                .build());

        return convertToReviewDTO(savedReview);
    }

    @Override
    public List<ReviewDTO> getReviews(Long lawyerId) {
        LawyerProfile lawyer = lawyerProfileRepository.findById(lawyerId)
                .orElseThrow(() -> new RuntimeException("Lawyer not found"));
        return reviewRepository.findByLawyerOrderByCreatedAtDesc(lawyer).stream()
                .map(this::convertToReviewDTO)
                .collect(Collectors.toList());
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

    private ReviewDTO convertToReviewDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .clientId(review.getClient().getId())
                .clientName(review.getClient().getName())
                .lawyerId(review.getLawyer().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
