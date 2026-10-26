package com.engineeringdigest.url_shortener.dto;

import java.time.LocalDateTime;

public record UrlResponse(
        Long id,
        String shortUrl,
        String shortCode,
        String longUrl,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        Long clicks
) {
}
