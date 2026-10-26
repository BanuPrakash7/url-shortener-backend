package com.engineeringdigest.url_shortener.controller;

import com.engineeringdigest.url_shortener.dto.CreateUrlRequest;
import com.engineeringdigest.url_shortener.dto.CreateUrlResponse;
import com.engineeringdigest.url_shortener.dto.UrlResponse;
import com.engineeringdigest.url_shortener.entity.Url;
import com.engineeringdigest.url_shortener.service.QrService;
import com.engineeringdigest.url_shortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;
    private final QrService qrService;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping
    public ResponseEntity<CreateUrlResponse> create(@Valid @RequestBody CreateUrlRequest request) {
        Url url = urlService.create(request.longUrl(), request.customAlias(), request.expiresAt());
        CreateUrlResponse response = new CreateUrlResponse(baseUrl + "/" + url.getShortCode(), url.getShortCode(), url.getLongUrl(), url.getExpiresAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping
    public List<UrlResponse> list() {
        List<Url> urls = urlService.listForCurrentUser();
        Map<Long, Long> counts = urlService.clickCountsList(urls);
        return urls.stream().map(x -> new UrlResponse(x.getId(), baseUrl + "/" + x.getShortCode(), x.getShortCode(), x.getLongUrl(), x.getExpiresAt(), x.getCreatedAt(), counts.getOrDefault(x.getId(), 0L))).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        urlService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/qr")
    public ResponseEntity<byte[]> qr(@PathVariable Long id) {
        Url url = urlService.getOwnedUrl(id);
        byte[] png = qrService.generate(baseUrl + "/" + url.getShortCode());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }

}
