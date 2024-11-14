package com.spring.be.jwt.controller;

import com.spring.be.jwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.sql.SQLOutput;
import java.util.Map;

@RestController
@RequestMapping("/auth/kakao")

public class KakaoAuthController {

    private final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final UserService userService;

    @Autowired
    public KakaoAuthController(UserService userService) {
        this.userService = userService;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/token")
    public ResponseEntity<String> handleKakaoToken(@RequestBody Map<String, String> request) {
        String authorizationCode = request.get("code"); // 클라이언트에서 넘어온 인가 코드
        System.out.println("Received Kakao authorization code(인가코드): " + authorizationCode); // 인가 코드 확인

        // 액세스 토큰을 요청할 URL 구성
        String clientId = "826a723547312cf55037f1bf217f293b"; // 카카오 앱의 클라이언트 ID
        String redirectUri = "http://localhost:3000/"; // 리다이렉트 URI

        String tokenUrl = KAKAO_TOKEN_URL +
                "?grant_type=authorization_code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&code=" + authorizationCode;

        // 액세스 토큰 요청
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Map.class);
        String kakaoAccessToken = (String) response.getBody().get("access_token");

        // 액세스 토큰 로그 출력 (디버깅)
        System.out.println("Received Kakao access token(액세스토큰): " + kakaoAccessToken);

        // 카카오 사용자 정보 API 호출
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.set("Authorization", "Bearer " + kakaoAccessToken);
        HttpEntity<String> userInfoEntity = new HttpEntity<>(userInfoHeaders);
        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(KAKAO_USER_INFO_URL, HttpMethod.GET, userInfoEntity, Map.class);

        // 사용자 정보 가져오기
        Map<String, Object> userInfo = userInfoResponse.getBody();
        Long kakaoUserId = (Long) userInfo.get("id"); // 카카오 사용자 ID
        Map<String, Object> properties = (Map<String, Object>) userInfo.get("properties");
        String nickname = (String) properties.get("nickname"); // 사용자 닉네임
        String profileImage = (String) properties.get("profile_image"); // 프로필 이미지
        String platform = "kakao";

        // 로그 출력
        System.out.println("Kakao User ID:" + kakaoUserId);
        System.out.println("Nickname:" + nickname);
        System.out.println("Profile Image URL:" + profileImage);

        // 소셜로그인 플랫폼, 소셜 계정 아이디, 닉네임, 이메일??
        userService.saveUser(platform, kakaoUserId, nickname, profileImage);

        return ResponseEntity.ok("카카오 토큰 처리 완료"+ kakaoUserId+","+ nickname+ ","+profileImage);
        //return ResponseEntity.ok("카카오 토큰 처리 완료");
    }
}
