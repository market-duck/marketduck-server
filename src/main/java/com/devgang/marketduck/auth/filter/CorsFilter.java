package com.devgang.marketduck.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 애플리케이션 전체의 CORS 정책을 관리하는 필터
 * HTTP 요청 및 WebSocket 연결 모두에 적용되는 CORS 설정을 중앙에서 관리합니다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CorsFilter extends OncePerRequestFilter {

    // 허용된 Origin 목록 - 외부 설정으로 분리할 수도 있음
    private static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:3000"
    // 추가 Origin은 여기에 추가
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String originUrl = request.getHeader("Origin");

        // Origin 검증 및 설정
        String origin = validateOrigin(originUrl);

        // CORS 헤더 설정
        setCorsHeaders(response, origin);

        // OPTIONS 요청(preflight) 처리
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Origin 검증 및 처리
     * 
     * @param originUrl 요청의 Origin 헤더 값
     * @return 허용된 Origin 또는 요청의 Origin
     */
    public String validateOrigin(String originUrl) {
        // 허용된 Origin 목록에서 확인
        return ALLOWED_ORIGINS.stream()
                .filter(o -> o.equals(originUrl))
                .findFirst()
                .orElse(originUrl);
    }

    /**
     * CORS 헤더 설정
     * 
     * @param response HTTP 응답
     * @param origin   설정할 Origin 값
     */
    public static void setCorsHeaders(HttpServletResponse response, String origin) {
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, PATCH, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Expose-Headers", "Authorization, userId, userStatus, Content-Disposition");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers",
                "Origin, X-Requested-With, Content-Type, Accept, Key, Authorization, userId, userStatus, Content-Disposition, Timeout");
    }
}