package com.ai.CoinDoctor.repository;

import com.ai.CoinDoctor.entity.AiSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AI 신호 Repository
 * 단일 책임: AI 신호 데이터 접근만 담당
 * One Source of Truth: AI 신호 조회는 이 Repository를 통해서만
 */
@Repository
public interface AiSignalRepository extends JpaRepository<AiSignal, Integer> {
    
    /**
     * 특정 코인의 가장 최근 신호 조회
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 에러 처리: Optional 반환으로 null 안전성 확보
     * 
     * @param coinId 코인 ID
     * @return 가장 최근 신호
     */
    Optional<AiSignal> findFirstByCoinIdOrderByCreatedAtDesc(Integer coinId);
    
    /**
     * 특정 코인의 모든 신호 조회 (최신순 정렬)
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 
     * @param coinId 코인 ID
     * @return 해당 코인의 신호 목록
     */
    List<AiSignal> findByCoinIdOrderByCreatedAtDesc(Integer coinId);
    
    /**
     * 특정 신호 타입의 신호 조회 (최신순 정렬)
     * 하드코딩 금지: Enum 사용으로 타입 안전성 확보
     * 
     * @param signalType 신호 타입 (BUY, SELL, HOLD)
     * @return 해당 신호 타입의 목록
     */
    List<AiSignal> findBySignalTypeOrderByCreatedAtDesc(AiSignal.SignalType signalType);
    
    /**
     * 날짜 범위로 신호 조회 (최신순 정렬)
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 
     * @param start 시작 일시
     * @param end 종료 일시
     * @return 날짜 범위 내의 신호 목록
     */
    List<AiSignal> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    
    /**
     * 특정 신뢰도 이상의 신호 조회
     * 에러 처리: @Query 사용으로 명확한 쿼리 정의
     * 
     * @param minConfidence 최소 신뢰도
     * @return 신뢰도 이상의 신호 목록
     */
    @Query("SELECT a FROM AiSignal a WHERE a.confidence >= :minConfidence ORDER BY a.createdAt DESC")
    List<AiSignal> findByConfidenceGreaterThanEqual(@Param("minConfidence") java.math.BigDecimal minConfidence);
    
    /**
     * 특정 일시 이후의 신호 개수 조회
     * 에러 처리: Long 반환으로 null 안전성 확보
     * 
     * @param start 시작 일시
     * @return 신호 개수
     */
    @Query("SELECT COUNT(a) FROM AiSignal a WHERE a.createdAt >= :start")
    Long countSignalsSince(@Param("start") LocalDateTime start);
}
