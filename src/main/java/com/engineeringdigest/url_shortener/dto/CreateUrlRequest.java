package com.engineeringdigest.url_shortener.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateUrlRequest(

        @NotBlank(message = "longUrl is required")
        @Size(max = 2048, message = "longUrl must be at most 2048 characters")
        @Pattern(regexp = "^https?://.+", message = "longUrl must start with http:// or https://")
        String longUrl,

        @Size(min = 3, max = 30, message = "customAlias must be between 3 and 30 characters")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "customAlias can only contain letters, numbers, - and _")
        String customAlias,

        @Future(message = "expiresAt must be in the future")
        LocalDateTime expiresAt
) {
}