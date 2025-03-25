package com.calman.domain.worklog.controller;

import com.calman.domain.worklog.dto.WorkLogDTO;
import com.calman.domain.worklog.service.WorkLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 작업계획 REST API 컨트롤러
 * 작업계획의 생성, 조회, 수정, 삭제 및 상태 관리를 담당하는 API 엔드포인트들을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "작업계획 관리", description = "작업계획 생성, 조회, 수정, 삭제 API")
public class WorkLogApiController {

  private final WorkLogService workLogService;


  /**
   * 루트 경로에서 작업계획 페이지로 리다이렉트
   * /api/ 경로로 접근 시 정적 HTML 페이지로 리다이렉트합니다.
   *
   * @return 302 리다이렉트 응답
   */
  @Operation(
      summary = "루트 경로 리다이렉트",
      description = "API 루트 경로 접근 시 작업계획 HTML 페이지로 리다이렉트합니다."
  )
  @ApiResponse(
      responseCode = "302",
      description = "정적 HTML 페이지로 리다이렉트"
  )
  @GetMapping("/")
  public ResponseEntity<Void> redirectToWorklogs() {
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(URI.create("/worklogs.html"));
    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }

  /**
   * 작업계획 목록 조회 - 모든 작업계획 데이터를 JSON 형식으로 반환
   * 다양한 필터링 및 정렬 옵션을 통해 작업계획를 조회할 수 있습니다.
   *
   * 반환 데이터:
   * - id: 작업계획 고유 ID
   * - workDatetime: 작업시간 (YYYY-MM-DD HH:MM:SS)
   * - carModel: 차종
   * - productColor: 제품 색상
   * - productCode: 제품 코드
   * - productName: 제품 이름
   * - quantity: 수량
   * - completedAt: 완료 일시 (완료인 경우)
   * - createdAt: 생성일시
   *
   * @param carModel 차종으로 필터링 (선택적)
   * @param productCode 제품 코드로 필터링 (선택적)
   * @param status 상태로 필터링 (completed, incomplete, 선택적)
   * @param startDate 시작 날짜로 필터링 (ISO 형식, 선택적)
   * @param endDate 종료 날짜로 필터링 (ISO 형식, 선택적)
   * @param sortField 정렬 필드 (선택적, 기본값: wl_work_datetime)
   * @param sortDirection 정렬 방향 (ASC 또는 DESC, 기본값: DESC)
   * @return 필터링 및 정렬된 작업계획 목록
   */
  @Operation(
      summary = "작업계획 목록 조회",
      description = "다양한 필터링 및 정렬 옵션을 통해 작업계획 목록을 조회합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "작업계획 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = Map.class))
      )
  })
  @GetMapping("/worklogs")
  public ResponseEntity<Map<String, Object>> getWorkLogs(
      @Parameter(description = "차종 (부분 일치 검색)")
      @RequestParam(required = false) String carModel,

      @Parameter(description = "제품 코드 (부분 일치 검색)")
      @RequestParam(required = false) String productCode,

      @Parameter(description = "상태 필터 (completed: 완료, incomplete: 미완료)")
      @RequestParam(required = false) String status,

      @Parameter(description = "시작 날짜 (ISO 형식: yyyy-MM-dd'T'HH:mm:ss)")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

      @Parameter(description = "종료 날짜 (ISO 형식: yyyy-MM-dd'T'HH:mm:ss)")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

      @Parameter(description = "정렬 필드 (wl_work_datetime, wl_car_model, wl_product_code 등)")
      @RequestParam(required = false) String sortField,

      @Parameter(description = "정렬 방향 (ASC: 오름차순, DESC: 내림차순)")
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
   * 특정 날짜의 작업계획 조회
   * 지정된 날짜에 해당하는 작업계획만 필터링하여 조회합니다.
   * 상태 필터와 정렬 옵션을 추가로 지정할 수 있습니다.
   *
   * @param date 조회할 날짜 (YYYY-MM-DD)
   * @param status 상태 필터 (completed: 완료, incomplete: 미완료, null: 모든 상태)
   * @param sortField 정렬 필드 (선택적, 기본값: wl_work_datetime)
   * @param sortDirection 정렬 방향 (ASC: 오름차순, DESC: 내림차순, 기본값: ASC)
   * @return 해당 날짜의 작업계획 목록
   */
  @Operation(
      summary = "특정 날짜의 작업계획 조회",
      description = "지정된 날짜에 해당하는 작업계획를 필터링하여 조회합니다. 상태 필터와 정렬 옵션을 추가로 지정할 수 있습니다."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "날짜별 작업계획 조회 성공",
          content = @Content(schema = @Schema(implementation = Map.class))
      )
  })
  @GetMapping("/worklogs/date/{date}")
  public ResponseEntity<Map<String, Object>> getWorkLogsByDate(
      @Parameter(description = "조회할 날짜 (yyyy-MM-dd 형식)")
      @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,

      @Parameter(description = "상태 필터 (completed: 완료, incomplete: 미완료, null: 모든 상태)")
      @RequestParam(required = false) String status,

      @Parameter(description = "정렬 필드 (wl_work_datetime, wl_car_model 등)")
      @RequestParam(required = false) String sortField,

      @Parameter(description = "정렬 방향 (ASC: 오름차순, DESC: 내림차순)")
      @RequestParam(required = false, defaultValue = "ASC") String sortDirection
  ) {
    // 날짜 파라미터 검증
    LocalDate validDate = date;
    if (validDate == null) {
      // 날짜가 null이면 현재 날짜 사용
      validDate = LocalDate.now();
      log.warn("날짜 파라미터가 null입니다. 현재 날짜로 대체: {}", validDate);
    }

    // 유효한 날짜로 조회 (정렬 필드와 방향, 상태 필터 함께 전달)
    Map<String, Object> result = workLogService.getWorkLogsByExactDate(validDate, status, sortField, sortDirection);
    return ResponseEntity.ok(result);
  }

  /**
   * 작업계획 상세 조회
   * 특정 ID의 작업계획 상세 정보를 조회합니다.
   *
   * @param id 작업계획 ID
   * @return 작업계획 상세 정보
   */
  @Operation(
      summary = "작업계획 상세 조회",
      description = "특정 ID의 작업계획 상세 정보를 조회합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "작업계획 상세 조회 성공",
          content = @Content(schema = @Schema(implementation = WorkLogDTO.DetailResponse.class))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "작업계획를 찾을 수 없음"
      )
  })
  @GetMapping("/worklogs/{id}")
  public ResponseEntity<?> getWorkLogById(
      @Parameter(description = "조회할 작업계획 ID")
      @PathVariable Long id
  ) {
    WorkLogDTO.DetailResponse workLog = workLogService.getWorkLogDetailById(id);
    if (workLog == null) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "작업계획를 찾을 수 없습니다.");
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(workLog);
  }

  /**
   * 작업계획 생성
   * 새로운 작업계획를 생성합니다.
   *
   * @param request 작업계획 생성 요청 정보 (작업시간, 차종, 제품 색상, 제품 코드, 제품 이름, 수량)
   * @return 생성된 작업계획 ID 및 성공 메시지
   */
  @Operation(
      summary = "작업계획 생성",
      description = "새로운 작업계획를 생성합니다. 작업 시간, 차종, 제품 정보, 수량 등의 정보가 필요합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "작업계획 생성 성공",
          content = @Content(schema = @Schema(implementation = Map.class))
      )
  })
  @PostMapping("/worklogs")
  public ResponseEntity<Map<String, Object>> createWorkLog(
      @Parameter(description = "작업계획 생성 요청 정보")
      @RequestBody WorkLogDTO.CreateRequest request
  ) {
    Long id = workLogService.createWorkLog(request);

    Map<String, Object> response = new HashMap<>();
    response.put("id", id);
    response.put("success", true);
    response.put("message", "작업계획가 성공적으로 생성되었습니다.");

    return ResponseEntity.ok(response);
  }

  /**
   * 작업계획 수정
   * 기존 작업계획의 정보를 수정합니다.
   *
   * @param id 수정할 작업계획 ID
   * @param request 수정 요청 정보 (작업시간, 차종, 제품 색상, 제품 코드, 제품 이름, 수량)
   * @return 수정 결과 메시지
   */
  @Operation(
      summary = "작업계획 수정",
      description = "기존 작업계획의 정보를 수정합니다. 작업 시간, 차종, 제품 정보, 수량 등을 변경할 수 있습니다."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "작업계획 수정 성공",
          content = @Content(schema = @Schema(implementation = Map.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "작업계획 수정 실패",
          content = @Content(schema = @Schema(implementation = Map.class))
      )
  })
  @PutMapping("/worklogs/{id}")
  public ResponseEntity<Map<String, Object>> updateWorkLog(
      @Parameter(description = "수정할 작업계획 ID")
      @PathVariable Long id,

      @Parameter(description = "작업계획 수정 요청 정보")
      @RequestBody WorkLogDTO.UpdateRequest request
  ) {
    boolean updated = workLogService.updateWorkLog(id, request);

    Map<String, Object> response = new HashMap<>();
    response.put("success", updated);

    if (updated) {
      response.put("message", "작업계획가 성공적으로 수정되었습니다.");
      return ResponseEntity.ok(response);
    } else {
      response.put("message", "작업계획 수정에 실패했습니다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 작업계획 상태 업데이트
   * 작업계획의 완료/미완료 상태를 변경합니다.
   * 클라이언트 ID 헤더를 통해 상태 변경 주체를 추적합니다.
   *
   * @param id 수정할 작업계획 ID
   * @param request 상태 업데이트 요청 정보 (completed: true/false)
   * @param clientId 클라이언트/디바이스 식별자 (헤더 X-Client-ID에서 추출)
   * @return 상태 변경 결과 메시지
   */
  @Operation(
      summary = "작업계획 상태 업데이트",
      description = "작업계획의 완료/미완료 상태를 변경합니다. 클라이언트 ID 헤더를 통해 상태 변경 주체를 추적합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "상태 업데이트 성공",
          content = @Content(schema = @Schema(implementation = Map.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "상태 업데이트 실패",
          content = @Content(schema = @Schema(implementation = Map.class))
      )
  })
  @PutMapping("/worklogs/{id}/status")
  public ResponseEntity<Map<String, Object>> updateWorkLogStatus(
      @Parameter(description = "상태를 변경할 작업계획 ID")
      @PathVariable Long id,

      @Parameter(description = "상태 업데이트 요청 정보 (completed: true/false)")
      @RequestBody WorkLogDTO.StatusUpdateRequest request,

      @Parameter(description = "클라이언트/디바이스 식별자")
      @RequestHeader(value = "X-Client-ID", required = false) String clientId
  ) {
    // 클라이언트 ID가 없으면 "unknown"으로 기본값 설정
    String actualClientId = (clientId != null && !clientId.isEmpty()) ? clientId : "unknown";

    log.debug("작업계획 상태 변경 요청: ID={}, 완료={}, 클라이언트={}", id, request.isCompleted(), actualClientId);

    boolean updated = workLogService.updateWorkLogCompletionStatus(id, request.isCompleted(), actualClientId);

    Map<String, Object> response = new HashMap<>();
    response.put("success", updated);

    if (updated) {
      String message = request.isCompleted() ?
          "작업이 완료 상태로 변경되었습니다." : "작업이 미완료 상태로 변경되었습니다.";
      response.put("message", message);
      response.put("clientId", actualClientId); // 응답에 클라이언트 ID 포함 (디버깅용)
      return ResponseEntity.ok(response);
    } else {
      response.put("message", "작업계획 상태 변경에 실패했습니다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 작업계획 삭제
   * 지정된 ID의 작업계획를 삭제합니다.
   *
   * @param id 삭제할 작업계획 ID
   * @return 삭제 결과 메시지
   */
  @Operation(
      summary = "작업계획 삭제",
      description = "지정된 ID의 작업계획를 삭제합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "작업계획 삭제 성공",
          content = @Content(schema = @Schema(implementation = Map.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "작업계획 삭제 실패",
          content = @Content(schema = @Schema(implementation = Map.class))
      )
  })
  @DeleteMapping("/worklogs/{id}")
  public ResponseEntity<Map<String, Object>> deleteWorkLog(
      @Parameter(description = "삭제할 작업계획 ID")
      @PathVariable Long id
  ) {
    boolean deleted = workLogService.deleteWorkLog(id);

    Map<String, Object> response = new HashMap<>();
    response.put("success", deleted);

    if (deleted) {
      response.put("message", "작업계획가 성공적으로 삭제되었습니다.");
      return ResponseEntity.ok(response);
    } else {
      response.put("message", "작업계획 삭제에 실패했습니다.");
      return ResponseEntity.badRequest().body(response);
    }
  }
}