package com.spring.be.blogReview.controller;

import com.spring.be.blogReview.dto.BlogSaveRequestDto;
import com.spring.be.blogReview.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @PostMapping("/save")
    public ResponseEntity<Long> saveBlog(@RequestBody BlogSaveRequestDto request) {
        Long blogId = blogService.saveBlog(request.getBlogUrl());
        return ResponseEntity.ok(blogId);
    }
}
