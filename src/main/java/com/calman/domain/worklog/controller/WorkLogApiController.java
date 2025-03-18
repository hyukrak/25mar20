package com.calman.domain.worklog.controller;

import com.calman.domain.worklog.dto.WorkLogDTO;
import com.calman.domain.worklog.dto.WorkLogDTO.CreateRequest;
import com.calman.domain.worklog.dto.WorkLogDTO.DetailResponse;
import com.calman.domain.worklog.dto.WorkLogDTO.UpdateRequest;
import com.calman.domain.worklog.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 작업 로그 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/worklogs")
@RequiredArgsConstructor
public class WorkLogApiController {

  private final WorkLogService workLogService;

  /**
   * 작업 로그 생성
   * @param request 생성 요청 DTO
   * @return 생성된 작업 로그 ID
   */
  @PostMapping
  public ResponseEntity<Map<String, Long>> createWorkLog(@RequestBody CreateRequest request) {
    Long id = workLogService.createWorkLog(request);
    return ResponseEntity.ok(Map.of("id", id));
  }

  /**
   * ID로 작업 로그 조회
   * @param id 작업 로그 ID
   * @return 작업 로그 정보
   */
  @GetMapping("/{id}")
  public ResponseEntity<DetailResponse> getWorkLog(@PathVariable Long id) {
    DetailResponse workLog = workLogService.getWorkLogDetailById(id);
    if (workLog == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "작업 로그를 찾을 수 없습니다.");
    }
    return ResponseEntity.ok(workLog);
  }

  /**
   * 작업 로그 목록 조회 (필터링 및 페이징)
   * @param carModel 차량 모델 필터
   * @param materialCode 자재 코드 필터
   * @param status 상태 필터
   * @param startDate 시작 날짜 필터
   * @param endDate 종료 날짜 필터
   * @param page 페이지 번호
   * @param size 페이지 크기
   * @param sortField 정렬 필드
   * @param sortDirection 정렬 방향
   * @return 작업 로그 목록 및 페이징 정보
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getWorkLogs(
      @RequestParam(required = false) String carModel,
      @RequestParam(required = false) String materialCode,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size,
      @RequestParam(required = false) String sortField,
      @RequestParam(required = false, defaultValue = "DESC") String sortDirection
  ) {
    Map<String, Object> result = workLogService.getWorkLogs(
        carModel, materialCode, status, startDate, endDate,
        page, size, sortField, sortDirection
    );
    return ResponseEntity.ok(result);
  }

  /**
   * 작업 로그 업데이트
   * @param id 업데이트할 작업 로그 ID
   * @param request 업데이트 요청 DTO
   * @return 성공 시 204 No Content
   */
  @PutMapping("/{id}")
  public ResponseEntity<Void> updateWorkLog(
      @PathVariable Long id,
      @RequestBody UpdateRequest request
  ) {
    boolean updated = workLogService.updateWorkLog(id, request);
    if (!updated) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "작업 로그를 찾을 수 없습니다.");
    }
    return ResponseEntity.noContent().build();
  }

  /**
   * 작업 로그 삭제 (소프트 삭제)
   * @param id 삭제할 작업 로그 ID
   * @return 성공 시 204 No Content
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteWorkLog(@PathVariable Long id) {
    boolean deleted = workLogService.deleteWorkLog(id);
    if (!deleted) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "작업 로그를 찾을 수 없습니다.");
    }
    return ResponseEntity.noContent().build();
  }

  /**
   * 날짜 범위로 작업 로그 목록 조회
   * @param startDate 시작 날짜
   * @param endDate 종료 날짜
   * @return 작업 로그 목록
   */
  @GetMapping("/by-date-range")
  public ResponseEntity<List<WorkLogDTO>> getWorkLogsByDateRange(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
  ) {
    return ResponseEntity.ok(workLogService.getWorkLogsByDateRange(startDate, endDate));
  }

  /**
   * 차량 모델로 작업 로그 목록 조회
   * @param carModel 차량 모델명
   * @return 작업 로그 목록
   */
  @GetMapping("/by-car-model/{carModel}")
  public ResponseEntity<List<WorkLogDTO>> getWorkLogsByCarModel(@PathVariable String carModel) {
    return ResponseEntity.ok(workLogService.getWorkLogsByCarModel(carModel));
  }

  /**
   * 자재 코드로 작업 로그 목록 조회
   * @param materialCode 자재 코드
   * @return 작업 로그 목록
   */
  @GetMapping("/by-material-code/{materialCode}")
  public ResponseEntity<List<WorkLogDTO>> getWorkLogsByMaterialCode(@PathVariable String materialCode) {
    return ResponseEntity.ok(workLogService.getWorkLogsByMaterialCode(materialCode));
  }

  /**
   * 상태별 작업 로그 목록 조회
   * @param status 상태
   * @return 작업 로그 목록
   */
  @GetMapping("/by-status/{status}")
  public ResponseEntity<List<WorkLogDTO>> getWorkLogsByStatus(@PathVariable String status) {
    return ResponseEntity.ok(workLogService.getWorkLogsByStatus(status));
  }

  /**
   * 작업자 ID로 작업 로그 목록 조회
   * @param userId 작업자 ID
   * @return 작업 로그 목록
   */
  @GetMapping("/by-user/{userId}")
  public ResponseEntity<List<WorkLogDTO>> getWorkLogsByUserId(@PathVariable Long userId) {
    return ResponseEntity.ok(workLogService.getWorkLogsByUserId(userId));
  }
}