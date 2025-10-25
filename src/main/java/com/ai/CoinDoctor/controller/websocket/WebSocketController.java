package com.ai.CoinDoctor.controller.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket 컨트롤러
 * 단일 책임: WebSocket 메시지 처리만 담당
 * One Source of Truth: WebSocket 메시지는 이 컨트롤러를 통해서만
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * 클라이언트로부터 받은 메시지 처리
     * 단일 책임: 클라이언트 메시지 수신 및 응답만 담당
     * 
     * @param message 클라이언트 메시지
     * @return 응답 메시지
     */
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Map<String, Object> greeting(Map<String, Object> message) {
        log.debug("WebSocket 메시지 수신: {}", message);
        
        // 하드코딩 금지: 응답 데이터를 Map으로 구성
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello, " + message.get("name"));
        response.put("timestamp", LocalDateTime.now());
        
        return response;
    }
    
    /**
     * 실시간 시세 브로드캐스트
     * 단일 책임: 시세 데이터 브로드캐스트만 담당
     * 
     * @param marketData 시세 데이터
     */
    public void broadcastMarketData(Map<String, Object> marketData) {
        try {
            // One Source of Truth: 메시지 전송은 messagingTemplate을 통해서만
            messagingTemplate.convertAndSend("/topic/market", marketData);
            log.debug("시세 데이터 브로드캐스트: {}", marketData.get("market"));
        } catch (Exception e) {
            log.error("시세 데이터 브로드캐스트 중 오류 발생", e);
        }
    }
    
    /**
     * 거래 알림 브로드캐스트
     * 단일 책임: 거래 알림 브로드캐스트만 담당
     * 
     * @param tradeData 거래 데이터
     */
    public void broadcastTradeNotification(Map<String, Object> tradeData) {
        try {
            // One Source of Truth: 메시지 전송은 messagingTemplate을 통해서만
            messagingTemplate.convertAndSend("/topic/trading", tradeData);
            log.debug("거래 알림 브로드캐스트: {}", tradeData.get("type"));
        } catch (Exception e) {
            log.error("거래 알림 브로드캐스트 중 오류 발생", e);
        }
    }
    
    /**
     * 리스크 이벤트 알림 브로드캐스트
     * 단일 책임: 리스크 이벤트 알림 브로드캐스트만 담당
     * 
     * @param riskEvent 리스크 이벤트 데이터
     */
    public void broadcastRiskEvent(Map<String, Object> riskEvent) {
        try {
            // One Source of Truth: 메시지 전송은 messagingTemplate을 통해서만
            messagingTemplate.convertAndSend("/topic/risk", riskEvent);
            log.info("리스크 이벤트 브로드캐스트: {}", riskEvent.get("eventType"));
        } catch (Exception e) {
            log.error("리스크 이벤트 브로드캐스트 중 오류 발생", e);
        }
    }
    
    /**
     * 통계 업데이트 알림 브로드캐스트
     * 단일 책임: 통계 업데이트 알림 브로드캐스트만 담당
     * 
     * @param statistics 통계 데이터
     */
    public void broadcastStatisticsUpdate(Map<String, Object> statistics) {
        try {
            // One Source of Truth: 메시지 전송은 messagingTemplate을 통해서만
            messagingTemplate.convertAndSend("/topic/statistics", statistics);
            log.debug("통계 업데이트 브로드캐스트");
        } catch (Exception e) {
            log.error("통계 업데이트 브로드캐스트 중 오류 발생", e);
        }
    }
    
    /**
     * 시스템 상태 알림 브로드캐스트
     * 단일 책임: 시스템 상태 알림 브로드캐스트만 담당
     * 
     * @param systemStatus 시스템 상태 데이터
     */
    public void broadcastSystemStatus(Map<String, Object> systemStatus) {
        try {
            // One Source of Truth: 메시지 전송은 messagingTemplate을 통해서만
            messagingTemplate.convertAndSend("/topic/system", systemStatus);
            log.debug("시스템 상태 브로드캐스트: {}", systemStatus.get("status"));
        } catch (Exception e) {
            log.error("시스템 상태 브로드캐스트 중 오류 발생", e);
        }
    }
    
    /**
     * 특정 사용자에게 메시지 전송
     * 단일 책임: 특정 사용자 메시지 전송만 담당
     * 
     * @param userId 사용자 ID
     * @param message 메시지 데이터
     */
    public void sendToUser(String userId, Map<String, Object> message) {
        try {
            // One Source of Truth: 메시지 전송은 messagingTemplate을 통해서만
            messagingTemplate.convertAndSendToUser(
                userId, 
                "/queue/notifications", 
                message
            );
            log.debug("사용자 메시지 전송: userId={}", userId);
        } catch (Exception e) {
            log.error("사용자 메시지 전송 중 오류 발생: userId={}", userId, e);
        }
    }
}
