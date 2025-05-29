package com.devgang.marketduck.config.websocket;

import com.devgang.marketduck.auth.jwt.JwtTokenizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 웹소켓 핸드셰이크 인터셉터
 * 연결 전에 토큰 유효성 검사를 수행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    private final JwtTokenizer jwtTokenizer;

    /**
     * 핸드셰이크 전처리
     */
    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {
        log.debug("WebSocket 연결 시도: {}", request.getRemoteAddress());

        // CORS 헤더 추가
        HttpHeaders headers = response.getHeaders();

        // 클라이언트의 Origin 헤더 가져오기
        String origin = request.getHeaders().getOrigin();
        if (origin != null) {
            headers.add("Access-Control-Allow-Origin", origin);
        } else {
            headers.add("Access-Control-Allow-Origin", "*");
        }

        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        headers.add("Access-Control-Allow-Headers",
                "Origin, X-Requested-With, Content-Type, Accept, Authorization");
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Max-Age", "3600");

        // 인증 토큰 처리
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;

            // 헤더에서 Authorization 토큰 확인
            String token = servletRequest.getServletRequest().getHeader("Authorization");

            // 헤더에 토큰이 없으면 URL 파라미터에서 추출 시도
            if (token == null || !token.startsWith("Bearer ")) {
                String query = servletRequest.getServletRequest().getQueryString();
                if (query != null) {
                    Map<String, String> params = parseQueryString(query);
                    token = params.get("token");
                    if (token.contains("Bearer")) {
                        token = token.substring(7);
                    }
                    log.debug("WebSocket 연결 시도: URL 파라미터에서 토큰 추출 - {}", token);
                }
            } else {
                // Bearer 접두사 제거
                token = token.substring(7);
                log.debug("WebSocket 연결 시도: 헤더에서 토큰 추출 - {}", token);
            }

            if (token == null) {
                log.warn("WebSocket 연결 거부: 토큰이 없음 - {}", request.getRemoteAddress());
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            try {
                // 토큰 유효성 검증
                jwtTokenizer.verifyAccessToken(token);

                // 토큰으로부터 사용자명 추출하여 속성에 저장
                String username = jwtTokenizer.getUsername(token);
                attributes.put("username", username);
                log.debug("WebSocket 연결 인증됨: 사용자 - {}", username);
            } catch (Exception e) {
                log.warn("WebSocket 연결 거부: 유효하지 않은 토큰 - {}", request.getRemoteAddress());
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
        }

        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    /**
     * 핸드셰이크 후처리
     */
    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception ex) {
        if (ex != null) {
            log.error("WebSocket 연결 실패: {}, 에러: {}", request.getRemoteAddress(), ex.getMessage(), ex);
        } else {
            log.debug("WebSocket 연결 성공: {}", request.getRemoteAddress());
        }
        super.afterHandshake(request, response, wsHandler, ex);
    }

    /**
     * 쿼리 문자열을 파싱하여 파라미터 맵으로 변환
     */
    private Map<String, String> parseQueryString(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                try {
                    if (idx > 0) {
                        String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                        String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                        params.put(key, value);
                    }
                } catch (UnsupportedEncodingException e) {
                    log.warn("URL 디코딩 실패", e);
                }
            }
        }
        return params;
    }
}