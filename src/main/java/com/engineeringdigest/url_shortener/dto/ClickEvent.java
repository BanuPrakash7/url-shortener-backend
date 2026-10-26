package com.engineeringdigest.url_shortener.dto;

public record ClickEvent(
        String shortCode,
        String clickedAt,
        String referrer,
        String userAgent
) {
}