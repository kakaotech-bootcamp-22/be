package com.spring.be.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "result_feedback")
@Getter @Setter
public class ResultFeedback extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @ManyToOne
    @JoinColumn(name = "result_id", nullable = false)
    private ReviewCheckResult reviewCheckResult;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Boolean type;

    @Column(length = 200)
    private String reason;
}
