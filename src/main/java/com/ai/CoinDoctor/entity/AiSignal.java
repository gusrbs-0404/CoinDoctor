package com.ai.CoinDoctor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 신호 엔티티
 * One Source of Truth: AI 매매 신호는 이 테이블에 기록
 */
@Entity
@Table(name = "ai_signal")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiSignal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "signal_id")
    private Integer signalId;
    
    /**
     * 코인 ID (외래키)
     * 추후 Coin 엔티티와 연결 가능
     */
    @Column(name = "coin_id")
    private Integer coinId;
    
    /**
     * 신호 타입 (BUY, SELL, HOLD)
     * 하드코딩 금지: Enum으로 타입 안전성 확보
     */
    @Column(name = "signal_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private SignalType signalType;
    
    /**
     * 신호 신뢰도 (0.00 ~ 1.00)
     * 예: 0.85 = 85% 신뢰도
     */
    @Column(name = "confidence", nullable = false, precision = 3, scale = 2)
    private BigDecimal confidence;
    
    /**
     * 생성 일시 (신호 발생 시각)
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 엔티티 생성 시 자동으로 생성 일시 설정
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 신호 타입 Enum
     * 하드코딩 금지: Enum으로 타입 안전성 확보
     */
    public enum SignalType {
        BUY("매수 신호"),
        SELL("매도 신호"),
        HOLD("보유 신호");
        
        private final String description;
        
        SignalType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
