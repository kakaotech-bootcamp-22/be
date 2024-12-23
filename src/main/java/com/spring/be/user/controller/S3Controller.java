package com.spring.be.user.controller;

import com.spring.be.entity.User;
import com.spring.be.jwt.config.JwtUtils;
import com.spring.be.user.dto.S3ResponseDto;
import com.spring.be.user.service.S3Service;
import com.spring.be.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/s3")
public class S3Controller {

    private final S3Service s3Service;
    private final JwtUtils jwtUtils;

    public S3Controller(S3Service s3Service, JwtUtils jwtUtils) {
        this.s3Service = s3Service;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/upload")
    public ResponseEntity<S3ResponseDto> uploadProfileImage(@RequestParam("file") MultipartFile file) throws IOException {
        String fileUrl = s3Service.uploadFile(file);
        S3ResponseDto responseDto = new S3ResponseDto(fileUrl);
        return ResponseEntity.ok(responseDto);
    }
}
