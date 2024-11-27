package com.spring.be.reviewcheck.repository;

import com.spring.be.entity.ReviewCheckResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewCheckResultRepository extends JpaRepository<ReviewCheckResult, Long> {
}