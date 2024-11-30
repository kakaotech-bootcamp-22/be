package com.spring.be.blogReview.controller;

import com.spring.be.blogReview.dto.BlogReviewResponseDto;
import com.spring.be.blogReview.service.BlogReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/review")
public class BlogReviewController {

    @Autowired
    private BlogReviewService blogReviewService;

    @GetMapping("{blog_id}")
    public ResponseEntity<BlogReviewResponseDto> getBlogReviews(@PathVariable Long blog_id) {
        return ResponseEntity.ok(blogReviewService.getBlogReviewResponse(blog_id));
    }
}
