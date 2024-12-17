package com.spring.be.feedback.repository;

import com.spring.be.entity.ResultFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<ResultFeedback, Long> {
}
