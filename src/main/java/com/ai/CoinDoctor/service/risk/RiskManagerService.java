package com.ai.CoinDoctor.service.risk;

import com.ai.CoinDoctor.entity.RiskEventLog;
import com.ai.CoinDoctor.entity.SystemStatus;
import com.ai.CoinDoctor.entity.TradeLog;
import com.ai.CoinDoctor.repository.RiskEventLogRepository;
import com.ai.CoinDoctor.repository.SystemStatusRepository;
import com.ai.CoinDoctor.repository.TradeLogRepository;
import com.ai.CoinDoctor.shared.constants.RiskConstants;
import com.ai.CoinDoctor.shared.exceptions.RiskException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 리스크 관리 서비스
 * 단일 책임: 리스크 감지 및 관리만 담당
 * One Source of Truth: 리스크 관리 로직은 이 서비스를 통해서만
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskManagerService {
    
    private final TradeLogRepository tradeLogRepository;
    private final RiskEventLogRepository riskEventLogRepository;
    private final SystemStatusRepository systemStatusRepository;
    
    /**
     * 연속 손실 체크
     * 단일 책임: 연속 손실 감지만 담당
     * 에러 처리: null 체크 및 예외 처리
     * 
     * @param maxConsecutiveLosses 최대 연속 손실 횟수
     * @return 연속 손실 발생 여부
     */
    @Transactional
    public boolean checkConsecutiveLoss(int maxConsecutiveLosses) {
        log.debug("연속 손실 체크: maxConsecutiveLosses={}", maxConsecutiveLosses);
        
        try {
            // One Source of Truth: 거래 로그는 Repository를 통해서만 조회
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            List<TradeLog> recentLossTrades = tradeLogRepository.findLossTradesSince(oneDayAgo);
            
            // 에러 처리: null 체크
            if (recentLossTrades == null || recentLossTrades.isEmpty()) {
                return false;
            }
            
            // 최근 거래부터 연속 손실 카운트
            int consecutiveLossCount = 0;
            for (TradeLog trade : recentLossTrades) {
                if (trade.getProfitLoss() != null && trade.getProfitLoss().compareTo(BigDecimal.ZERO) < 0) {
                    consecutiveLossCount++;
                    if (consecutiveLossCount >= maxConsecutiveLosses) {
                        // 리스크 이벤트 기록
                        recordRiskEvent(
                            RiskEventLog.EventType.CONSECUTIVE_LOSS,
                            String.format("%d연속 손실 발생", consecutiveLossCount)
                        );
                        log.warn("연속 손실 감지: {}회", consecutiveLossCount);
                        return true;
                    }
                } else {
                    break; // 연속이 끊김
                }
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("연속 손실 체크 중 오류 발생", e);
            throw new RiskException("연속 손실 체크 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 서킷브레이커 체크
     * 단일 책임: 급락 감지만 담당
     * 하드코딩 금지: 임계값은 파라미터로 받음
     * 
     * @param priceChangePercent 가격 변동률 (%)
     * @param threshold 서킷브레이커 임계값 (%)
     * @return 서킷브레이커 발동 여부
     */
    @Transactional
    public boolean checkCircuitBreaker(BigDecimal priceChangePercent, BigDecimal threshold) {
        log.debug("서킷브레이커 체크: priceChangePercent={}, threshold={}", 
                  priceChangePercent, threshold);
        
        try {
            // 에러 처리: null 체크
            if (priceChangePercent == null || threshold == null) {
                return false;
            }
            
            // 급락 감지 (음수 비교)
            if (priceChangePercent.compareTo(threshold) <= 0) {
                // 리스크 이벤트 기록
                recordRiskEvent(
                    RiskEventLog.EventType.CIRCUIT_BREAKER,
                    String.format("급락 감지: %.2f%% 하락", priceChangePercent.abs())
                );
                log.warn("서킷브레이커 발동: {}% 하락", priceChangePercent);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("서킷브레이커 체크 중 오류 발생", e);
            throw new RiskException("서킷브레이커 체크 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 일일 손실 한도 체크
     * 단일 책임: 일일 손실 한도 확인만 담당
     * 에러 처리: null 체크
     * 
     * @param maxDailyLoss 최대 일일 손실률 (%)
     * @return 일일 손실 한도 초과 여부
     */
    @Transactional
    public boolean checkDailyLossLimit(BigDecimal maxDailyLoss) {
        log.debug("일일 손실 한도 체크: maxDailyLoss={}", maxDailyLoss);
        
        try {
            // One Source of Truth: 거래 로그는 Repository를 통해서만 조회
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            BigDecimal todayProfitLoss = tradeLogRepository.sumProfitLossSince(todayStart);
            
            // 에러 처리: null 체크
            if (todayProfitLoss == null) {
                todayProfitLoss = BigDecimal.ZERO;
            }
            
            // 손실률 계산 (음수 비교)
            if (todayProfitLoss.compareTo(maxDailyLoss) <= 0) {
                // 리스크 이벤트 기록
                recordRiskEvent(
                    RiskEventLog.EventType.DAILY_LOSS_LIMIT,
                    String.format("일일 손실 한도 초과: %.2f원", todayProfitLoss)
                );
                log.warn("일일 손실 한도 초과: {}원", todayProfitLoss);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("일일 손실 한도 체크 중 오류 발생", e);
            throw new RiskException("일일 손실 한도 체크 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 리스크 이벤트 기록
     * 단일 책임: 리스크 이벤트 기록만 담당
     * 에러 처리: 트랜잭션으로 데이터 일관성 보장
     * 
     * @param eventType 이벤트 타입
     * @param detail 이벤트 상세 내용
     */
    @Transactional
    public void recordRiskEvent(RiskEventLog.EventType eventType, String detail) {
        log.info("리스크 이벤트 기록: eventType={}, detail={}", eventType, detail);
        
        try {
            // 하드코딩 금지: Enum 사용
            RiskEventLog riskEvent = new RiskEventLog();
            riskEvent.setDate(LocalDate.now());
            riskEvent.setEventType(eventType);
            riskEvent.setDetail(detail);
            
            // One Source of Truth: 리스크 이벤트는 Repository를 통해서만 저장
            riskEventLogRepository.save(riskEvent);
            
            log.info("리스크 이벤트 기록 완료: eventType={}", eventType);
            
        } catch (Exception e) {
            log.error("리스크 이벤트 기록 중 오류 발생", e);
            // 리스크 이벤트 기록 실패는 치명적이지 않으므로 예외를 던지지 않음
        }
    }
    
    /**
     * 시스템 상태 업데이트
     * 단일 책임: 시스템 상태 업데이트만 담당
     * 에러 처리: 트랜잭션으로 데이터 일관성 보장
     * 
     * @param status 자동매매 상태
     * @param reason 상태 변경 사유
     * @param cooldownSeconds 쿨다운 시간 (초)
     */
    @Transactional
    public void updateSystemStatus(SystemStatus.TradingStatus status, String reason, int cooldownSeconds) {
        log.info("시스템 상태 업데이트: status={}, reason={}, cooldownSeconds={}", 
                 status, reason, cooldownSeconds);
        
        try {
            // One Source of Truth: 시스템 상태는 Repository를 통해서만 조회/저장
            SystemStatus systemStatus = systemStatusRepository.findFirstByOrderByLastUpdateDesc()
                .orElse(new SystemStatus());
            
            // 하드코딩 금지: Enum 사용
            systemStatus.setAutoTrading(status);
            systemStatus.setStatusReason(reason);
            systemStatus.setCooldownRemainingSeconds(cooldownSeconds);
            
            systemStatusRepository.save(systemStatus);
            
            log.info("시스템 상태 업데이트 완료: status={}", status);
            
        } catch (Exception e) {
            log.error("시스템 상태 업데이트 중 오류 발생", e);
            throw new RiskException("시스템 상태 업데이트 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 최근 리스크 이벤트 조회
     * 단일 책임: 리스크 이벤트 조회만 담당
     * 
     * @param limit 조회할 개수
     * @return 최근 리스크 이벤트 목록
     */
    @Transactional(readOnly = true)
    public List<RiskEventLog> getRecentRiskEvents(int limit) {
        log.debug("최근 리스크 이벤트 조회: limit={}", limit);
        
        // One Source of Truth: 리스크 이벤트는 Repository를 통해서만 조회
        return riskEventLogRepository.findRecentEvents(limit);
    }
    
    /**
     * 현재 시스템 상태 조회
     * 단일 책임: 시스템 상태 조회만 담당
     * 에러 처리: 상태가 없으면 기본 상태 반환
     * 
     * @return 현재 시스템 상태
     */
    @Transactional(readOnly = true)
    public SystemStatus getCurrentSystemStatus() {
        log.debug("현재 시스템 상태 조회");
        
        // One Source of Truth: 시스템 상태는 Repository를 통해서만 조회
        return systemStatusRepository.findFirstByOrderByLastUpdateDesc()
            .orElse(new SystemStatus());
    }
    
    /**
     * 현재 리스크 상태를 Map으로 조회
     * 단일 책임: 리스크 상태를 Map으로 변환만 담당
     * 
     * @return 리스크 상태 Map
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentRiskStatus() {
        log.debug("현재 리스크 상태 Map 조회");
        
        SystemStatus systemStatus = getCurrentSystemStatus();
        int consecutiveLosses = getConsecutiveLossCount();
        boolean isCircuitBreakerTriggered = isCircuitBreakerTriggered();
        boolean isCooldownActive = isCooldownActive();
        
        Map<String, Object> status = new HashMap<>();
        status.put("tradingStatus", systemStatus.getAutoTrading());
        status.put("consecutiveLosses", consecutiveLosses);
        status.put("isCircuitBreakerTriggered", isCircuitBreakerTriggered);
        status.put("isCooldownActive", isCooldownActive);
        status.put("cooldownRemainingSeconds", getCooldownRemainingSeconds());
        status.put("statusReason", systemStatus.getStatusReason());
        status.put("lastUpdate", systemStatus.getLastUpdate());
        
        return status;
    }
    
    /**
     * 연속 손실 횟수 조회
     * 단일 책임: 연속 손실 횟수 계산만 담당
     * 
     * @return 연속 손실 횟수
     */
    @Transactional(readOnly = true)
    public int getConsecutiveLossCount() {
        log.debug("연속 손실 횟수 조회");
        
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<TradeLog> recentLossTrades = tradeLogRepository.findLossTradesSince(oneDayAgo);
        
        if (recentLossTrades == null || recentLossTrades.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (TradeLog trade : recentLossTrades) {
            if (trade.getProfitLoss() != null && trade.getProfitLoss().compareTo(BigDecimal.ZERO) < 0) {
                count++;
            } else {
                break;
            }
        }
        
        return count;
    }
    
    /**
     * 연속 손실 한도 도달 여부 확인
     * 단일 책임: 연속 손실 한도 체크만 담당
     * 
     * @return 연속 손실 한도 도달 여부
     */
    @Transactional(readOnly = true)
    public boolean isConsecutiveLossLimitReached() {
        int consecutiveLosses = getConsecutiveLossCount();
        int maxConsecutiveLosses = RiskConstants.MAX_CONSECUTIVE_LOSSES;
        return consecutiveLosses >= maxConsecutiveLosses;
    }
    
    /**
     * 서킷브레이커 발동 여부 확인
     * 단일 책임: 서킷브레이커 상태 확인만 담당
     * 
     * @return 서킷브레이커 발동 여부
     */
    @Transactional(readOnly = true)
    public boolean isCircuitBreakerTriggered() {
        SystemStatus systemStatus = getCurrentSystemStatus();
        return systemStatus.getAutoTrading() == SystemStatus.TradingStatus.STOPPED &&
               "CIRCUIT_BREAKER".equals(systemStatus.getStatusReason());
    }
    
    /**
     * 쿨다운 활성 여부 확인
     * 단일 책임: 쿨다운 상태 확인만 담당
     * 
     * @return 쿨다운 활성 여부
     */
    @Transactional(readOnly = true)
    public boolean isCooldownActive() {
        SystemStatus systemStatus = getCurrentSystemStatus();
        return systemStatus.getCooldownRemainingSeconds() > 0;
    }
    
    /**
     * 쿨다운 남은 시간 조회
     * 단일 책임: 쿨다운 남은 시간 조회만 담당
     * 
     * @return 쿨다운 남은 시간 (초)
     */
    @Transactional(readOnly = true)
    public long getCooldownRemainingSeconds() {
        SystemStatus systemStatus = getCurrentSystemStatus();
        return systemStatus.getCooldownRemainingSeconds();
    }
    
    /**
     * 거래 금액 검증
     * 단일 책임: 거래 금액 유효성 검증만 담당
     * 
     * @param amount 거래 금액
     * @return 유효성 여부
     */
    public boolean validateTradeAmount(BigDecimal amount) {
        log.debug("거래 금액 검증: amount={}", amount);
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        BigDecimal maxAmount = getMaxTradeAmount();
        return amount.compareTo(maxAmount) <= 0;
    }
    
    /**
     * 최대 거래 금액 조회
     * 단일 책임: 최대 거래 금액 조회만 담당
     * 
     * @return 최대 거래 금액
     */
    public BigDecimal getMaxTradeAmount() {
        // 하드코딩 금지: Constants에서 조회
        return com.ai.CoinDoctor.shared.constants.TradingConstants.MAX_TRADING_AMOUNT;
    }
    
    /**
     * 리스크 이벤트 조회 (필터링)
     * 단일 책임: 리스크 이벤트 조회만 담당
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param eventType 이벤트 타입
     * @return 리스크 이벤트 목록
     */
    @Transactional(readOnly = true)
    public List<RiskEventLog> getRiskEvents(LocalDate startDate, LocalDate endDate, String eventType) {
        log.debug("리스크 이벤트 조회: {} ~ {}, eventType={}", startDate, endDate, eventType);
        
        if (startDate != null && endDate != null && eventType != null) {
            RiskEventLog.EventType type = RiskEventLog.EventType.valueOf(eventType);
            return riskEventLogRepository.findByDateBetweenAndEventType(startDate, endDate, type);
        } else if (startDate != null && endDate != null) {
            return riskEventLogRepository.findByDateBetweenOrderByTriggeredAtDesc(startDate, endDate);
        } else if (eventType != null) {
            RiskEventLog.EventType type = RiskEventLog.EventType.valueOf(eventType);
            return riskEventLogRepository.findByEventTypeOrderByTriggeredAtDesc(type);
        } else {
            return riskEventLogRepository.findAllByOrderByTriggeredAtDesc();
        }
    }
    
    /**
     * 일일 손실 한도 초과 여부 확인
     * 단일 책임: 일일 손실 한도 체크만 담당
     * 
     * @return 일일 손실 한도 초과 여부
     */
    @Transactional(readOnly = true)
    public boolean isDailyLossLimitExceeded() {
        BigDecimal todayLoss = getTodayTotalLoss();
        BigDecimal maxDailyLoss = getMaxDailyLoss();
        return todayLoss.abs().compareTo(maxDailyLoss) > 0;
    }
    
    /**
     * 오늘의 총 손실 조회
     * 단일 책임: 오늘의 총 손실 계산만 담당
     * 
     * @return 오늘의 총 손실
     */
    @Transactional(readOnly = true)
    public BigDecimal getTodayTotalLoss() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        BigDecimal todayProfitLoss = tradeLogRepository.sumProfitLossSince(todayStart);
        return todayProfitLoss != null ? todayProfitLoss : BigDecimal.ZERO;
    }
    
    /**
     * 최대 일일 손실 한도 조회
     * 단일 책임: 최대 일일 손실 한도 조회만 담당
     * 
     * @return 최대 일일 손실 한도
     */
    public BigDecimal getMaxDailyLoss() {
        // 하드코딩 금지: Constants에서 조회
        return BigDecimal.valueOf(RiskConstants.MAX_DAILY_LOSS_PERCENT);
    }
    
    /**
     * 최대 연속 손실 횟수 조회
     * 단일 책임: 최대 연속 손실 횟수 조회만 담당
     * 
     * @return 최대 연속 손실 횟수
     */
    public int getMaxConsecutiveLosses() {
        // 하드코딩 금지: Constants에서 조회
        return RiskConstants.MAX_CONSECUTIVE_LOSSES;
    }
    
    /**
     * 쿨다운 지속 시간 조회
     * 단일 책임: 쿨다운 지속 시간 조회만 담당
     * 
     * @return 쿨다운 지속 시간 (초)
     */
    public long getCooldownDuration() {
        // 하드코딩 금지: Constants에서 조회
        return RiskConstants.COOLDOWN_DURATION_SECONDS;
    }
    
    /**
     * 서킷브레이커 임계값 조회
     * 단일 책임: 서킷브레이커 임계값 조회만 담당
     * 
     * @return 서킷브레이커 임계값 (%)
     */
    public BigDecimal getCircuitBreakerThreshold() {
        // 하드코딩 금지: Constants에서 조회
        return BigDecimal.valueOf(RiskConstants.CIRCUIT_BREAKER_THRESHOLD_PERCENT);
    }
    
    /**
     * 서킷브레이커 수동 해제
     * 단일 책임: 서킷브레이커 해제만 담당
     */
    @Transactional
    public void resetCircuitBreaker() {
        log.info("서킷브레이커 수동 해제");
        
        SystemStatus systemStatus = getCurrentSystemStatus();
        systemStatus.setAutoTrading(SystemStatus.TradingStatus.RUNNING);
        systemStatus.setStatusReason("MANUAL_RESET");
        systemStatus.setCooldownRemainingSeconds(0);
        
        systemStatusRepository.save(systemStatus);
        
        recordRiskEvent(RiskEventLog.EventType.MANUAL_RESET, "서킷브레이커 수동 해제");
    }
    
    /**
     * 쿨다운 타이머 수동 해제
     * 단일 책임: 쿨다운 타이머 해제만 담당
     */
    @Transactional
    public void resetCooldown() {
        log.info("쿨다운 타이머 수동 해제");
        
        SystemStatus systemStatus = getCurrentSystemStatus();
        systemStatus.setCooldownRemainingSeconds(0);
        systemStatus.setStatusReason("COOLDOWN_RESET");
        
        systemStatusRepository.save(systemStatus);
        
        recordRiskEvent(RiskEventLog.EventType.MANUAL_RESET, "쿨다운 타이머 수동 해제");
    }
}
