package com.spring.be.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;



@Component
@ConfigurationProperties(prefix = "spring.cloud.aws")
@Data
@NoArgsConstructor
public class AwsProperties {

    private Credentials credentials;
    private String region;
    private String bucketName;

    @Data
    @NoArgsConstructor
    public static class Credentials {
        private String accessKey;
        private String secretKey;
    }
}
