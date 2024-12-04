package com.spring.be.blogReview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {
    private Long blogReviewId;
    private int rating;
    private LocalDateTime date;
    private String content;
    private String author;
    private String profileImage;
    private int likes;
    private boolean isLiked;

    public ReviewDto(Long blogReviewId, int rating, LocalDateTime createdAt, String content,
                     String nickname, String userImage, int likesCnt) {
        this.blogReviewId = blogReviewId;
        this.rating = rating;
        this.date = createdAt;
        this.content = content;
        this.author = nickname;
        this.profileImage = userImage;
        this.likes = likesCnt;
    }
}