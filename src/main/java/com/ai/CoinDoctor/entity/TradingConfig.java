package com.ai.CoinDoctor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 거래 설정 엔티티
 * One Source of Truth: 모든 거래 설정은 이 테이블에서 관리
 */
@Entity
@Table(name = "trade_setting")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradingConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Integer settingId;
    
    /**
     * Take Profit 비율 (%)
     * 기본값: 1.0%
     */
    @Column(name = "tp", nullable = false, precision = 5, scale = 2)
    private BigDecimal tp = BigDecimal.valueOf(1.00);
    
    /**
     * Stop Loss 비율 (%)
     * 기본값: 0.5%
     */
    @Column(name = "sl", nullable = false, precision = 5, scale = 2)
    private BigDecimal sl = BigDecimal.valueOf(0.50);
    
    /**
     * 리스크 비율 (%)
     * 기본값: 0.0%
     */
    @Column(name = "risk", nullable = false, precision = 5, scale = 2)
    private BigDecimal risk = BigDecimal.ZERO;
    
    /**
     * 최대 연속 손실 횟수
     * 기본값: 3회
     */
    @Column(name = "max_loss_count", nullable = false)
    private Integer maxLossCount = 3;
    
    /**
     * 서킷브레이커 임계값 (%)
     * 기본값: -3.0%
     */
    @Column(name = "circuit_breaker", nullable = false, precision = 5, scale = 2)
    private BigDecimal circuitBreaker = BigDecimal.valueOf(3.00);
    
    /**
     * 거래당 금액 (원)
     * 기본값: 10,000원
     */
    @Column(name = "amount_per_trade", nullable = false)
    private Integer amountPerTrade = 10000;
    
    /**
     * 암호화된 API Key
     * 보안: AES256 암호화 저장
     */
    @Column(name = "api_key_encrypted", length = 255)
    private String apiKeyEncrypted;
    
    /**
     * 생성 일시
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 수정 일시
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 엔티티 생성 시 자동으로 생성 일시 설정
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 엔티티 수정 시 자동으로 수정 일시 갱신
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
