package com.spring.be.blogReview.repository;

import com.spring.be.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    @Query("SELECT b.reviewCount FROM Blog b WHERE b.blogId = :blogId")
    Integer findReviewCountByBlogId(Long blogId);
}
