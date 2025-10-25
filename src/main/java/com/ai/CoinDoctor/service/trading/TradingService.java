package com.ai.CoinDoctor.service.trading;

import com.ai.CoinDoctor.entity.TradeLog;
import com.ai.CoinDoctor.entity.TradingConfig;
import com.ai.CoinDoctor.repository.TradeLogRepository;
import com.ai.CoinDoctor.repository.TradingConfigRepository;
import com.ai.CoinDoctor.shared.constants.TradingConstants;
import com.ai.CoinDoctor.shared.exceptions.TradingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 자동매매 서비스
 * 단일 책임: 자동매매 실행 및 관리만 담당
 * One Source of Truth: 자동매매 로직은 이 서비스를 통해서만
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradingService {
    
    private final TradingConfigRepository tradingConfigRepository;
    private final TradeLogRepository tradeLogRepository;
    
    // 하드코딩 금지: 상태는 volatile 변수로 관리
    private volatile boolean isTradingEnabled = false;
    
    /**
     * 자동매매 시작
     * 단일 책임: 자동매매 시작만 담당
     * 에러 처리: 설정이 없으면 예외 발생
     * 
     * @throws TradingException 거래 설정이 없을 경우
     */
    public void startTrading() {
        log.info("자동매매 시작 요청");
        
        // One Source of Truth: 거래 설정은 Repository를 통해서만 조회
        TradingConfig config = tradingConfigRepository.findFirstByOrderByCreatedAtDesc()
            .orElseThrow(() -> new TradingException("거래 설정이 존재하지 않습니다."));
        
        this.isTradingEnabled = true;
        log.info("자동매매가 시작되었습니다. 거래금액: {}원", config.getAmountPerTrade());
    }
    
    /**
     * 자동매매 중지
     * 단일 책임: 자동매매 중지만 담당
     */
    public void stopTrading() {
        log.info("자동매매 중지 요청");
        this.isTradingEnabled = false;
        log.info("자동매매가 중지되었습니다.");
    }
    
    /**
     * 자동매매 상태 조회
     * 단일 책임: 상태 조회만 담당
     * 
     * @return 자동매매 활성화 여부
     */
    public boolean isTradingEnabled() {
        return this.isTradingEnabled;
    }
    
    /**
     * 매수 주문 실행
     * 단일 책임: 매수 주문 실행만 담당
     * 에러 처리: 트랜잭션으로 데이터 일관성 보장
     * 
     * @param coinId 코인 ID
     * @param price 매수 가격
     * @param quantity 매수 수량
     * @return 생성된 거래 로그
     * @throws TradingException 매수 실패 시
     */
    @Transactional
    public TradeLog executeBuyOrder(Integer coinId, BigDecimal price, BigDecimal quantity) {
        log.info("매수 주문 실행: coinId={}, price={}, quantity={}", coinId, price, quantity);
        
        try {
            // 하드코딩 금지: Enum 사용
            TradeLog tradeLog = new TradeLog();
            tradeLog.setCoinId(coinId);
            tradeLog.setTradeType(TradeLog.TradeType.BUY);
            tradeLog.setPrice(price);
            tradeLog.setQuantity(quantity);
            tradeLog.setProfitLoss(null); // 매수 시에는 손익 없음
            
            // One Source of Truth: 거래 로그는 Repository를 통해서만 저장
            TradeLog savedLog = tradeLogRepository.save(tradeLog);
            
            log.info("매수 주문 완료: tradeId={}", savedLog.getTradeId());
            return savedLog;
            
        } catch (Exception e) {
            log.error("매수 주문 실패: coinId={}", coinId, e);
            throw new TradingException("매수 주문 실행 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 매도 주문 실행
     * 단일 책임: 매도 주문 실행만 담당
     * 에러 처리: 트랜잭션으로 데이터 일관성 보장
     * 
     * @param coinId 코인 ID
     * @param price 매도 가격
     * @param quantity 매도 수량
     * @param profitLoss 손익
     * @return 생성된 거래 로그
     * @throws TradingException 매도 실패 시
     */
    @Transactional
    public TradeLog executeSellOrder(Integer coinId, BigDecimal price, BigDecimal quantity, BigDecimal profitLoss) {
        log.info("매도 주문 실행: coinId={}, price={}, quantity={}, profitLoss={}", 
                 coinId, price, quantity, profitLoss);
        
        try {
            // 하드코딩 금지: Enum 사용
            TradeLog tradeLog = new TradeLog();
            tradeLog.setCoinId(coinId);
            tradeLog.setTradeType(TradeLog.TradeType.SELL);
            tradeLog.setPrice(price);
            tradeLog.setQuantity(quantity);
            tradeLog.setProfitLoss(profitLoss);
            
            // One Source of Truth: 거래 로그는 Repository를 통해서만 저장
            TradeLog savedLog = tradeLogRepository.save(tradeLog);
            
            log.info("매도 주문 완료: tradeId={}, profitLoss={}", savedLog.getTradeId(), profitLoss);
            return savedLog;
            
        } catch (Exception e) {
            log.error("매도 주문 실패: coinId={}", coinId, e);
            throw new TradingException("매도 주문 실행 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 최근 거래 내역 조회
     * 단일 책임: 거래 내역 조회만 담당
     * 
     * @param limit 조회할 개수
     * @return 최근 거래 내역 목록
     */
    @Transactional(readOnly = true)
    public List<TradeLog> getRecentTrades(int limit) {
        log.debug("최근 거래 내역 조회: limit={}", limit);
        
        // One Source of Truth: 거래 로그는 Repository를 통해서만 조회
        return tradeLogRepository.findRecentTrades(limit);
    }
    
    /**
     * 특정 기간의 총 손익 조회
     * 단일 책임: 손익 조회만 담당
     * 에러 처리: null 체크
     * 
     * @param start 시작 일시
     * @return 총 손익 (null일 경우 0 반환)
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalProfitLoss(LocalDateTime start) {
        log.debug("총 손익 조회: start={}", start);
        
        // One Source of Truth: 손익 계산은 Repository를 통해서만
        BigDecimal totalProfitLoss = tradeLogRepository.sumProfitLossSince(start);
        
        // 에러 처리: null 체크
        return totalProfitLoss != null ? totalProfitLoss : BigDecimal.ZERO;
    }
    
    /**
     * 거래 설정 조회
     * 단일 책임: 설정 조회만 담당
     * 에러 처리: 설정이 없으면 예외 발생
     * 
     * @return 거래 설정
     * @throws TradingException 설정이 없을 경우
     */
    @Transactional(readOnly = true)
    public TradingConfig getTradingConfig() {
        log.debug("거래 설정 조회");
        
        // One Source of Truth: 거래 설정은 Repository를 통해서만 조회
        return tradingConfigRepository.findFirstByOrderByCreatedAtDesc()
            .orElseThrow(() -> new TradingException("거래 설정이 존재하지 않습니다."));
    }
    
    /**
     * 거래 설정 업데이트
     * 단일 책임: 설정 업데이트만 담당
     * 에러 처리: 트랜잭션으로 데이터 일관성 보장
     * 
     * @param config 업데이트할 거래 설정
     * @return 업데이트된 거래 설정
     * @throws TradingException 업데이트 실패 시
     */
    @Transactional
    public TradingConfig updateTradingConfig(TradingConfig config) {
        log.info("거래 설정 업데이트: {}", config);
        
        try {
            // One Source of Truth: 거래 설정은 Repository를 통해서만 저장
            TradingConfig savedConfig = tradingConfigRepository.save(config);
            
            log.info("거래 설정 업데이트 완료: settingId={}", savedConfig.getSettingId());
            return savedConfig;
            
        } catch (Exception e) {
            log.error("거래 설정 업데이트 실패", e);
            throw new TradingException("거래 설정 업데이트 중 오류가 발생했습니다.", e);
        }
    }
}
