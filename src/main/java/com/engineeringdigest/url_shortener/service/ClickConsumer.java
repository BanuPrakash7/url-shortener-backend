package com.engineeringdigest.url_shortener.service;

import com.engineeringdigest.url_shortener.dto.ClickEvent;
import com.engineeringdigest.url_shortener.entity.Click;
import com.engineeringdigest.url_shortener.entity.Url;
import com.engineeringdigest.url_shortener.repository.ClickRepository;
import com.engineeringdigest.url_shortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClickConsumer {

    private final ClickRepository clickRepository;
    private final UrlRepository urlRepository;

    @KafkaListener(topics = "click-events")
    public void consume(ClickEvent event){
        Url url = urlRepository.findByShortCode(event.shortCode()).orElse(null);
        if (url == null) {
            return;
        }
        Click click = new Click();
        click.setUrlId(url.getId());
        click.setClickedAt(LocalDateTime.parse(event.clickedAt()));
        click.setReferrer(event.referrer());
        click.setUserAgent(event.userAgent());
        clickRepository.save(click);
    }
}
