package com.springserver.springserver.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springserver.springserver.service.ShortUrlGeneratorService;
import com.springserver.springserver.service.ShortUrlGetterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@Slf4j
public class UrlShortenerController {
    private final ShortUrlGeneratorService mShortUrlService;
    private final ShortUrlGetterService mShortUrlGetterService;

    @Autowired
    public UrlShortenerController(final ShortUrlGeneratorService shortUrlService,
                                  final ShortUrlGetterService shortUrlGetterService) {
        this.mShortUrlService = shortUrlService;
        this.mShortUrlGetterService = shortUrlGetterService;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/v1/url")
    public ResponseEntity<String> getShortUrl(@RequestBody String urlJson) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String shortUrl = mapper.writeValueAsString(mShortUrlService.generateShortUrl(mapper.readValue(urlJson,
                    UrlRequest.class)));
            return ResponseEntity.ok(shortUrl);
        } catch (JsonProcessingException e) {
            log.error("Error occurred in getShortUrl : ", e);
        }

        return ResponseEntity.ok("FAIL");
    }

    @GetMapping(value = "/{url}")
    public RedirectView redirectUser(@PathVariable String url) {
        final String originalUrl = mShortUrlGetterService.getOriginalUrl(url);
        System.out.println("redirecting to original url: " + originalUrl);
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(originalUrl);
        return redirectView;
    }
}
