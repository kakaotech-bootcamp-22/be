package com.spring.be.util;

public class CookieUtils {

    // 쿠키 헤더에서 JWT 추출
    public static String extractTokenFromCookie(String cookieHeader, String cookieName) {
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            return null;
        }

        String[] cookies = cookieHeader.split("; ");
        for (String cookie : cookies) {
            if (cookie.startsWith(cookieName + "=")) {
                return cookie.substring((cookieName + "=").length());
            }
        }
        return null;
    }
}
