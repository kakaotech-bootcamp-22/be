package com.spring.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 30)
    private String socialPlatform;

    @Column(nullable = false)
    private Long socialId;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(nullable = true, length = 50)
    private String email;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Column(nullable = true, length = 255)
    private String userImage;

    @Column(nullable = true, length = 500) // Access Token은 길이가 길 수 있으므로 넉넉하게 설정
    private String kakaoAccessToken;


    public User(String socialPlatform, Long socialId, String nickname, String profileImage, String kakaoAccessToken){
        this.socialPlatform = socialPlatform;
        this.socialId = socialId;
        this.nickname = nickname;
        this.userImage = profileImage;
        this.isDeleted = false;
        this.kakaoAccessToken = kakaoAccessToken;

    }

    // Getters and Setters
}
