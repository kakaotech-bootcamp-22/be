package com.spring.be.login.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users") // 테이블 이름 설정
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 생성되는 ID
    private Long id;

    @Column(nullable = false, unique = true) // 유니크 제약 조건
    private String username;

    @Column(nullable = false) // 비밀번호 필드
    private String password;
}