package com.ai.CoinDoctor.controller.market;

import com.ai.CoinDoctor.service.market.UpbitApiService;
import com.ai.CoinDoctor.shared.dto.ApiResponse;
import com.ai.CoinDoctor.shared.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 시장 데이터 컨트롤러
 * 단일 책임: 시장 데이터 관련 HTTP 요청 처리만 담당
 * One Source of Truth: 시장 데이터 API는 이 컨트롤러를 통해서만
 */
@Slf4j
@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 프론트엔드 연동을 위한 CORS 설정
public class MarketController {
    
    private final UpbitApiService upbitApiService;
    
    /**
     * 거래대금 상위 5종목 조회
     * 단일 책임: 상위 5종목 조회 요청 처리만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return 거래대금 상위 5종목 목록
     */
    @GetMapping("/top5")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTop5Markets() {
        log.debug("거래대금 상위 5종목 조회 요청 수신");
        
        try {
            // One Source of Truth: 비즈니스 로직은 Service에서만
            List<Map<String, Object>> top5Markets = upbitApiService.getTop5ByVolume();
            
            log.info("거래대금 상위 5종목 조회 성공: {} 종목", top5Markets.size());
            return ResponseEntity.ok(ApiResponse.success(top5Markets));
            
        } catch (ApiException e) {
            log.error("거래대금 상위 5종목 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode())
                .body(ApiResponse.error(e.getMessage()));
                
        } catch (Exception e) {
            log.error("거래대금 상위 5종목 조회 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("거래대금 상위 5종목 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 현재가 조회
     * 단일 책임: 현재가 조회 요청 처리만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param market 마켓 코드 (예: KRW-BTC)
     * @return 현재가 정보
     */
    @GetMapping("/ticker/{market}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTicker(
            @PathVariable String market) {
        log.debug("현재가 조회 요청: market={}", market);
        
        try {
            // 에러 처리: 파라미터 검증
            if (market == null || market.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("마켓 코드가 비어있습니다."));
            }
            
            // 하드코딩 금지: 마켓 코드 형식 검증
            if (!market.matches("^[A-Z]+-[A-Z]+$")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("올바른 마켓 코드 형식이 아닙니다. (예: KRW-BTC)"));
            }
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            Map<String, Object> ticker = upbitApiService.getTicker(market);
            
            log.debug("현재가 조회 성공: market={}", market);
            return ResponseEntity.ok(ApiResponse.success(ticker));
            
        } catch (ApiException e) {
            log.error("현재가 조회 실패: market={}, error={}", market, e.getMessage());
            return ResponseEntity.status(e.getStatusCode())
                .body(ApiResponse.error(e.getMessage()));
                
        } catch (Exception e) {
            log.error("현재가 조회 중 예상치 못한 오류 발생: market={}", market, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("현재가 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 1분봉 데이터 조회
     * 단일 책임: 캔들 데이터 조회 요청 처리만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param market 마켓 코드 (예: KRW-BTC)
     * @param count 조회할 캔들 개수 (기본값: 20)
     * @return 1분봉 데이터 목록
     */
    @GetMapping("/candles/{market}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMinuteCandles(
            @PathVariable String market,
            @RequestParam(defaultValue = "20") int count) {
        log.debug("1분봉 데이터 조회 요청: market={}, count={}", market, count);
        
        try {
            // 에러 처리: 파라미터 검증
            if (market == null || market.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("마켓 코드가 비어있습니다."));
            }
            
            // 하드코딩 금지: 마켓 코드 형식 검증
            if (!market.matches("^[A-Z]+-[A-Z]+$")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("올바른 마켓 코드 형식이 아닙니다. (예: KRW-BTC)"));
            }
            
            // 에러 처리: count 범위 검증
            if (count <= 0 || count > 200) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("캔들 개수는 1~200 사이여야 합니다."));
            }
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            List<Map<String, Object>> candles = upbitApiService.getMinuteCandles(market, count);
            
            log.debug("1분봉 데이터 조회 성공: market={}, count={}", market, candles.size());
            return ResponseEntity.ok(ApiResponse.success(candles));
            
        } catch (ApiException e) {
            log.error("1분봉 데이터 조회 실패: market={}, error={}", market, e.getMessage());
            return ResponseEntity.status(e.getStatusCode())
                .body(ApiResponse.error(e.getMessage()));
                
        } catch (Exception e) {
            log.error("1분봉 데이터 조회 중 예상치 못한 오류 발생: market={}", market, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("1분봉 데이터 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 여러 마켓의 현재가 일괄 조회
     * 단일 책임: 여러 마켓 현재가 조회 요청 처리만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param markets 마켓 코드 목록 (쉼표로 구분, 예: KRW-BTC,KRW-ETH)
     * @return 여러 마켓의 현재가 정보 목록
     */
    @GetMapping("/tickers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMultipleTickers(
            @RequestParam String markets) {
        log.debug("여러 마켓 현재가 조회 요청: markets={}", markets);
        
        try {
            // 에러 처리: 파라미터 검증
            if (markets == null || markets.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("마켓 코드가 비어있습니다."));
            }
            
            // 하드코딩 금지: 마켓 코드 분리 및 검증
            String[] marketArray = markets.split(",");
            
            // 에러 처리: 마켓 개수 제한
            if (marketArray.length > 10) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("한 번에 최대 10개의 마켓만 조회할 수 있습니다."));
            }
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            List<Map<String, Object>> tickers = new java.util.ArrayList<>();
            for (String market : marketArray) {
                String trimmedMarket = market.trim();
                if (!trimmedMarket.isEmpty()) {
                    Map<String, Object> ticker = upbitApiService.getTicker(trimmedMarket);
                    tickers.add(ticker);
                }
            }
            
            log.debug("여러 마켓 현재가 조회 성공: {} 종목", tickers.size());
            return ResponseEntity.ok(ApiResponse.success(tickers));
            
        } catch (ApiException e) {
            log.error("여러 마켓 현재가 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode())
                .body(ApiResponse.error(e.getMessage()));
                
        } catch (Exception e) {
            log.error("여러 마켓 현재가 조회 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("여러 마켓 현재가 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 업비트 API 연결 테스트
     * 단일 책임: API 연결 상태 확인만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return API 연결 상태
     */
    @GetMapping("/connection-test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testConnection() {
        log.debug("업비트 API 연결 테스트 요청");
        
        try {
            // One Source of Truth: 비즈니스 로직은 Service에서만
            boolean isConnected = upbitApiService.testConnection();
            
            // 하드코딩 금지: 응답 데이터를 Map으로 구성
            Map<String, Object> data = new HashMap<>();
            data.put("connected", isConnected);
            data.put("status", isConnected ? "UP" : "DOWN");
            data.put("message", isConnected ? 
                "업비트 API 연결 성공" : "업비트 API 연결 실패");
            
            if (isConnected) {
                log.info("업비트 API 연결 테스트 성공");
                return ResponseEntity.ok(ApiResponse.success(data));
            } else {
                log.warn("업비트 API 연결 테스트 실패");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("업비트 API에 연결할 수 없습니다.", data));
            }
            
        } catch (Exception e) {
            log.error("업비트 API 연결 테스트 중 오류 발생", e);
            
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("connected", false);
            errorData.put("status", "ERROR");
            errorData.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("API 연결 테스트 중 오류가 발생했습니다.", errorData));
        }
    }
    
    /**
     * 헬스 체크
     * 단일 책임: API 상태 확인만 담당
     * 
     * @return API 상태
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(ApiResponse.success("Market API is running", data));
    }
}
