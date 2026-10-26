package com.engineeringdigest.url_shortener.service;


import com.engineeringdigest.url_shortener.dto.ClickEvent;
import com.engineeringdigest.url_shortener.entity.Click;
import com.engineeringdigest.url_shortener.entity.Url;
import com.engineeringdigest.url_shortener.repository.ClickRepository;
import com.engineeringdigest.url_shortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClickService {
    private static final String TOPIC = "click-events";

    private final UrlRepository urlRepository;
    private final ClickRepository clickRepository;
    private final KafkaTemplate<String, ClickEvent> kafkaTemplate;

    public void record(String shortCode, String referrer, String userAgent) {
        ClickEvent event = new ClickEvent(
                shortCode,
                LocalDateTime.now().toString(),
                referrer,
                userAgent
        );
        kafkaTemplate.send(TOPIC, event);
    }
}
