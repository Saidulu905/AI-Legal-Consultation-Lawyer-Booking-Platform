package com.legal.platform.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String code;
    private String newPassword;
}
