package com.nodestand.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClient;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AmazonServiceConfig {

    @Autowired
    public AmazonServiceConfig(Environment environment) {
        // In some cases it's OK if these are null.
        // I may decide not to provide them and let the AWS SDK fall back
        // to the EC2 role (if we're currently running on EC2).
        // If you're running a dev environment you'll need these though.
        String accessKeyId = environment.getProperty("aws.accessKeyId");
        String secretKey = environment.getProperty("aws.secretKey");
        if (accessKeyId != null && secretKey != null) {
            System.setProperty("aws.accessKeyId", accessKeyId);
            System.setProperty("aws.secretKey", secretKey);
        }
    }

    @Bean
    public AWSLogs awsLogs() {
        return new AWSLogsClient();
    }

    @Bean
    public AmazonSimpleEmailService simpleEmailService() {
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();
    }

}
