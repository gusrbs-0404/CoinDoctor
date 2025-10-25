package com.ai.CoinDoctor.repository;

import com.ai.CoinDoctor.entity.DailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 일별 통계 Repository
 * 단일 책임: 일별 통계 데이터 접근만 담당
 * One Source of Truth: 일별 통계 조회는 이 Repository를 통해서만
 */
@Repository
public interface DailySummaryRepository extends JpaRepository<DailySummary, Integer> {
    
    /**
     * 특정 날짜의 통계 조회
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 에러 처리: Optional 반환으로 null 안전성 확보
     * 
     * @param date 조회할 날짜
     * @return 해당 날짜의 통계
     */
    Optional<DailySummary> findByDate(LocalDate date);
    
    /**
     * 날짜 범위로 통계 조회 (최신순 정렬)
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 날짜 범위 내의 통계 목록
     */
    List<DailySummary> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);
    
    /**
     * 최근 N일간의 통계 조회
     * 에러 처리: @Query 사용으로 명확한 쿼리 정의
     * 
     * @param days 조회할 일수
     * @return 최근 N일간의 통계 목록
     */
    @Query("SELECT d FROM DailySummary d WHERE d.date >= :startDate ORDER BY d.date DESC")
    List<DailySummary> findRecentDays(@Param("startDate") LocalDate startDate);
    
    /**
     * 전체 통계 조회 (최신순 정렬)
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 
     * @return 전체 통계 목록
     */
    List<DailySummary> findAllByOrderByDateDesc();
    
    /**
     * 특정 날짜 이후의 총 수익 합계
     * 에러 처리: null 반환 가능성 고려
     * 
     * @param startDate 시작 날짜
     * @return 총 수익 합계
     */
    @Query("SELECT SUM(d.totalProfit) FROM DailySummary d WHERE d.date >= :startDate")
    java.math.BigDecimal sumTotalProfitSince(@Param("startDate") LocalDate startDate);
}
