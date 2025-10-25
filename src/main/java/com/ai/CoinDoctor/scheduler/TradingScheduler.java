package com.ai.CoinDoctor.scheduler;

import com.ai.CoinDoctor.entity.SystemStatus;
import com.ai.CoinDoctor.entity.TradeLog;
import com.ai.CoinDoctor.repository.SystemStatusRepository;
import com.ai.CoinDoctor.repository.TradeLogRepository;
import com.ai.CoinDoctor.service.market.UpbitApiService;
import com.ai.CoinDoctor.service.risk.RiskManagerService;
import com.ai.CoinDoctor.shared.constants.TradingConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * 자동매매 스케줄러
 * 단일 책임: 주기적인 자동매매 실행만 담당
 * One Source of Truth: 자동매매 스케줄링은 이 클래스를 통해서만
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TradingScheduler {
    
    private final UpbitApiService upbitApiService;
    private final RiskManagerService riskManagerService;
    private final SystemStatusRepository systemStatusRepository;
    private final TradeLogRepository tradeLogRepository;
    
    // 하드코딩 금지: application.properties에서 설정값 주입
    @Value("${trading.auto-trading.enabled:false}")
    private boolean autoTradingEnabled;
    
    @Value("${trading.auto-trading.amount-per-trade:10000}")
    private long amountPerTrade;
    
    @Value("${trading.auto-trading.tp-percent:1.0}")
    private double tpPercent;
    
    @Value("${trading.auto-trading.sl-percent:0.5}")
    private double slPercent;
    
    /**
     * 자동매매 메인 스케줄러
     * 단일 책임: 5초마다 자동매매 실행만 담당
     * 에러 처리: try-catch로 예외 처리하여 스케줄러 중단 방지
     * 
     * fixedDelay: 이전 작업 완료 후 5초 대기
     * initialDelay: 애플리케이션 시작 후 10초 대기
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void executeAutoTrading() {
        // 에러 처리: 전체를 try-catch로 감싸서 스케줄러 중단 방지
        try {
            // 1. 자동매매 활성화 여부 확인
            if (!isAutoTradingActive()) {
                log.debug("자동매매가 비활성화 상태입니다.");
                return;
            }
            
            // 2. 리스크 체크
            if (!checkRiskStatus()) {
                log.warn("리스크 체크 실패: 자동매매를 건너뜁니다.");
                return;
            }
            
            // 3. 거래대금 상위 5종목 조회
            List<Map<String, Object>> top5Markets = upbitApiService.getTop5ByVolume();
            if (top5Markets == null || top5Markets.isEmpty()) {
                log.warn("상위 5종목 조회 실패");
                return;
            }
            
            log.info("=== 자동매매 스캔 시작: {} 종목 ===", top5Markets.size());
            
            // 4. 각 종목별로 매매 신호 분석 및 실행
            for (Map<String, Object> market : top5Markets) {
                try {
                    String marketCode = (String) market.get("market");
                    analyzeAndTrade(marketCode);
                } catch (Exception e) {
                    log.error("종목 분석 중 오류 발생: {}", market.get("market"), e);
                    // 에러 처리: 한 종목 실패해도 다음 종목 계속 진행
                }
            }
            
            log.info("=== 자동매매 스캔 완료 ===");
            
        } catch (Exception e) {
            log.error("자동매매 스케줄러 실행 중 예상치 못한 오류 발생", e);
            // 에러 처리: 스케줄러는 계속 실행되어야 하므로 예외를 던지지 않음
        }
    }
    
    /**
     * 자동매매 활성화 여부 확인
     * 단일 책임: 자동매매 활성화 상태 확인만 담당
     * 
     * @return 자동매매 활성화 여부
     */
    private boolean isAutoTradingActive() {
        // 하드코딩 금지: 설정값 사용
        if (!autoTradingEnabled) {
            return false;
        }
        
        // One Source of Truth: 시스템 상태는 Repository를 통해서만 조회
        SystemStatus systemStatus = systemStatusRepository.findFirstByOrderByLastUpdateDesc()
            .orElse(null);
        
        if (systemStatus == null) {
            return false;
        }
        
        return systemStatus.getAutoTrading() == SystemStatus.TradingStatus.RUNNING;
    }
    
    /**
     * 리스크 상태 체크
     * 단일 책임: 리스크 상태 확인만 담당
     * 에러 처리: 각 리스크 항목별로 체크
     * 
     * @return 거래 가능 여부
     */
    private boolean checkRiskStatus() {
        try {
            // 1. 쿨다운 체크
            if (riskManagerService.isCooldownActive()) {
                long remainingSeconds = riskManagerService.getCooldownRemainingSeconds();
                log.warn("쿨다운 활성 중: {}초 남음", remainingSeconds);
                return false;
            }
            
            // 2. 서킷브레이커 체크
            if (riskManagerService.isCircuitBreakerTriggered()) {
                log.warn("서킷브레이커 발동 중");
                return false;
            }
            
            // 3. 연속 손실 체크
            if (riskManagerService.isConsecutiveLossLimitReached()) {
                log.warn("연속 손실 한도 도달");
                return false;
            }
            
            // 4. 일일 손실 한도 체크
            if (riskManagerService.isDailyLossLimitExceeded()) {
                log.warn("일일 손실 한도 초과");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("리스크 상태 체크 중 오류 발생", e);
            // 에러 처리: 리스크 체크 실패 시 안전하게 거래 중단
            return false;
        }
    }
    
    /**
     * 종목 분석 및 거래 실행
     * 단일 책임: 특정 종목의 매매 신호 분석 및 거래 실행만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param marketCode 마켓 코드 (예: KRW-BTC)
     */
    private void analyzeAndTrade(String marketCode) {
        log.debug("종목 분석 시작: {}", marketCode);
        
        try {
            // 1. 1분봉 데이터 조회 (최근 20개)
            List<Map<String, Object>> candles = upbitApiService.getMinuteCandles(marketCode, 20);
            if (candles == null || candles.size() < 20) {
                log.debug("캔들 데이터 부족: {} ({}개)", marketCode, 
                    candles != null ? candles.size() : 0);
                return;
            }
            
            // 2. 매매 신호 분석
            TradingSignal signal = analyzeTradingSignal(candles);
            
            // 3. 매수 신호 확인
            if (signal.isBuySignal()) {
                log.info("매수 신호 감지: {} (신뢰도: {})", marketCode, signal.getConfidence());
                executeBuy(marketCode, signal);
            } else {
                log.debug("매수 신호 없음: {}", marketCode);
            }
            
            // 4. 보유 포지션 확인 및 매도 신호 체크
            checkAndExecuteSell(marketCode, candles);
            
        } catch (Exception e) {
            log.error("종목 분석 중 오류 발생: {}", marketCode, e);
        }
    }
    
    /**
     * 매매 신호 분석
     * 단일 책임: 캔들 데이터로부터 매매 신호 분석만 담당
     * 
     * @param candles 캔들 데이터 목록
     * @return 매매 신호
     */
    private TradingSignal analyzeTradingSignal(List<Map<String, Object>> candles) {
        // 하드코딩 금지: Constants에서 설정값 조회
        int emaShortPeriod = TradingConstants.EMA_SHORT_PERIOD;
        int emaLongPeriod = TradingConstants.EMA_LONG_PERIOD;
        int rsiPeriod = TradingConstants.RSI_PERIOD;
        
        // 1. EMA 계산
        BigDecimal emaShort = calculateEMA(candles, emaShortPeriod);
        BigDecimal emaLong = calculateEMA(candles, emaLongPeriod);
        
        // 2. RSI 계산
        BigDecimal rsi = calculateRSI(candles, rsiPeriod);
        
        // 3. 거래량 분석
        boolean volumeIncreasing = isVolumeIncreasing(candles);
        
        // 4. 매수 신호 판단
        boolean emaCrossover = emaShort.compareTo(emaLong) > 0;
        boolean rsiOversold = rsi.compareTo(BigDecimal.valueOf(TradingConstants.RSI_OVERSOLD)) < 0;
        
        // 신뢰도 계산
        int confidence = 0;
        if (emaCrossover) confidence += 40;
        if (rsiOversold) confidence += 30;
        if (volumeIncreasing) confidence += 30;
        
        boolean buySignal = emaCrossover && volumeIncreasing && confidence >= 60;
        
        return new TradingSignal(buySignal, confidence, emaShort, emaLong, rsi);
    }
    
    /**
     * EMA (지수이동평균) 계산
     * 단일 책임: EMA 계산만 담당
     * 
     * @param candles 캔들 데이터 목록
     * @param period EMA 기간
     * @return EMA 값
     */
    private BigDecimal calculateEMA(List<Map<String, Object>> candles, int period) {
        // 에러 처리: 데이터 부족 시 0 반환
        if (candles == null || candles.size() < period) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));
        BigDecimal ema = BigDecimal.ZERO;
        
        // 초기 SMA 계산
        for (int i = 0; i < period; i++) {
            Object tradePriceObj = candles.get(i).get("trade_price");
            BigDecimal price = convertToBigDecimal(tradePriceObj);
            ema = ema.add(price);
        }
        ema = ema.divide(BigDecimal.valueOf(period), 2, RoundingMode.HALF_UP);
        
        // EMA 계산
        for (int i = period; i < candles.size(); i++) {
            Object tradePriceObj = candles.get(i).get("trade_price");
            BigDecimal price = convertToBigDecimal(tradePriceObj);
            ema = price.subtract(ema).multiply(multiplier).add(ema);
        }
        
        return ema;
    }
    
    /**
     * RSI (상대강도지수) 계산
     * 단일 책임: RSI 계산만 담당
     * 
     * @param candles 캔들 데이터 목록
     * @param period RSI 기간
     * @return RSI 값
     */
    private BigDecimal calculateRSI(List<Map<String, Object>> candles, int period) {
        // 에러 처리: 데이터 부족 시 50 반환 (중립)
        if (candles == null || candles.size() < period + 1) {
            return BigDecimal.valueOf(50);
        }
        
        BigDecimal avgGain = BigDecimal.ZERO;
        BigDecimal avgLoss = BigDecimal.ZERO;
        
        // 초기 평균 계산
        for (int i = 1; i <= period; i++) {
            Object currentPriceObj = candles.get(i).get("trade_price");
            Object prevPriceObj = candles.get(i - 1).get("trade_price");
            
            BigDecimal currentPrice = convertToBigDecimal(currentPriceObj);
            BigDecimal prevPrice = convertToBigDecimal(prevPriceObj);
            BigDecimal change = currentPrice.subtract(prevPrice);
            
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                avgGain = avgGain.add(change);
            } else {
                avgLoss = avgLoss.add(change.abs());
            }
        }
        
        avgGain = avgGain.divide(BigDecimal.valueOf(period), 2, RoundingMode.HALF_UP);
        avgLoss = avgLoss.divide(BigDecimal.valueOf(period), 2, RoundingMode.HALF_UP);
        
        // 에러 처리: 0으로 나누기 방지
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        
        BigDecimal rs = avgGain.divide(avgLoss, 2, RoundingMode.HALF_UP);
        BigDecimal rsi = BigDecimal.valueOf(100)
            .subtract(BigDecimal.valueOf(100).divide(
                BigDecimal.ONE.add(rs), 2, RoundingMode.HALF_UP));
        
        return rsi;
    }
    
    /**
     * 거래량 증가 여부 확인
     * 단일 책임: 거래량 증가 여부 판단만 담당
     * 
     * @param candles 캔들 데이터 목록
     * @return 거래량 증가 여부
     */
    private boolean isVolumeIncreasing(List<Map<String, Object>> candles) {
        // 에러 처리: 데이터 부족 시 false 반환
        if (candles == null || candles.size() < 5) {
            return false;
        }
        
        // 최근 5개 캔들의 평균 거래량
        BigDecimal recentVolume = BigDecimal.ZERO;
        for (int i = 0; i < 5; i++) {
            Object volumeObj = candles.get(i).get("candle_acc_trade_volume");
            BigDecimal volume = convertToBigDecimal(volumeObj);
            recentVolume = recentVolume.add(volume);
        }
        recentVolume = recentVolume.divide(BigDecimal.valueOf(5), 2, RoundingMode.HALF_UP);
        
        // 이전 5개 캔들의 평균 거래량
        BigDecimal prevVolume = BigDecimal.ZERO;
        for (int i = 5; i < 10 && i < candles.size(); i++) {
            Object volumeObj = candles.get(i).get("candle_acc_trade_volume");
            BigDecimal volume = convertToBigDecimal(volumeObj);
            prevVolume = prevVolume.add(volume);
        }
        prevVolume = prevVolume.divide(BigDecimal.valueOf(5), 2, RoundingMode.HALF_UP);
        
        // 거래량이 20% 이상 증가했는지 확인
        return recentVolume.compareTo(prevVolume.multiply(BigDecimal.valueOf(1.2))) > 0;
    }
    
    /**
     * 매수 실행
     * 단일 책임: 매수 주문 실행만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param marketCode 마켓 코드
     * @param signal 매매 신호
     */
    private void executeBuy(String marketCode, TradingSignal signal) {
        try {
            // 하드코딩 금지: 설정값 사용
            BigDecimal amount = BigDecimal.valueOf(amountPerTrade);
            
            // 거래 금액 검증
            if (!riskManagerService.validateTradeAmount(amount)) {
                log.warn("거래 금액 검증 실패: {} (최대: {})", 
                    amount, riskManagerService.getMaxTradeAmount());
                return;
            }
            
            // 시장가 매수 주문
            Map<String, Object> orderResult = upbitApiService.placeMarketBuyOrder(marketCode, amount);
            
            if (orderResult != null && "done".equals(orderResult.get("state"))) {
                log.info("매수 체결 성공: {} (금액: {}원)", marketCode, amount);
                
                // 거래 로그 저장
                saveTradeLog(marketCode, "BUY", amount, signal);
            } else {
                log.warn("매수 체결 실패: {}", marketCode);
            }
            
        } catch (Exception e) {
            log.error("매수 실행 중 오류 발생: {}", marketCode, e);
        }
    }
    
    /**
     * 보유 포지션 확인 및 매도 실행
     * 단일 책임: 보유 포지션의 TP/SL 체크 및 매도 실행만 담당
     * 
     * @param marketCode 마켓 코드
     * @param candles 캔들 데이터 (향후 TP/SL 계산에 사용)
     */
    @SuppressWarnings("unused")
    private void checkAndExecuteSell(String marketCode, List<Map<String, Object>> candles) {
        // TODO: 보유 포지션 조회 로직 구현
        // 현재는 TradeLog에서 최근 매수 내역 조회
        // 실제로는 업비트 API의 잔고 조회를 사용해야 함
        log.debug("매도 신호 체크: {}", marketCode);
    }
    
    /**
     * 거래 로그 저장
     * 단일 책임: 거래 로그 저장만 담당
     * 
     * @param marketCode 마켓 코드
     * @param tradeType 거래 타입 (BUY/SELL)
     * @param amount 거래 금액
     * @param signal 매매 신호 (현재 미사용, 향후 확장용)
     */
    @SuppressWarnings("unused")
    private void saveTradeLog(String marketCode, String tradeType, 
                             BigDecimal amount, TradingSignal signal) {
        try {
            TradeLog tradeLog = new TradeLog();
            // coinId는 Integer이므로 임시로 0 설정 (향후 코인 ID 매핑 테이블 필요)
            tradeLog.setCoinId(0);
            // TradeType은 Enum이므로 변환
            tradeLog.setTradeType("BUY".equals(tradeType) ? 
                TradeLog.TradeType.BUY : TradeLog.TradeType.SELL);
            tradeLog.setPrice(amount);
            tradeLog.setQuantity(BigDecimal.ONE); // 임시값
            tradeLog.setProfitLoss(BigDecimal.ZERO); // 매수 시점에는 0
            
            tradeLogRepository.save(tradeLog);
            log.debug("거래 로그 저장 완료: {} {} (coinId: {})", tradeType, marketCode, 0);
            
        } catch (Exception e) {
            log.error("거래 로그 저장 중 오류 발생", e);
        }
    }
    
    /**
     * Object를 BigDecimal로 변환
     * 단일 책임: 타입 변환만 담당
     * 에러 처리: 변환 실패 시 0 반환
     * 
     * @param obj 변환할 객체
     * @return BigDecimal 값
     */
    private BigDecimal convertToBigDecimal(Object obj) {
        if (obj == null) {
            return BigDecimal.ZERO;
        }
        
        try {
            if (obj instanceof BigDecimal) {
                return (BigDecimal) obj;
            } else if (obj instanceof Number) {
                return BigDecimal.valueOf(((Number) obj).doubleValue());
            } else {
                return new BigDecimal(obj.toString());
            }
        } catch (Exception e) {
            log.warn("BigDecimal 변환 실패: {}", obj, e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 매매 신호 내부 클래스
     * 단일 책임: 매매 신호 데이터 보관만 담당
     */
    private static class TradingSignal {
        private final boolean buySignal;
        private final int confidence;
        private final BigDecimal emaShort;
        private final BigDecimal emaLong;
        private final BigDecimal rsi;
        
        public TradingSignal(boolean buySignal, int confidence, 
                           BigDecimal emaShort, BigDecimal emaLong, BigDecimal rsi) {
            this.buySignal = buySignal;
            this.confidence = confidence;
            this.emaShort = emaShort;
            this.emaLong = emaLong;
            this.rsi = rsi;
        }
        
        public boolean isBuySignal() {
            return buySignal;
        }
        
        public int getConfidence() {
            return confidence;
        }
        
        public BigDecimal getEmaShort() {
            return emaShort;
        }
        
        public BigDecimal getEmaLong() {
            return emaLong;
        }
        
        public BigDecimal getRsi() {
            return rsi;
        }
    }
}
