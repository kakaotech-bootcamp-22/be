package com.spring.be.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "blogs")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Blog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blogId;

    @Column(nullable = false, length = 255)
    private String blogUrl;

    @Column(nullable = false)
    private Integer reviewCount;
}