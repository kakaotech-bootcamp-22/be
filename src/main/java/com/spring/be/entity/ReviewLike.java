package com.spring.be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "review_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"blog_review_id", "user_id"})
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class ReviewLike extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewLikeId;

    @ManyToOne
    @JoinColumn(name = "blog_review_id", nullable = false)
    private BlogReview blogReview;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
