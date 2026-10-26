package com.engineeringdigest.url_shortener.repository;

import com.engineeringdigest.url_shortener.entity.Click;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClickRepository extends JpaRepository<Click, Long> {

    @Query("SELECT c.urlId, COUNT(c) FROM Click c where c.urlId in :urlIds GROUP BY c.urlId")
    List<Object[]> countByUrlIds(@Param("urlIds") List<Long> urlIds);
}