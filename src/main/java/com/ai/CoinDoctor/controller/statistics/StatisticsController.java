package com.ai.CoinDoctor.controller.statistics;

import com.ai.CoinDoctor.service.statistics.StatisticsService;
import com.ai.CoinDoctor.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 컨트롤러
 * 단일 책임: 거래 통계 관련 HTTP 요청 처리만 담당
 * One Source of Truth: 통계 데이터는 이 컨트롤러를 통해서만
 */
@Slf4j
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 프론트엔드 연동을 위한 CORS 설정
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    /**
     * 일별 통계 조회
     * 단일 책임: 특정 날짜의 통계 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param date 조회할 날짜 (yyyy-MM-dd)
     * @return 일별 통계 데이터
     */
    @GetMapping("/daily/{date}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDailyStatistics(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        log.debug("일별 통계 조회 요청: date={}", date);
        
        try {
            // 에러 처리: 파라미터 검증
            if (date == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("날짜가 비어있습니다."));
            }
            
            // 에러 처리: 미래 날짜 검증
            if (date.isAfter(LocalDate.now())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("미래 날짜는 조회할 수 없습니다."));
            }
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            Map<String, Object> statistics = statisticsService.getDailyStatistics(date);
            
            log.debug("일별 통계 조회 성공: date={}", date);
            return ResponseEntity.ok(ApiResponse.success(statistics));
            
        } catch (Exception e) {
            log.error("일별 통계 조회 중 오류 발생: date={}", date, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("일별 통계 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 오늘의 통계 조회
     * 단일 책임: 오늘 날짜의 통계 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return 오늘의 통계 데이터
     */
    @GetMapping("/daily/today")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTodayStatistics() {
        log.debug("오늘의 통계 조회 요청");
        
        try {
            // 하드코딩 금지: 오늘 날짜를 동적으로 계산
            LocalDate today = LocalDate.now();
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            Map<String, Object> statistics = statisticsService.getDailyStatistics(today);
            
            log.debug("오늘의 통계 조회 성공: date={}", today);
            return ResponseEntity.ok(ApiResponse.success(statistics));
            
        } catch (Exception e) {
            log.error("오늘의 통계 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("오늘의 통계 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 기간별 통계 조회
     * 단일 책임: 특정 기간의 통계 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param startDate 시작 날짜 (yyyy-MM-dd)
     * @param endDate 종료 날짜 (yyyy-MM-dd)
     * @return 기간별 통계 데이터 목록
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRangeStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.debug("기간별 통계 조회 요청: startDate={}, endDate={}", startDate, endDate);
        
        try {
            // 에러 처리: 파라미터 검증
            if (startDate == null || endDate == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("시작 날짜와 종료 날짜가 필요합니다."));
            }
            
            // 에러 처리: 날짜 순서 검증
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("시작 날짜는 종료 날짜보다 이전이어야 합니다."));
            }
            
            // 에러 처리: 미래 날짜 검증
            if (endDate.isAfter(LocalDate.now())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("종료 날짜는 오늘 이전이어야 합니다."));
            }
            
            // 에러 처리: 조회 기간 제한 (최대 90일)
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            if (daysBetween > 90) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("조회 기간은 최대 90일까지 가능합니다."));
            }
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            List<Map<String, Object>> statistics = statisticsService.getRangeStatistics(startDate, endDate);
            
            log.debug("기간별 통계 조회 성공: {} ~ {}, {} 건", startDate, endDate, statistics.size());
            return ResponseEntity.ok(ApiResponse.success(statistics));
            
        } catch (Exception e) {
            log.error("기간별 통계 조회 중 오류 발생: {} ~ {}", startDate, endDate, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("기간별 통계 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 최근 N일 통계 조회
     * 단일 책임: 최근 N일의 통계 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param days 조회할 일수 (기본값: 7일)
     * @return 최근 N일 통계 데이터 목록
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecentStatistics(
            @RequestParam(defaultValue = "7") int days) {
        log.debug("최근 N일 통계 조회 요청: days={}", days);
        
        try {
            // 에러 처리: days 범위 검증
            if (days <= 0 || days > 90) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("조회 일수는 1~90일 사이여야 합니다."));
            }
            
            // 하드코딩 금지: 날짜 범위를 동적으로 계산
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            List<Map<String, Object>> statistics = statisticsService.getRangeStatistics(startDate, endDate);
            
            log.debug("최근 {}일 통계 조회 성공: {} 건", days, statistics.size());
            return ResponseEntity.ok(ApiResponse.success(statistics));
            
        } catch (Exception e) {
            log.error("최근 {}일 통계 조회 중 오류 발생", days, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("최근 통계 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 전체 통계 요약 조회
     * 단일 책임: 전체 기간의 통계 요약 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @return 전체 통계 요약 데이터
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTotalSummary() {
        log.debug("전체 통계 요약 조회 요청");
        
        try {
            // One Source of Truth: 비즈니스 로직은 Service에서만
            Map<String, Object> summary = statisticsService.getTotalSummary();
            
            log.debug("전체 통계 요약 조회 성공");
            return ResponseEntity.ok(ApiResponse.success(summary));
            
        } catch (Exception e) {
            log.error("전체 통계 요약 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("전체 통계 요약 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 월별 통계 조회
     * 단일 책임: 특정 월의 통계 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param year 연도 (yyyy)
     * @param month 월 (1-12)
     * @return 월별 통계 데이터
     */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyStatistics(
            @PathVariable int year,
            @PathVariable int month) {
        log.debug("월별 통계 조회 요청: year={}, month={}", year, month);
        
        try {
            // 에러 처리: 연도 검증
            if (year < 2020 || year > LocalDate.now().getYear()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("올바른 연도를 입력해주세요. (2020 ~ 현재)"));
            }
            
            // 에러 처리: 월 검증
            if (month < 1 || month > 12) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("월은 1~12 사이여야 합니다."));
            }
            
            // 하드코딩 금지: 월의 시작일을 동적으로 계산
            LocalDate startDate = LocalDate.of(year, month, 1);
            
            // 에러 처리: 미래 날짜 검증
            if (startDate.isAfter(LocalDate.now())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("미래 날짜는 조회할 수 없습니다."));
            }
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            Map<String, Object> statistics = statisticsService.getMonthlyStatistics(year, month);
            
            log.debug("월별 통계 조회 성공: {}-{}", year, month);
            return ResponseEntity.ok(ApiResponse.success(statistics));
            
        } catch (Exception e) {
            log.error("월별 통계 조회 중 오류 발생: {}-{}", year, month, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("월별 통계 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 승률 조회
     * 단일 책임: 특정 기간의 승률 조회만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param startDate 시작 날짜 (yyyy-MM-dd, 선택)
     * @param endDate 종료 날짜 (yyyy-MM-dd, 선택)
     * @return 승률 데이터
     */
    @GetMapping("/win-rate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWinRate(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.debug("승률 조회 요청: startDate={}, endDate={}", startDate, endDate);
        
        try {
            // 하드코딩 금지: 날짜가 없으면 전체 기간으로 설정
            if (startDate == null && endDate == null) {
                // 전체 기간 승률 조회
                double winRate = statisticsService.calculateWinRate(null, null);
                
                Map<String, Object> data = new HashMap<>();
                data.put("winRate", winRate);
                data.put("period", "전체");
                
                log.debug("전체 기간 승률 조회 성공: {}%", winRate);
                return ResponseEntity.ok(ApiResponse.success(data));
            }
            
            // 에러 처리: 날짜 검증
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("시작 날짜는 종료 날짜보다 이전이어야 합니다."));
            }
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            double winRate = statisticsService.calculateWinRate(startDate, endDate);
            
            Map<String, Object> data = new HashMap<>();
            data.put("winRate", winRate);
            data.put("startDate", startDate);
            data.put("endDate", endDate);
            
            log.debug("승률 조회 성공: {} ~ {}, {}%", startDate, endDate, winRate);
            return ResponseEntity.ok(ApiResponse.success(data));
            
        } catch (Exception e) {
            log.error("승률 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("승률 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 일별 요약 생성
     * 단일 책임: 일별 통계 요약 생성만 담당
     * 에러 처리: try-catch로 예외 처리
     * 
     * @param date 생성할 날짜 (yyyy-MM-dd)
     * @return 생성된 일별 요약 데이터
     */
    @PostMapping("/daily/generate/{date}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateDailySummary(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        log.debug("일별 요약 생성 요청: date={}", date);
        
        try {
            // 에러 처리: 파라미터 검증
            if (date == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("날짜가 비어있습니다."));
            }
            
            // 에러 처리: 미래 날짜 검증
            if (date.isAfter(LocalDate.now())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("미래 날짜의 요약은 생성할 수 없습니다."));
            }
            
            // One Source of Truth: 비즈니스 로직은 Service에서만
            statisticsService.generateDailySummary(date);
            
            // 생성된 요약 조회
            Map<String, Object> summary = statisticsService.getDailyStatistics(date);
            
            log.info("일별 요약 생성 성공: date={}", date);
            return ResponseEntity.ok(ApiResponse.success("일별 요약이 생성되었습니다.", summary));
            
        } catch (Exception e) {
            log.error("일별 요약 생성 중 오류 발생: date={}", date, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("일별 요약 생성 중 오류가 발생했습니다."));
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
        
        return ResponseEntity.ok(ApiResponse.success("Statistics API is running", data));
    }
}
