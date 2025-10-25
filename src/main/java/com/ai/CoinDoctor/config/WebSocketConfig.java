package com.ai.CoinDoctor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정
 * 단일 책임: WebSocket 및 STOMP 설정만 담당
 * One Source of Truth: WebSocket 설정은 이 클래스를 통해서만
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    /**
     * STOMP 엔드포인트 등록
     * 단일 책임: WebSocket 연결 엔드포인트 설정만 담당
     * 
     * @param registry STOMP 엔드포인트 레지스트리
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 하드코딩 금지: 엔드포인트 경로를 명확하게 정의
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*") // 에러 처리: CORS 설정 (개발 환경)
            .withSockJS(); // SockJS fallback 지원
    }
    
    /**
     * 메시지 브로커 설정
     * 단일 책임: 메시지 브로커 설정만 담당
     * 
     * @param registry 메시지 브로커 레지스트리
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 하드코딩 금지: 브로커 경로를 명확하게 정의
        
        // 클라이언트가 구독할 경로 (서버 → 클라이언트)
        registry.enableSimpleBroker(
            "/topic",   // 일반 브로드캐스트
            "/queue"    // 특정 사용자 메시지
        );
        
        // 클라이언트가 메시지를 보낼 경로 (클라이언트 → 서버)
        registry.setApplicationDestinationPrefixes("/app");
    }
}
