package com.spring.be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;

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

    @Column(nullable = false, unique = true)
    private BigInteger socialId;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = true, length = 400)
    private String userImage;

    @Column(nullable = true, length = 500) // Access Token은 길이가 길 수 있으므로 넉넉하게 설정
    private String accessToken;


    public User(String socialPlatform, BigInteger socialId, String nickname, String profileImage, String accessToken, String email){
        this.socialPlatform = socialPlatform;
        this.socialId = socialId;
        this.nickname = nickname;
        this.userImage = profileImage;
        this.accessToken = accessToken;
        this.email = email;
    }
}
