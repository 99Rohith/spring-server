package com.springserver.springserver.service;

import com.springserver.springserver.api.entity.UrlEntity;
import com.springserver.springserver.api.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShortUrlGetterService {
    private static final String HTTP_STR = "https://";
    private final UrlRepository urlRepository;

    @Autowired
    public ShortUrlGetterService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public String getOriginalUrl(final String shortUrl) {
        UrlEntity urlEntity = urlRepository.findByShortUrl(shortUrl);
        if (urlEntity == null) return null;
        String originalUrl = urlEntity.getOrigUrl();
        if (!originalUrl.startsWith(HTTP_STR)) {
            originalUrl = HTTP_STR + originalUrl;
        }

        return originalUrl;
    }
}
