package com.ai.CoinDoctor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 거래 로그 엔티티
 * One Source of Truth: 모든 거래 내역은 이 테이블에 기록
 * 이 데이터를 기반으로 통계, 수익률, 리스크 분석 수행
 */
@Entity
@Table(name = "trade_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Integer tradeId;
    
    /**
     * 코인 ID (외래키)
     * 추후 Coin 엔티티와 연결 가능
     */
    @Column(name = "coin_id")
    private Integer coinId;
    
    /**
     * 일별 통계 ID (외래키)
     * DailySummary와 연결
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summary_id", referencedColumnName = "summary_id")
    private DailySummary dailySummary;
    
    /**
     * 거래 타입 (BUY, SELL)
     * 하드코딩 금지: TradingConstants 사용
     */
    @Column(name = "trade_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private TradeType tradeType;
    
    /**
     * 체결 가격 (원)
     */
    @Column(name = "price", nullable = false, precision = 20, scale = 8)
    private BigDecimal price;
    
    /**
     * 체결 수량 (코인 개수)
     */
    @Column(name = "quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;
    
    /**
     * 손익 (원)
     * null 가능: 매수 시에는 null, 매도 시에만 계산
     */
    @Column(name = "profit_loss", precision = 20, scale = 8)
    private BigDecimal profitLoss;
    
    /**
     * 생성 일시 (거래 발생 시각)
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
     * 거래 타입 Enum
     * 하드코딩 금지: Enum으로 타입 안전성 확보
     */
    public enum TradeType {
        BUY("매수"),
        SELL("매도");
        
        private final String description;
        
        TradeType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
