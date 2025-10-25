package com.ai.CoinDoctor.repository;

import com.ai.CoinDoctor.entity.SystemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 시스템 상태 Repository
 * 단일 책임: 시스템 상태 데이터 접근만 담당
 * One Source of Truth: 시스템 상태 조회는 이 Repository를 통해서만
 */
@Repository
public interface SystemStatusRepository extends JpaRepository<SystemStatus, Integer> {
    
    /**
     * 가장 최근 시스템 상태 조회
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 에러 처리: Optional 반환으로 null 안전성 확보
     * 
     * @return 가장 최근 시스템 상태
     */
    Optional<SystemStatus> findFirstByOrderByLastUpdateDesc();
    
    /**
     * 특정 자동매매 상태의 시스템 조회
     * 하드코딩 금지: Enum 사용으로 타입 안전성 확보
     * 
     * @param autoTrading 자동매매 상태 (RUNNING, STOPPED)
     * @return 해당 상태의 시스템 목록
     */
    java.util.List<SystemStatus> findByAutoTrading(SystemStatus.TradingStatus autoTrading);
    
    /**
     * 시스템 상태 존재 여부 확인
     * 에러 처리: boolean 반환으로 명확한 결과
     * 
     * @return 시스템 상태 존재 여부
     */
    boolean existsByStatusIdIsNotNull();
}
