package com.spring.be.user.service;

import com.spring.be.config.AwsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    public S3Service(S3Client s3Client, AwsProperties awsProperties) {
        this.s3Client = s3Client;
        this.awsProperties = awsProperties;
    }

    public String uploadFile(MultipartFile file) throws IOException {

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        Path tempFile = Files.createTempFile("upload-", fileName);
        file.transferTo(tempFile.toFile());

        s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(awsProperties.getBucketName())
                    .key(fileName)
                    .build(),
                tempFile
        );
        // 업로드된 객체의 URL 반환
        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", awsProperties.getBucketName(),  awsProperties.getRegion(),  fileName);
        System.out.println("업로드 성공, URL: " + fileUrl);

        // 임시 파일 삭제
        Files.delete(tempFile);

        return fileUrl; // 최종 URL 반환
    }
}
