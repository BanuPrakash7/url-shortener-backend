package com.engineeringdigest.url_shortener.service;


import com.engineeringdigest.url_shortener.entity.Url;
import com.engineeringdigest.url_shortener.exception.AliasNotAvailableException;
import com.engineeringdigest.url_shortener.exception.ShortCodeGenerationException;
import com.engineeringdigest.url_shortener.exception.UrlExpiredException;
import com.engineeringdigest.url_shortener.exception.UrlNotFoundException;
import com.engineeringdigest.url_shortener.repository.ClickRepository;
import com.engineeringdigest.url_shortener.repository.UrlRepository;
import com.engineeringdigest.url_shortener.util.SecurityUtils;
import com.engineeringdigest.url_shortener.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final StringRedisTemplate redis;

    private static final Set<String> RESERVED = Set.of(
            "api", "login", "signup", "admin", "dashboard", "health", "static", "assets"
    );

    private static final int MAX_ATTEMPTS = 5;
    private static final int CODE_LENGTH = 7;
    private static final String KEY_PREFIX = "url:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final UrlRepository urlRepository;
    private final ClickRepository clickRepository;


    public Url create(String longUrl, String customAlias, LocalDateTime expiresAt) {
        if (customAlias != null && !customAlias.isBlank()) {
            return creteWithAlias(longUrl, customAlias, expiresAt);
        }
        return createWithRandomCode(longUrl, expiresAt);
    }

    private Url createWithRandomCode(String longUrl, LocalDateTime expiresAt) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            try {
                return save(longUrl, ShortCodeGenerator.generate(CODE_LENGTH), expiresAt);
            } catch (DataIntegrityViolationException e) {

            }
        }
        throw new ShortCodeGenerationException("Could not generate a short code");
    }

    private Url creteWithAlias(String longUrl, String customAlias, LocalDateTime expiresAt) {
        if (RESERVED.contains(customAlias.toLowerCase())) {
            throw new AliasNotAvailableException("This alias is reserved");
        }
        try {
            return save(longUrl, customAlias, expiresAt);
        } catch (DataIntegrityViolationException e) {
            throw new AliasNotAvailableException("This alias is already taken");
        }
    }

    private Url save(String longUrl, String shortCode, LocalDateTime expiresAt) {
        Url url = new Url();
        url.setLongUrl(longUrl);
        url.setShortCode(shortCode);
        url.setUserId(SecurityUtils.currentUserId());
        url.setExpiresAt(expiresAt);
        return urlRepository.save(url);
    }


    public String getLongUrl(String code) {
        String key = KEY_PREFIX + code;
        String cached = redis.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }
        Url url = urlRepository.findByShortCode(code).orElseThrow(() -> new UrlNotFoundException("Short URL not found"));
        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("This link has expired");
        }
        Duration ttl = CACHE_TTL;
        if (url.getExpiresAt() != null) {
            Duration untilExpiry = Duration.between(LocalDateTime.now(), url.getExpiresAt());
            if (untilExpiry.compareTo(ttl) < 0) {
                ttl = untilExpiry;
            }
        }
        if (ttl.isPositive()) {
            redis.opsForValue().set(key, url.getLongUrl(), ttl);
        }
        return url.getLongUrl();
    }


    public List<Url> listForCurrentUser() {
        return urlRepository.findByUserIdOrderByCreatedAtDesc(SecurityUtils.currentUserId());
    }

    public Map<Long, Long> clickCountsList(List<Url> urls) {
        if(urls.isEmpty()){
            return Map.of();
        }
        List<Long> ids = urls.stream().map(Url::getId).toList();
        return clickRepository.countByUrlIds(ids).stream()
                .collect(Collectors.toMap(x -> (Long) x[0], y -> (Long) y[1]));
    }

    public void delete(Long id) {
        Url url = getOwnedUrl(id);
        urlRepository.delete(url);
        redis.delete(KEY_PREFIX + url.getShortCode());
    }

    public Url getOwnedUrl(Long id) {
        return urlRepository.findByIdAndUserId(id, SecurityUtils.currentUserId())
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found"));
    }
}
