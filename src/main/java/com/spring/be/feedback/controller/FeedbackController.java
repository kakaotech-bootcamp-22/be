package com.spring.be.feedback.controller;

import com.spring.be.feedback.dto.FeedbackRequestDto;
import com.spring.be.feedback.dto.FeedbackResponseDto;
import com.spring.be.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponseDto> createFeedback(@RequestHeader("user-id") Long userId,
                                                              @RequestBody FeedbackRequestDto feedbackRequestDto) {
        FeedbackResponseDto feedbackResponseDto = feedbackService.createFeedback(userId, feedbackRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackResponseDto);
    }
}
