package com.legal.platform.dto;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}
