package com.calman.domain.worklog.controller;

import com.calman.domain.worklog.dto.WorkLogDTO;
import com.calman.domain.worklog.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
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
   * - completedAt: 완료 일시 (완료인 경우)
   * - createdAt: 생성일시
   *
   * @return 모든 작업 로그 목록을 포함한 JSON 응답
   */
  @GetMapping("/worklogs")
  public ResponseEntity<Map<String, Object>> getWorkLogs(
      @RequestParam(required = false) String carModel,
      @RequestParam(required = false) String productCode,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
      @RequestParam(required = false) String sortField,
      @RequestParam(required = false, defaultValue = "DESC") String sortDirection
  ) {
    Map<String, Object> result = workLogService.getWorkLogs(
        carModel,
        productCode,
        status,
        startDate,
        endDate,
        sortField,
        sortDirection
    );

    return ResponseEntity.ok(result);
  }
/**
 *  (OLD)작업 로그 목록 조회
  */
//    public ResponseEntity<Map<String, Object>> getWorkLogs() {
//    Map<String, Object> result = workLogService.getWorkLogs(
//        null,  // carModel
//        null,  // productCode
//        null,  // status
//        null,  // startDate
//        null,  // endDate
//        null,  // pageNumber (사용 안함)
//        null,  // pageSize (사용 안함)
//        "wl_created_at",    // sortField
//        "DESC"  // sortDirection
//    );


  /**
   * 특정 날짜의 작업 로그 조회
   *
   * @param date 조회할 날짜 (YYYY-MM-DD)
   * @return 해당 날짜의 작업 로그 목록
   */
  @GetMapping("/worklogs/date/{date}")
  public ResponseEntity<Map<String, Object>> getWorkLogsByDate(
      @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
  ) {
    Map<String, Object> result = workLogService.getWorkLogsByExactDate(date);
    return ResponseEntity.ok(result);
  }

  /**
   * 작업 로그 상세 조회
   *
   * @param id 작업 로그 ID
   * @return 작업 로그 상세 정보
   */
  @GetMapping("/worklogs/{id}")
  public ResponseEntity<?> getWorkLogById(@PathVariable Long id) {
    WorkLogDTO.DetailResponse workLog = workLogService.getWorkLogDetailById(id);
    if (workLog == null) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "작업 로그를 찾을 수 없습니다.");
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(workLog);
  }

  /**
   * 작업 로그 생성
   *
   * @param request 작업 로그 생성 요청 정보
   * @return 생성된 작업 로그 ID
   */
  @PostMapping("/worklogs")
  public ResponseEntity<Map<String, Object>> createWorkLog(@RequestBody WorkLogDTO.CreateRequest request) {
    Long id = workLogService.createWorkLog(request);

    Map<String, Object> response = new HashMap<>();
    response.put("id", id);
    response.put("success", true);
    response.put("message", "작업 로그가 성공적으로 생성되었습니다.");

    return ResponseEntity.ok(response);
  }

  /**
   * 작업 로그 수정
   *
   * @param id 수정할 작업 로그 ID
   * @param request 수정 요청 정보
   * @return 수정 결과
   */
  @PutMapping("/worklogs/{id}")
  public ResponseEntity<Map<String, Object>> updateWorkLog(
      @PathVariable Long id,
      @RequestBody WorkLogDTO.UpdateRequest request
  ) {
    boolean updated = workLogService.updateWorkLog(id, request);

    Map<String, Object> response = new HashMap<>();
    response.put("success", updated);

    if (updated) {
      response.put("message", "작업 로그가 성공적으로 수정되었습니다.");
      return ResponseEntity.ok(response);
    } else {
      response.put("message", "작업 로그 수정에 실패했습니다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 작업 로그 상태 업데이트
   *
   * @param id 수정할 작업 로그 ID
   * @param request 상태 업데이트 요청 정보
   * @return 수정 결과
   */
  @PutMapping("/worklogs/{id}/status")
  public ResponseEntity<Map<String, Object>> updateWorkLogStatus(
      @PathVariable Long id,
      @RequestBody WorkLogDTO.StatusUpdateRequest request
  ) {
    boolean updated = workLogService.updateWorkLogCompletionStatus(id, request.isCompleted());

    Map<String, Object> response = new HashMap<>();
    response.put("success", updated);

    if (updated) {
      String message = request.isCompleted() ?
          "작업이 완료 상태로 변경되었습니다." : "작업이 미완료 상태로 변경되었습니다.";
      response.put("message", message);
      return ResponseEntity.ok(response);
    } else {
      response.put("message", "작업 로그 상태 변경에 실패했습니다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 작업 로그 삭제
   *
   * @param id 삭제할 작업 로그 ID
   * @return 삭제 결과
   */
  @DeleteMapping("/worklogs/{id}")
  public ResponseEntity<Map<String, Object>> deleteWorkLog(@PathVariable Long id) {
    boolean deleted = workLogService.deleteWorkLog(id);

    Map<String, Object> response = new HashMap<>();
    response.put("success", deleted);

    if (deleted) {
      response.put("message", "작업 로그가 성공적으로 삭제되었습니다.");
      return ResponseEntity.ok(response);
    } else {
      response.put("message", "작업 로그 삭제에 실패했습니다.");
      return ResponseEntity.badRequest().body(response);
    }
  }
}