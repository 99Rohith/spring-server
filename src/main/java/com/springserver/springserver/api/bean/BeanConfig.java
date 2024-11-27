package com.springserver.springserver.api.bean;

import com.springserver.springserver.service.ZooKeeperService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class BeanConfig {
    @Bean(name = "zookeeperService")
    @Scope("singleton")
    public ZooKeeperService zooKeeperService() {
        return new ZooKeeperService();
    }
}
