package com.nodestand.service.email;

import com.nodestand.service.AmazonServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationServicesConfig {

    private final AmazonServiceConfig amazonServiceConfig;

    @Autowired
    public NotificationServicesConfig(AmazonServiceConfig amazonServiceConfig) {
        this.amazonServiceConfig = amazonServiceConfig;
    }

    @Bean
    public CommentNotificationSender commentNotificationSender() {
        return new CommentNotificationSender(amazonServiceConfig.simpleEmailService());
    }

}
