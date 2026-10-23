package com.company.service.auth.dto;

public record LoginResult(String token, long expiresInSeconds) {
}
