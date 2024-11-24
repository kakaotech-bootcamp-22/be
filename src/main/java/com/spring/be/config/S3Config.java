package com.spring.be.config;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;


@Configuration
public class S3Config {

    private final Environment environment;

    public S3Config(Environment environment){
        this.environment = environment;
    }

    @Bean
    public S3Client s3Client() {
        String accessKey = environment.getProperty("spring.cloud.aws.credentials.accessKey");
        String secretKey = environment.getProperty("spring.cloud.aws.credentials.secretKey");
        String region = environment.getProperty("spring.cloud.aws.region.static");

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
