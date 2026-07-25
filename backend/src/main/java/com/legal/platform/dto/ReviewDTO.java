package com.legal.platform.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long clientId;
    private String clientName;
    private Long lawyerId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
