package com.ai.CoinDoctor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 리스크 이벤트 로그 엔티티
 * One Source of Truth: 모든 리스크 이벤트는 이 테이블에 기록
 * 연속 손실, 서킷브레이커, 일일 손실 한도 등의 이벤트 추적
 */
@Entity
@Table(name = "risk_event_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskEventLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;
    
    /**
     * 이벤트 발생 날짜
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    /**
     * 이벤트 타입
     * 하드코딩 금지: Enum으로 타입 안전성 확보
     */
    @Column(name = "event_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    
    /**
     * 이벤트 상세 내용
     * 예: "3연속 손실 발생", "1분 내 -3.5% 급락 감지"
     */
    @Column(name = "detail", length = 255)
    private String detail;
    
    /**
     * 이벤트 발생 시각
     */
    @Column(name = "triggered_at", nullable = false, updatable = false)
    private LocalDateTime triggeredAt;
    
    /**
     * 엔티티 생성 시 자동으로 발생 시각 설정
     */
    @PrePersist
    protected void onCreate() {
        this.triggeredAt = LocalDateTime.now();
        if (this.date == null) {
            this.date = LocalDate.now();
        }
    }
    
    /**
     * 리스크 이벤트 타입 Enum
     * 하드코딩 금지: Enum으로 타입 안전성 확보
     */
    public enum EventType {
        CONSECUTIVE_LOSS("연속 손실"),
        CIRCUIT_BREAKER("서킷브레이커"),
        DAILY_LOSS_LIMIT("일일 손실 한도"),
        API_ERROR("API 오류"),
        NETWORK_ERROR("네트워크 오류"),
        MANUAL_STOP("수동 중지"),
        SYSTEM_ERROR("시스템 오류");
        
        private final String description;
        
        EventType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
