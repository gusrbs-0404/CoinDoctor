package com.ai.CoinDoctor.repository;

import com.ai.CoinDoctor.entity.TradingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 거래 설정 Repository
 * 단일 책임: 거래 설정 데이터 접근만 담당
 * One Source of Truth: 거래 설정 조회는 이 Repository를 통해서만
 */
@Repository
public interface TradingConfigRepository extends JpaRepository<TradingConfig, Integer> {
    
    /**
     * 가장 최근 거래 설정 조회
     * 하드코딩 금지: 메서드 이름으로 쿼리 자동 생성
     * 
     * @return 가장 최근 생성된 거래 설정
     */
    Optional<TradingConfig> findFirstByOrderByCreatedAtDesc();
    
    /**
     * 활성화된 거래 설정 존재 여부 확인
     * 에러 처리: Optional 반환으로 null 안전성 확보
     * 
     * @return 거래 설정 존재 여부
     */
    boolean existsBySettingIdIsNotNull();
}
