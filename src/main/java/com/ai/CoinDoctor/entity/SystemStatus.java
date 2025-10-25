package com.ai.CoinDoctor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 시스템 상태 엔티티
 * One Source of Truth: 자동매매 시스템의 현재 상태는 이 테이블에서 관리
 */
@Entity
@Table(name = "system_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatus {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Integer statusId;
    
    /**
     * 자동매매 상태 (RUNNING, STOPPED)
     * 하드코딩 금지: Enum으로 타입 안전성 확보
     */
    @Column(name = "auto_trading", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TradingStatus autoTrading = TradingStatus.STOPPED;
    
    /**
     * 마지막 업데이트 시각
     */
    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;
    
    /**
     * 상태 변경 사유
     * 예: "연속 3회 손실", "서킷브레이커 발동", "수동 중지"
     */
    @Column(name = "status_reason", length = 100)
    private String statusReason;
    
    /**
     * 쿨다운 남은 시간 (초)
     * 기본값: 0
     */
    @Column(name = "cooldown_remaining_seconds", nullable = false)
    private Integer cooldownRemainingSeconds = 0;
    
    /**
     * 엔티티 생성 시 자동으로 마지막 업데이트 시각 설정
     */
    @PrePersist
    protected void onCreate() {
        this.lastUpdate = LocalDateTime.now();
    }
    
    /**
     * 엔티티 수정 시 자동으로 마지막 업데이트 시각 갱신
     */
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }
    
    /**
     * 자동매매 상태 Enum
     * 하드코딩 금지: Enum으로 타입 안전성 확보
     */
    public enum TradingStatus {
        RUNNING("실행 중"),
        STOPPED("중지됨");
        
        private final String description;
        
        TradingStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
