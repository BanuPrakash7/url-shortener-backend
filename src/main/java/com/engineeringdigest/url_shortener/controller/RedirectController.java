package com.engineeringdigest.url_shortener.controller;

import com.engineeringdigest.url_shortener.service.ClickService;
import com.engineeringdigest.url_shortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class RedirectController {
    private final UrlService urlService;
    private final ClickService clickService;

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code,
                                         @RequestHeader(value = "Referer", required = false) String referrer,
                                         @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        String longUrl = urlService.getLongUrl(code);
        clickService.record(code, referrer, userAgent);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(longUrl)).build();
    }

}
