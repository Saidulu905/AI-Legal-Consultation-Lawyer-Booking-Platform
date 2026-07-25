package com.legal.platform.service;

import com.legal.platform.dto.LawyerDTO;
import com.legal.platform.dto.ReviewDTO;
import com.legal.platform.model.Category;
import com.legal.platform.model.Availability;

import java.util.List;

public interface LawyerService {
    List<LawyerDTO> getAllLawyers(String searchQuery, Long categoryId);
    LawyerDTO getLawyerById(Long id);
    LawyerDTO approveLawyer(Long id, boolean approve);
    List<Category> getAllCategories();
    Category createCategory(Category category);
    
    // Availability
    List<Availability> getAvailability(Long lawyerId);
    List<Availability> setAvailability(Long lawyerId, List<Availability> availabilityList);
    List<String> getAvailableSlots(Long lawyerId, String date);
    
    // Reviews
    ReviewDTO addReview(Long clientId, Long lawyerId, int rating, String comment);
    List<ReviewDTO> getReviews(Long lawyerId);
}
