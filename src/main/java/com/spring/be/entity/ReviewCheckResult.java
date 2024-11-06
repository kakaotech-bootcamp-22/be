package com.spring.be.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "review_check_results")
@Getter @Setter
public class ReviewCheckResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resultId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String blogUrl;

    @Column(nullable = false, length = 30)
    private String summaryTitle;

    @Column(nullable = false, length = 250)
    private String summaryText;

    @Column(nullable = false)
    private Integer score;

    @Column(length = 250)
    private String evidence;

    // Getters and Setters
}