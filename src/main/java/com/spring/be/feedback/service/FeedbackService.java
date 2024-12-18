package com.spring.be.feedback.service;

import com.spring.be.entity.ResultFeedback;
import com.spring.be.entity.ReviewCheckResult;
import com.spring.be.entity.User;
import com.spring.be.feedback.dto.FeedbackRequestDto;
import com.spring.be.feedback.dto.FeedbackResponseDto;
import com.spring.be.feedback.repository.FeedbackRepository;
import com.spring.be.reviewcheck.repository.ReviewCheckResultRepository;
import com.spring.be.util.RedisCacheUtil;
import com.spring.be.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final ReviewCheckResultRepository reviewCheckResultRepository;
    private final UserRepository userRepository;
    private final RedisCacheUtil redisCacheUtil;

    public FeedbackResponseDto createFeedback(Long userId, FeedbackRequestDto feedbackRequestDto) {
        // Redis에서 검사 결과 조회
        String cacheKey = "reviewResult:" + feedbackRequestDto.getResultId();
        ReviewCheckResult reviewCheckResult = redisCacheUtil.getCachedResult(cacheKey, ReviewCheckResult.class);

        if (reviewCheckResult == null) {
            throw new EntityNotFoundException("ReviewCheckResult not found in Redis for ID: " + feedbackRequestDto.getResultId());
        }

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found for ID: " + userId));

        // 데이터베이스에 검사 결과가 없는 경우 저장
        if (!reviewCheckResultRepository.existsById(reviewCheckResult.getResultId())) {
            reviewCheckResultRepository.save(reviewCheckResult);
            System.out.println("Saved ReviewCheckResult to database: " + reviewCheckResult.getResultId());
        }

        // 피드백 생성
        ResultFeedback feedback = new ResultFeedback();
        feedback.setReviewCheckResult(reviewCheckResult);
        feedback.setUser(user);
        feedback.setType(feedbackRequestDto.getType());
        feedback.setReason(feedbackRequestDto.getReason());

        // 피드백 저장
        ResultFeedback savedFeedback = feedbackRepository.save(feedback);

        // 응답 DTO 생성
        FeedbackResponseDto responseDto = new FeedbackResponseDto();
        responseDto.setFeedbackId(savedFeedback.getFeedbackId());
        responseDto.setResultId(savedFeedback.getReviewCheckResult().getResultId());
        responseDto.setType(savedFeedback.getType());
        responseDto.setReason(savedFeedback.getReason());
        return responseDto;

    }
}
