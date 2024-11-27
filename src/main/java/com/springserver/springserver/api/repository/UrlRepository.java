package com.springserver.springserver.api.repository;

import com.springserver.springserver.api.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRepository extends JpaRepository<UrlEntity, Integer> {
    UrlEntity findByShortUrl(String shortUrl);
}
