package com.ai.CoinDoctor.service.statistics;

import com.ai.CoinDoctor.entity.DailySummary;
import com.ai.CoinDoctor.entity.TradeLog;
import com.ai.CoinDoctor.repository.DailySummaryRepository;
import com.ai.CoinDoctor.repository.TradeLogRepository;
import com.ai.CoinDoctor.shared.exceptions.TradingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 통계 서비스
 * 단일 책임: 거래 통계 계산 및 관리만 담당
 * One Source of Truth: 통계 계산 로직은 이 서비스를 통해서만
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    
    private final DailySummaryRepository dailySummaryRepository;
    private final TradeLogRepository tradeLogRepository;
    
    /**
     * 일별 통계 계산 및 저장
     * 단일 책임: 일별 통계 계산만 담당
     * 에러 처리: 트랜잭션으로 데이터 일관성 보장
     * 
     * @param date 통계를 계산할 날짜
     * @return 계산된 일별 통계
     */
    @Transactional
    public DailySummary calculateDailySummary(LocalDate date) {
        log.info("일별 통계 계산 시작: date={}", date);
        
        try {
            // One Source of Truth: 거래 로그는 Repository를 통해서만 조회
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            List<TradeLog> dailyTrades = tradeLogRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(startOfDay, endOfDay);
            
            // 통계 계산
            int totalTrades = dailyTrades.size();
            BigDecimal totalProfit = calculateTotalProfit(dailyTrades);
            BigDecimal winRate = calculateWinRate(dailyTrades);
            
            // One Source of Truth: 일별 통계는 Repository를 통해서만 조회/저장
            DailySummary summary = dailySummaryRepository.findByDate(date)
                .orElse(new DailySummary());
            
            summary.setDate(date);
            summary.setTotalTrades(totalTrades);
            summary.setTotalProfit(totalProfit);
            summary.setWinRate(winRate);
            
            DailySummary savedSummary = dailySummaryRepository.save(summary);
            
            log.info("일별 통계 계산 완료: date={}, totalTrades={}, totalProfit={}, winRate={}",
                     date, totalTrades, totalProfit, winRate);
            
            return savedSummary;
            
        } catch (Exception e) {
            log.error("일별 통계 계산 중 오류 발생: date={}", date, e);
            throw new TradingException("일별 통계 계산 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 총 수익 계산
     * 단일 책임: 총 수익 계산만 담당
     * 에러 처리: null 체크
     * 
     * @param trades 거래 목록
     * @return 총 수익
     */
    private BigDecimal calculateTotalProfit(List<TradeLog> trades) {
        // 에러 처리: null 체크
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return trades.stream()
            .map(TradeLog::getProfitLoss)
            .filter(profitLoss -> profitLoss != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 승률 계산
     * 단일 책임: 승률 계산만 담당
     * 에러 처리: 0으로 나누기 방지
     * 
     * @param trades 거래 목록
     * @return 승률 (%)
     */
    private BigDecimal calculateWinRate(List<TradeLog> trades) {
        // 에러 처리: null 체크 및 0으로 나누기 방지
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        long winningTrades = trades.stream()
            .filter(trade -> trade.getProfitLoss() != null)
            .filter(trade -> trade.getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
            .count();
        
        long totalTradesWithProfitLoss = trades.stream()
            .filter(trade -> trade.getProfitLoss() != null)
            .count();
        
        // 에러 처리: 0으로 나누기 방지
        if (totalTradesWithProfitLoss == 0) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(winningTrades)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(totalTradesWithProfitLoss), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * 특정 날짜의 통계 조회
     * 단일 책임: 통계 조회만 담당
     * 에러 처리: 통계가 없으면 예외 발생
     * 
     * @param date 조회할 날짜
     * @return 일별 통계
     * @throws TradingException 통계가 없을 경우
     */
    @Transactional(readOnly = true)
    public DailySummary getDailySummary(LocalDate date) {
        log.debug("일별 통계 조회: date={}", date);
        
        // One Source of Truth: 일별 통계는 Repository를 통해서만 조회
        return dailySummaryRepository.findByDate(date)
            .orElseThrow(() -> new TradingException(
                String.format("%s의 통계가 존재하지 않습니다.", date)));
    }
    
    /**
     * 날짜 범위의 통계 조회
     * 단일 책임: 통계 조회만 담당
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 날짜 범위 내의 통계 목록
     */
    @Transactional(readOnly = true)
    public List<DailySummary> getDailySummaries(LocalDate startDate, LocalDate endDate) {
        log.debug("날짜 범위 통계 조회: startDate={}, endDate={}", startDate, endDate);
        
        // One Source of Truth: 일별 통계는 Repository를 통해서만 조회
        return dailySummaryRepository.findByDateBetweenOrderByDateDesc(startDate, endDate);
    }
    
    /**
     * 최근 N일간의 통계 조회
     * 단일 책임: 통계 조회만 담당
     * 
     * @param days 조회할 일수
     * @return 최근 N일간의 통계 목록
     */
    @Transactional(readOnly = true)
    public List<DailySummary> getRecentDailySummaries(int days) {
        log.debug("최근 N일 통계 조회: days={}", days);
        
        LocalDate startDate = LocalDate.now().minusDays(days);
        
        // One Source of Truth: 일별 통계는 Repository를 통해서만 조회
        return dailySummaryRepository.findRecentDays(startDate);
    }
    
    /**
     * 전체 통계 조회
     * 단일 책임: 통계 조회만 담당
     * 
     * @return 전체 통계 목록
     */
    @Transactional(readOnly = true)
    public List<DailySummary> getAllDailySummaries() {
        log.debug("전체 통계 조회");
        
        // One Source of Truth: 일별 통계는 Repository를 통해서만 조회
        return dailySummaryRepository.findAllByOrderByDateDesc();
    }
    
    /**
     * 특정 기간의 총 수익 조회
     * 단일 책임: 총 수익 조회만 담당
     * 에러 처리: null 체크
     * 
     * @param startDate 시작 날짜
     * @return 총 수익 (null일 경우 0 반환)
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalProfitSince(LocalDate startDate) {
        log.debug("총 수익 조회: startDate={}", startDate);
        
        // One Source of Truth: 총 수익은 Repository를 통해서만 조회
        BigDecimal totalProfit = dailySummaryRepository.sumTotalProfitSince(startDate);
        
        // 에러 처리: null 체크
        return totalProfit != null ? totalProfit : BigDecimal.ZERO;
    }
    
    /**
     * 오늘의 통계 조회 (없으면 계산)
     * 단일 책임: 오늘 통계 조회 또는 계산만 담당
     * 
     * @return 오늘의 통계
     */
    @Transactional
    public DailySummary getTodaySummary() {
        log.debug("오늘의 통계 조회");
        
        LocalDate today = LocalDate.now();
        
        // One Source of Truth: 일별 통계는 Repository를 통해서만 조회
        return dailySummaryRepository.findByDate(today)
            .orElseGet(() -> calculateDailySummary(today));
    }
}
