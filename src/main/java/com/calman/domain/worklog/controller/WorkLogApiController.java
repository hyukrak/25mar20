package com.calman.domain.worklog.controller;

import com.calman.domain.worklog.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 작업 로그 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkLogApiController {

  private final WorkLogService workLogService;

  /**
   * 작업 로그 목록 조회 - 모든 작업 로그 데이터를 JSON 형식으로 반환
   *
   * 반환 데이터:
   * - id: 작업 로그 고유 ID
   * - workDatetime: 작업시간 (YYYY-MM-DD HH:MM:SS)
   * - carModel: 차종
   * - productColor: 제품 색상
   * - productCode: 제품 코드
   * - productName: 제품 이름
   * - quantity: 수량
   * - createdAt: 생성일시
   *
   * @return 모든 작업 로그 목록을 포함한 JSON 응답
   */
  @GetMapping("/worklogs")
  public ResponseEntity<Map<String, Object>> getWorkLogs() {
    Map<String, Object> result = workLogService.getWorkLogs(
        null,  // carModel
        null,  // productCode
        null,  // status
        null,  // startDate
        null,  // endDate
        null,  // pageNumber (사용 안함)
        null,  // pageSize (사용 안함)
        "wl_created_at",    // sortField
        "DESC"  // sortDirection
    );

    return ResponseEntity.ok(result);
  }
}