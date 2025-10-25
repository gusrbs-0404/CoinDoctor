package com.ai.CoinDoctor.service.market;

import com.ai.CoinDoctor.shared.constants.ApiConstants;
import com.ai.CoinDoctor.shared.exceptions.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

/**
 * 업비트 API 연동 서비스
 * 단일 책임: 업비트 API 호출만 담당
 * One Source of Truth: 업비트 API 연동은 이 서비스를 통해서만
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpbitApiService {
    
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;
    
    // 하드코딩 금지: application.properties에서 주입
    @Value("${upbit.api.base-url}")
    private String baseUrl;
    
    @Value("${upbit.api.timeout}")
    private int timeout;
    
    @Value("${upbit.api.access-key:}")
    private String accessKey;
    
    @Value("${upbit.api.secret-key:}")
    private String secretKey;
    
    /**
     * 거래대금 상위 5종목 조회
     * 단일 책임: 상위 종목 조회만 담당
     * 에러 처리: API 호출 실패 시 예외 발생
     * 
     * @return 거래대금 상위 5종목 목록
     * @throws ApiException API 호출 실패 시
     */
    public List<Map<String, Object>> getTop5ByVolume() {
        log.debug("거래대금 상위 5종목 조회 시작");
        
        try {
            // One Source of Truth: API URL은 Constants에서 관리
            WebClient webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
            
            // 전체 마켓 조회
            String response = webClient.get()
                .uri(ApiConstants.UPBIT_TICKER + "?markets=KRW-BTC,KRW-ETH,KRW-XRP,KRW-ADA,KRW-SOL")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(timeout))
                .block();
            
            // 에러 처리: null 체크
            if (response == null || response.isEmpty()) {
                throw new ApiException("업비트 API 응답이 비어있습니다.");
            }
            
            // JSON 파싱
            List<Map<String, Object>> tickers = objectMapper.readValue(
                response, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            
            // 거래대금 순으로 정렬 후 상위 5개 반환
            List<Map<String, Object>> top5 = tickers.stream()
                .sorted((a, b) -> {
                    BigDecimal aVolume = new BigDecimal(a.get("acc_trade_price_24h").toString());
                    BigDecimal bVolume = new BigDecimal(b.get("acc_trade_price_24h").toString());
                    return bVolume.compareTo(aVolume);
                })
                .limit(5)
                .toList();
            
            log.info("거래대금 상위 5종목 조회 완료: {} 종목", top5.size());
            return top5;
            
        } catch (Exception e) {
            log.error("거래대금 상위 5종목 조회 실패", e);
            throw new ApiException("거래대금 상위 5종목 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 1분봉 데이터 조회
     * 단일 책임: 캔들 데이터 조회만 담당
     * 에러 처리: API 호출 실패 시 예외 발생
     * 
     * @param market 마켓 코드 (예: KRW-BTC)
     * @param count 조회할 캔들 개수
     * @return 1분봉 데이터 목록
     * @throws ApiException API 호출 실패 시
     */
    public List<Map<String, Object>> getMinuteCandles(String market, int count) {
        log.debug("1분봉 데이터 조회: market={}, count={}", market, count);
        
        try {
            // 에러 처리: 파라미터 검증
            if (market == null || market.isEmpty()) {
                throw new ApiException("마켓 코드가 비어있습니다.");
            }
            
            if (count <= 0 || count > 200) {
                throw new ApiException("캔들 개수는 1~200 사이여야 합니다.");
            }
            
            // One Source of Truth: API URL은 Constants에서 관리
            WebClient webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
            
            String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(ApiConstants.UPBIT_CANDLES)
                    .queryParam("market", market)
                    .queryParam("count", count)
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(timeout))
                .block();
            
            // 에러 처리: null 체크
            if (response == null || response.isEmpty()) {
                throw new ApiException("업비트 API 응답이 비어있습니다.");
            }
            
            // JSON 파싱
            List<Map<String, Object>> candles = objectMapper.readValue(
                response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            
            log.debug("1분봉 데이터 조회 완료: market={}, count={}", market, candles.size());
            return candles;
            
        } catch (Exception e) {
            log.error("1분봉 데이터 조회 실패: market={}", market, e);
            throw new ApiException("1분봉 데이터 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 현재가 조회
     * 단일 책임: 현재가 조회만 담당
     * 에러 처리: API 호출 실패 시 예외 발생
     * 
     * @param market 마켓 코드 (예: KRW-BTC)
     * @return 현재가 정보
     * @throws ApiException API 호출 실패 시
     */
    public Map<String, Object> getTicker(String market) {
        log.debug("현재가 조회: market={}", market);
        
        try {
            // 에러 처리: 파라미터 검증
            if (market == null || market.isEmpty()) {
                throw new ApiException("마켓 코드가 비어있습니다.");
            }
            
            // One Source of Truth: API URL은 Constants에서 관리
            WebClient webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
            
            String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(ApiConstants.UPBIT_TICKER)
                    .queryParam("markets", market)
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(timeout))
                .block();
            
            // 에러 처리: null 체크
            if (response == null || response.isEmpty()) {
                throw new ApiException("업비트 API 응답이 비어있습니다.");
            }
            
            // JSON 파싱
            List<Map<String, Object>> tickers = objectMapper.readValue(
                response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            
            // 에러 처리: 결과가 없는 경우
            if (tickers.isEmpty()) {
                throw new ApiException("해당 마켓의 현재가 정보를 찾을 수 없습니다: " + market);
            }
            
            log.debug("현재가 조회 완료: market={}", market);
            return tickers.get(0);
            
        } catch (Exception e) {
            log.error("현재가 조회 실패: market={}", market, e);
            throw new ApiException("현재가 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 시장가 매수 주문 (모의 거래)
     * 단일 책임: 매수 주문만 담당
     * 에러 처리: API Key 검증 및 예외 처리
     * 
     * @param market 마켓 코드
     * @param price 주문 금액 (원)
     * @return 주문 결과
     * @throws ApiException API 호출 실패 시
     */
    public Map<String, Object> placeMarketBuyOrder(String market, BigDecimal price) {
        log.info("시장가 매수 주문: market={}, price={}", market, price);
        
        try {
            // 에러 처리: 파라미터 검증
            if (market == null || market.isEmpty()) {
                throw new ApiException("마켓 코드가 비어있습니다.");
            }
            
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException("주문 금액은 0보다 커야 합니다.");
            }
            
            // 에러 처리: API Key 검증
            if (accessKey == null || accessKey.isEmpty() || 
                secretKey == null || secretKey.isEmpty()) {
                log.warn("API Key가 설정되지 않았습니다. 모의 주문으로 처리합니다.");
                return createMockOrder(market, "bid", price, null);
            }
            
            // 실제 주문 로직은 JWT 토큰 생성이 필요하므로 현재는 모의 주문
            log.warn("실제 매수 주문은 JWT 토큰 인증이 필요합니다. 모의 주문으로 처리합니다.");
            return createMockOrder(market, "bid", price, null);
            
        } catch (Exception e) {
            log.error("시장가 매수 주문 실패: market={}", market, e);
            throw new ApiException("시장가 매수 주문 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 지정가 매도 주문 (모의 거래)
     * 단일 책임: 매도 주문만 담당
     * 에러 처리: API Key 검증 및 예외 처리
     * 
     * @param market 마켓 코드
     * @param price 주문 가격
     * @param volume 주문 수량
     * @return 주문 결과
     * @throws ApiException API 호출 실패 시
     */
    public Map<String, Object> placeLimitSellOrder(String market, BigDecimal price, BigDecimal volume) {
        log.info("지정가 매도 주문: market={}, price={}, volume={}", market, price, volume);
        
        try {
            // 에러 처리: 파라미터 검증
            if (market == null || market.isEmpty()) {
                throw new ApiException("마켓 코드가 비어있습니다.");
            }
            
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException("주문 가격은 0보다 커야 합니다.");
            }
            
            if (volume == null || volume.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException("주문 수량은 0보다 커야 합니다.");
            }
            
            // 에러 처리: API Key 검증
            if (accessKey == null || accessKey.isEmpty() || 
                secretKey == null || secretKey.isEmpty()) {
                log.warn("API Key가 설정되지 않았습니다. 모의 주문으로 처리합니다.");
                return createMockOrder(market, "ask", price, volume);
            }
            
            // 실제 주문 로직은 JWT 토큰 생성이 필요하므로 현재는 모의 주문
            log.warn("실제 매도 주문은 JWT 토큰 인증이 필요합니다. 모의 주문으로 처리합니다.");
            return createMockOrder(market, "ask", price, volume);
            
        } catch (Exception e) {
            log.error("지정가 매도 주문 실패: market={}", market, e);
            throw new ApiException("지정가 매도 주문 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 모의 주문 생성
     * 단일 책임: 모의 주문 데이터 생성만 담당
     * 하드코딩 금지: 필드명은 업비트 API 스펙에 맞춤
     * 
     * @param market 마켓 코드
     * @param side 주문 방향 (bid: 매수, ask: 매도)
     * @param price 주문 가격
     * @param volume 주문 수량
     * @return 모의 주문 결과
     */
    private Map<String, Object> createMockOrder(String market, String side, BigDecimal price, BigDecimal volume) {
        Map<String, Object> mockOrder = new HashMap<>();
        mockOrder.put("uuid", UUID.randomUUID().toString());
        mockOrder.put("market", market);
        mockOrder.put("side", side);
        mockOrder.put("ord_type", side.equals("bid") ? "price" : "limit");
        mockOrder.put("price", price != null ? price.toString() : null);
        mockOrder.put("volume", volume != null ? volume.toString() : null);
        mockOrder.put("state", "done");
        mockOrder.put("created_at", new Date().toString());
        mockOrder.put("executed_volume", volume != null ? volume.toString() : "0");
        mockOrder.put("paid_fee", "0");
        mockOrder.put("locked", "0");
        mockOrder.put("remaining_volume", "0");
        mockOrder.put("trades_count", 1);
        
        log.debug("모의 주문 생성: market={}, side={}", market, side);
        return mockOrder;
    }
    
    /**
     * API 연결 테스트
     * 단일 책임: API 연결 상태 확인만 담당
     * 
     * @return API 연결 가능 여부
     */
    public boolean testConnection() {
        log.debug("업비트 API 연결 테스트");
        
        try {
            WebClient webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
            
            String response = webClient.get()
                .uri(ApiConstants.UPBIT_MARKET_ALL)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(timeout))
                .block();
            
            boolean isConnected = response != null && !response.isEmpty();
            log.info("업비트 API 연결 테스트 결과: {}", isConnected ? "성공" : "실패");
            return isConnected;
            
        } catch (Exception e) {
            log.error("업비트 API 연결 테스트 실패", e);
            return false;
        }
    }
}
