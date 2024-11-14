package com.spring.be.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "blogs")
@Getter @Setter
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blogId;

    @Column(nullable = false, length = 255)
    private String blogUrl;

    @Column(nullable = false)
    private Integer reviewCount;

    // Getters and Setters
}