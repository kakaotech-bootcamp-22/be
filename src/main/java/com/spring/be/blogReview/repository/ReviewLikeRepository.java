package com.spring.be.blogReview.repository;

import com.spring.be.entity.BlogReview;
import com.spring.be.entity.ReviewLike;
import com.spring.be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    Optional<ReviewLike> findByBlogReviewAndUser(BlogReview blogReview, User user);
    boolean existsByBlogReviewAndUser(BlogReview blogReview, User user);
    int countByUser_UserId(Long userId);
}
