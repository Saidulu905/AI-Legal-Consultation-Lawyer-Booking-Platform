package com.legal.platform.controller;

import com.legal.platform.dto.LawyerDTO;
import com.legal.platform.dto.ReviewDTO;
import com.legal.platform.model.Availability;
import com.legal.platform.model.Category;
import com.legal.platform.model.User;
import com.legal.platform.model.LawyerProfile;
import com.legal.platform.repository.UserRepository;
import com.legal.platform.repository.LawyerProfileRepository;
import com.legal.platform.service.LawyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lawyers")
@RequiredArgsConstructor
public class LawyerController {

    private final LawyerService lawyerService;
    private final UserRepository userRepository;
    private final LawyerProfileRepository lawyerProfileRepository;

    @GetMapping
    public ResponseEntity<List<LawyerDTO>> getAllLawyers(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "categoryId", required = false) Long categoryId
    ) {
        return ResponseEntity.ok(lawyerService.getAllLawyers(search, categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LawyerDTO> getLawyerById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(lawyerService.getLawyerById(id));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(lawyerService.getAllCategories());
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<List<Availability>> getAvailability(@PathVariable("id") Long id) {
        return ResponseEntity.ok(lawyerService.getAvailability(id));
    }

    @PostMapping("/availability")
    public ResponseEntity<List<Availability>> setAvailability(@RequestBody List<Availability> availabilityList) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        LawyerProfile profile = lawyerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Lawyer profile not found"));
                
        return ResponseEntity.ok(lawyerService.setAvailability(profile.getId(), availabilityList));
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<List<String>> getAvailableSlots(
            @PathVariable("id") Long id,
            @RequestParam("date") String date
    ) {
        return ResponseEntity.ok(lawyerService.getAvailableSlots(id, date));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewDTO>> getReviews(@PathVariable("id") Long id) {
        return ResponseEntity.ok(lawyerService.getReviews(id));
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<ReviewDTO> addReview(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> body
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int rating = (Integer) body.get("rating");
        String comment = (String) body.get("comment");

        return ResponseEntity.ok(lawyerService.addReview(client.getId(), id, rating, comment));
    }
}
