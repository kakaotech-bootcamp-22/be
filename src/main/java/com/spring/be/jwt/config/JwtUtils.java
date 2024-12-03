package com.spring.be.jwt.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String JWT_SECRET;  // 토큰 서명에 사용할 비밀키
    @Value("${jwt.expirationMs}")
    private long JWT_EXPIRATION_MS; // 토큰 유효시간 (예: 24시간)

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    // JWT 토큰 생성
    public String generateJwtToken(String username) {
        var claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60 * 30))
                .subject(username)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }

    // JWT 토큰에서 사용자 이름 추출 (NimbusJwtDecoder 사용)
    public String getUsernameFromJwtToken(String token) {
//        return Jwts.parser()
//                .setSigningKey(JWT_SECRET)
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
        return jwtDecoder.decode(token).getSubject();
    }

    // JWT 토큰 유효성 확인 (NimbusJwtDecoder 사용)
    public boolean validateJwtToken(String token) {
//        try {
//            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token);
//            return true;
//        } catch (MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
//            // 예외 처리
//            return false;
//        }
        try {
            jwtDecoder.decode(token);  // JWT 디코딩 및 검증
            return true;
        } catch (Exception e) {
            return false;  // 예외 처리 (잘못된 토큰인 경우)
        }
    }
}
