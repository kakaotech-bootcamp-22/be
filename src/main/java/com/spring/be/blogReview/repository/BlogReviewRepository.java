package com.spring.be.blogReview.repository;

import com.spring.be.blogReview.dto.ReviewDto;
import com.spring.be.entity.BlogReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BlogReviewRepository extends JpaRepository<BlogReview, Long> {

    List<BlogReview> findByBlogBlogId(Long blogId);
    BlogReview findByBlogReviewId(Long blogReviewId);

    @Query("SELECT new com.spring.be.blogReview.dto.ReviewDto(" +
            "br.blogReviewId, " +
            "br.rating, " +
            "br.createdAt, " +
            "br.content, " +
            "u.nickname, " +
            "u.userImage, " +
            "br.likesCnt) " +
            "FROM BlogReview br " +
            "JOIN br.user u " +
            "WHERE br.blog.blogId = :blogId " +
            "ORDER BY br.likesCnt DESC")
    Page<ReviewDto> findByLikesCntDesc(@Param("blogId") Long blogId, Pageable pageable);

    @Query("SELECT new com.spring.be.blogReview.dto.ReviewDto(" +
            "br.blogReviewId, " +
            "br.rating, " +
            "br.createdAt, " +
            "br.content, " +
            "u.nickname, " +
            "u.userImage, " +
            "br.likesCnt) " +
            "FROM BlogReview br " +
            "JOIN br.user u " +
            "WHERE br.blog.blogId = :blogId " +
            "ORDER BY br.createdAt DESC")
    Page<ReviewDto> findByCreatedAtDesc(@Param("blogId") Long blogId, Pageable pageable);

    @Query("SELECT new com.spring.be.blogReview.dto.ReviewDto(" +
            "br.blogReviewId, " +
            "br.rating, " +
            "br.createdAt, " +
            "br.content, " +
            "u.nickname, " +
            "u.userImage, " +
            "br.likesCnt) " +
            "FROM BlogReview br " +
            "JOIN br.user u " +
            "WHERE br.blog.blogId = :blogId " +
            "ORDER BY br.rating DESC")
    Page<ReviewDto> findByRatingDesc(@Param("blogId") Long blogId, Pageable pageable);

    @Query("SELECT new com.spring.be.blogReview.dto.ReviewDto(" +
            "br.blogReviewId, " +
            "br.rating, " +
            "br.createdAt, " +
            "br.content, " +
            "u.nickname, " +
            "u.userImage, " +
            "br.likesCnt) " +
            "FROM BlogReview br " +
            "JOIN br.user u " +
            "WHERE br.blog.blogId = :blogId " +
            "ORDER BY br.rating ASC")
    Page<ReviewDto> findByRatingAsc(@Param("blogId") Long blogId, Pageable pageable);
}
