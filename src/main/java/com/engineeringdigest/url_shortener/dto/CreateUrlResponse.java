package com.engineeringdigest.url_shortener.dto;

import java.time.LocalDateTime;

public record CreateUrlResponse(
        String shortUrl,
        String shortCode,
        String longUrl,
        LocalDateTime expiresAt
) {
}
