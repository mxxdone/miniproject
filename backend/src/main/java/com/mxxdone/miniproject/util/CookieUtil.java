package com.mxxdone.miniproject.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CookieUtil {

    /**
     * 쿠키명으로 쿠키 찾기
     * @param request HttpServletRequest 객체
     * @param name 찾고자 하는 쿠키의 이름
     * @return 해당 이름의 쿠키 객체 (Optional)
     */
    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return Optional.of(cookie);
            }
        }

        return Optional.empty();
    }

    /**
     * Token을 담은 HTTP Only 쿠키를 생성하여 HttpServletResponse에 추가
     * @param response ttpServletResponse 객체
     * @param name 쿠키 이름 (예: "refreshToken")
     * @param value 쿠키 값 (리프레시 토큰 문자열)
     * @param maxAgeSeconds 쿠키 유효 기간 (초 단위)
     */
    public void addCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)       // JavaScript에서 접근 불가
                .secure(true)         // HTTPS 환경에서만 전송 (개발: false, 운영: true 필수)
                .path("/")            // 쿠키 적용 경로
                .maxAge(maxAgeSeconds)// 쿠키 유효 기간 설정
                .sameSite("Lax")      // CSRF 공격 방지 설정 (필요에 따라 Strict 또는 None으로 변경 가능)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 특정 이름의 쿠키를 삭제 (Max-Age를 0으로 설정하여 만료시킴)
     * @param request HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @param name 삭제할 쿠키의 이름
     */
    public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                //Max-Age를 0으로 설정 -> 만료
                ResponseCookie deletedCookie = ResponseCookie.from(name, "")
                        .path("/")
                        .maxAge(0)
                        .secure(true)
                        .build();
                response.addHeader("Set-Cookie", deletedCookie.toString());
                break; // 해당 쿠키 발견 -> 반복 중단
            }
        }
    }
}
