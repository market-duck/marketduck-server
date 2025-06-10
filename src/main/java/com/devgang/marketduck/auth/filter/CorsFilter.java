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
 * HTTP 요청에 대한 CORS 설정을 중앙에서 관리합니다.
 * WebSocket 핸드셰이크는 AuthHandshakeInterceptor에서 처리합니다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CorsFilter extends OncePerRequestFilter {

    // 허용된 Origin 목록 - 외부 설정으로 분리할 수도 있음
    public static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://marketduck.goghdev.xyz",
            "https://marketduck.goghdev.xyz"
    // 추가 Origin은 여기에 추가
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 실제 WebSocket 핸드셰이크 요청만 건너뜀
        // SockJS의 일반 HTTP 요청(/info, /iframe 등)은 여기서 CORS 처리
        boolean isWebSocketHandshake = requestURI != null &&
                (requestURI.matches("/ws-chat/\\d+/[^/]+/websocket") ||
                        requestURI.matches("/ws-chat/websocket"));

        if (isWebSocketHandshake) {
            log.debug("WebSocket 핸드셰이크 요청 감지, CORS 필터 처리 건너뜀: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String originUrl = request.getHeader("Origin");
        log.info("요청된 Origin: {}, URI: {}", originUrl, requestURI);
        log.info("허용된 Origin 목록: {}", ALLOWED_ORIGINS);

        String allowedOrigin = null;
        if (originUrl != null) {
            allowedOrigin = ALLOWED_ORIGINS.stream()
                    .filter(o -> o.equals(originUrl))
                    .findFirst()
                    .orElse(null);
        }

        // Origin이 허용 목록에 있거나 null인 경우 처리
        if (allowedOrigin != null) {
            response.setHeader("Access-Control-Allow-Origin", allowedOrigin);
            log.info("CORS 허용된 Origin 설정: {}", allowedOrigin);
        } else if (originUrl == null) {
            // Origin 헤더가 없는 경우 (직접 접근 등)
            response.setHeader("Access-Control-Allow-Origin", "*");
            log.info("Origin 헤더가 없음. 모든 Origin 허용");
        } else {
            // 허용되지 않은 Origin도 일시적으로 허용 (개발/테스트용)
            log.warn("허용되지 않은 Origin 요청, 임시 허용: {}", originUrl);
            response.setHeader("Access-Control-Allow-Origin", originUrl);
        }

        // CORS 헤더 설정
        setCorsHeaders(response);

        // OPTIONS 요청(preflight) 처리
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.info("OPTIONS 요청 처리: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    /**
     * CORS 헤더 설정
     * 
     * @param response HTTP 응답
     */
    public static void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Expose-Headers",
                "Authorization, userId, userStatus, Content-Disposition");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers",
                "Origin, X-Requested-With, Content-Type, Accept, Key, Authorization, userId, userStatus, Content-Disposition, Timeout");
    }
}