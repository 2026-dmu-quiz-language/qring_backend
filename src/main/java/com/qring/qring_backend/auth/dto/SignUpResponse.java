package com.qring.qring_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignUpResponse {
    private Long userId;
    private String signupStatus;
    private String message;
}
