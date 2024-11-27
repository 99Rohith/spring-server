package com.springserver.springserver.service;

import com.springserver.springserver.api.controller.UrlRequest;
import com.springserver.springserver.api.entity.UrlEntity;
import com.springserver.springserver.api.repository.UrlRepository;
import com.springserver.springserver.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShortUrlGeneratorService {
    private static final String BASE58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final ZooKeeperService zooKeeperService;
    private final UrlRepository urlRepository;

    @Autowired
    public ShortUrlGeneratorService(ZooKeeperService zooKeeperService, UrlRepository urlRepository) {
        this.zooKeeperService = zooKeeperService;
        this.urlRepository = urlRepository;
    }

    public UrlEntity generateShortUrl(final UrlRequest urlRequest) {
        String shortUrl = base58Encode(zooKeeperService.getCurLong());
        UrlEntity urlEntity = new UrlEntity();
        urlEntity.setOrigUrl(urlRequest.url);
        urlEntity.setShortUrl(shortUrl);
        log.info("Storing ==> " + urlEntity.getOrigUrl() + " : " + urlEntity.getShortUrl());
        urlRepository.save(urlEntity);
        urlEntity.setShortUrl(Utils.getServerIpAndPort() + shortUrl);
        return urlEntity;
    }

    private String base58Encode(long num) {
        StringBuilder ans = new StringBuilder();
        while (num > 0) {
            long rem = num % 58;
            ans.insert(0, BASE58.charAt((int) rem));
            num /= 58;
        }

        return ans.toString();
    }
}
