package com.medtrack.dto.response;

public record AuthResponse(
        String token,
        String email,
        String role
) {}
