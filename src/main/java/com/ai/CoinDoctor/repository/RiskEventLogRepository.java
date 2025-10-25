package com.ai.CoinDoctor.repository;

import com.ai.CoinDoctor.entity.RiskEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 리스크 이벤트 로그 Repository
 * 단일 책임: 리스크 이벤트 로그 데이터 접근만 담당
 * One Source of Truth: 리스크 이벤트 조회는 이 Repository를 통해서만
 */
@Repository
public interface RiskEventLogRepository extends JpaRepository<RiskEventLog, Integer> {
    
    /**
     * 특정 날짜의 리스크 이벤트 조회 (최신순 정렬)
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 
     * @param date 조회할 날짜
     * @return 해당 날짜의 리스크 이벤트 목록
     */
    List<RiskEventLog> findByDateOrderByTriggeredAtDesc(LocalDate date);
    
    /**
     * 날짜 범위로 리스크 이벤트 조회 (최신순 정렬)
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 날짜 범위 내의 리스크 이벤트 목록
     */
    List<RiskEventLog> findByDateBetweenOrderByTriggeredAtDesc(LocalDate startDate, LocalDate endDate);
    
    /**
     * 특정 이벤트 타입의 로그 조회 (최신순 정렬)
     * 하드코딩 금지: Enum 사용으로 타입 안전성 확보
     * 
     * @param eventType 이벤트 타입
     * @return 해당 이벤트 타입의 로그 목록
     */
    List<RiskEventLog> findByEventTypeOrderByTriggeredAtDesc(RiskEventLog.EventType eventType);
    
    /**
     * 특정 일시 이후의 리스크 이벤트 조회 (최신순 정렬)
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 
     * @param triggeredAt 시작 일시
     * @return 해당 일시 이후의 리스크 이벤트 목록
     */
    List<RiskEventLog> findByTriggeredAtAfterOrderByTriggeredAtDesc(LocalDateTime triggeredAt);
    
    /**
     * 특정 일시 이후의 특정 이벤트 타입 개수 조회
     * 에러 처리: Long 반환으로 null 안전성 확보
     * 
     * @param eventType 이벤트 타입
     * @param triggeredAt 시작 일시
     * @return 이벤트 개수
     */
    @Query("SELECT COUNT(r) FROM RiskEventLog r WHERE r.eventType = :eventType AND r.triggeredAt >= :triggeredAt")
    Long countByEventTypeAndTriggeredAtAfter(
        @Param("eventType") RiskEventLog.EventType eventType,
        @Param("triggeredAt") LocalDateTime triggeredAt
    );
    
    /**
     * 최근 N개의 리스크 이벤트 조회
     * 에러 처리: @Query 사용으로 명확한 쿼리 정의
     * 
     * @param limit 조회할 개수
     * @return 최근 N개의 리스크 이벤트
     */
    @Query("SELECT r FROM RiskEventLog r ORDER BY r.triggeredAt DESC LIMIT :limit")
    List<RiskEventLog> findRecentEvents(@Param("limit") int limit);
    
    /**
     * 전체 리스크 이벤트 조회 (최신순 정렬)
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 
     * @return 전체 리스크 이벤트 목록
     */
    List<RiskEventLog> findAllByOrderByTriggeredAtDesc();
    
    /**
     * 날짜 범위 내의 리스크 이벤트 조회
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 날짜 범위 내의 리스크 이벤트 목록
     */
    List<RiskEventLog> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 특정 이벤트 타입의 로그 조회
     * 하드코딩 금지: Enum 사용으로 타입 안전성 확보
     * 
     * @param eventType 이벤트 타입
     * @return 해당 이벤트 타입의 로그 목록
     */
    List<RiskEventLog> findByEventType(RiskEventLog.EventType eventType);
    
    /**
     * 날짜 범위 및 이벤트 타입으로 리스크 이벤트 조회
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param eventType 이벤트 타입
     * @return 조건에 맞는 리스크 이벤트 목록
     */
    List<RiskEventLog> findByDateBetweenAndEventType(LocalDate startDate, LocalDate endDate, RiskEventLog.EventType eventType);
}
