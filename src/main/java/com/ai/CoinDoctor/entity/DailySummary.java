package com.ai.CoinDoctor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 일별 통계 엔티티
 * One Source of Truth: 일별 수익률, 승률, 거래횟수 등 모든 통계는 여기서 관리
 */
@Entity
@Table(name = "daily_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailySummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Integer summaryId;
    
    /**
     * 통계 날짜
     * 유니크 제약: 하루에 하나의 통계만 존재
     */
    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;
    
    /**
     * 총 수익/손실 (원)
     * 기본값: 0.00
     */
    @Column(name = "total_profit", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalProfit = BigDecimal.ZERO;
    
    /**
     * 승률 (%)
     * 기본값: 0.00
     */
    @Column(name = "win_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal winRate = BigDecimal.ZERO;
    
    /**
     * 총 거래 횟수
     * 기본값: 0
     */
    @Column(name = "total_trades", nullable = false)
    private Integer totalTrades = 0;
    
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
