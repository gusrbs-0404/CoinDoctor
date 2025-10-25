package com.ai.CoinDoctor.repository;

import com.ai.CoinDoctor.entity.TradeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 거래 로그 Repository
 * 단일 책임: 거래 로그 데이터 접근만 담당
 * One Source of Truth: 거래 로그 조회는 이 Repository를 통해서만
 */
@Repository
public interface TradeLogRepository extends JpaRepository<TradeLog, Integer> {
    
    /**
     * 날짜 범위로 거래 로그 조회 (최신순 정렬)
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 
     * @param start 시작 일시
     * @param end 종료 일시
     * @return 날짜 범위 내의 거래 로그 목록
     */
    List<TradeLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    
    /**
     * 특정 코인의 거래 로그 조회 (최신순 정렬)
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 
     * @param coinId 코인 ID
     * @return 해당 코인의 거래 로그 목록
     */
    List<TradeLog> findByCoinIdOrderByCreatedAtDesc(Integer coinId);
    
    /**
     * 특정 거래 타입의 로그 조회 (최신순 정렬)
     * 하드코딩 금지: Enum 사용으로 타입 안전성 확보
     * 
     * @param tradeType 거래 타입 (BUY, SELL)
     * @return 해당 거래 타입의 로그 목록
     */
    List<TradeLog> findByTradeTypeOrderByCreatedAtDesc(TradeLog.TradeType tradeType);
    
    /**
     * 특정 일시 이후의 거래 횟수 조회
     * 에러 처리: Long 반환으로 null 안전성 확보
     * 
     * @param start 시작 일시
     * @return 거래 횟수
     */
    @Query("SELECT COUNT(t) FROM TradeLog t WHERE t.createdAt >= :start")
    Long countTradesSince(@Param("start") LocalDateTime start);
    
    /**
     * 특정 일시 이후의 총 손익 합계
     * 에러 처리: null 반환 가능성 고려
     * 
     * @param start 시작 일시
     * @return 총 손익 합계
     */
    @Query("SELECT SUM(t.profitLoss) FROM TradeLog t WHERE t.createdAt >= :start AND t.profitLoss IS NOT NULL")
    java.math.BigDecimal sumProfitLossSince(@Param("start") LocalDateTime start);
    
    /**
     * 최근 N개의 거래 로그 조회
     * 에러 처리: @Query 사용으로 명확한 쿼리 정의
     * 
     * @param limit 조회할 개수
     * @return 최근 N개의 거래 로그
     */
    @Query("SELECT t FROM TradeLog t ORDER BY t.createdAt DESC LIMIT :limit")
    List<TradeLog> findRecentTrades(@Param("limit") int limit);
    
    /**
     * 특정 일시 이후의 손실 거래만 조회 (최신순 정렬)
     * 에러 처리: 손익이 음수인 거래만 필터링
     * 
     * @param start 시작 일시
     * @return 손실 거래 목록
     */
    @Query("SELECT t FROM TradeLog t WHERE t.createdAt >= :start AND t.profitLoss < 0 ORDER BY t.createdAt DESC")
    List<TradeLog> findLossTradesSince(@Param("start") LocalDateTime start);
    
    /**
     * 특정 일시 이후의 수익 거래만 조회 (최신순 정렬)
     * 에러 처리: 손익이 양수인 거래만 필터링
     * 
     * @param start 시작 일시
     * @return 수익 거래 목록
     */
    @Query("SELECT t FROM TradeLog t WHERE t.createdAt >= :start AND t.profitLoss > 0 ORDER BY t.createdAt DESC")
    List<TradeLog> findProfitTradesSince(@Param("start") LocalDateTime start);
}
