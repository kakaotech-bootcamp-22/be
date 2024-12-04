package com.spring.be.blogReview.repository;

import com.spring.be.blogReview.dto.ReviewDto;
import com.spring.be.entity.BlogReview;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlogReviewRepository extends JpaRepository<BlogReview, Long> {

    List<BlogReview> findByBlogBlogId(Long blogId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BlogReview> findByBlogReviewId(Long reviewId);

    @Modifying
    @Query("UPDATE BlogReview br SET br.likesCnt = br.likesCnt + 1 WHERE br.blogReviewId = :reviewId")
    void incrementLikes(@Param("reviewId") Long reviewId);

    @Modifying
    @Query("UPDATE BlogReview b SET b.likesCnt = b.likesCnt - 1 WHERE b.blogReviewId = :reviewId")
    void decrementLikes(@Param("reviewId") Long reviewId);

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
            "WHERE br.blog.blogId = :blogId")
    Page<ReviewDto> findByBlogId(@Param("blogId") Long blogId, Pageable pageable);
}
