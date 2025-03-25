package com.calman.domain.worklog.service;

import com.calman.DateTimeUtils;
import com.calman.domain.worklog.dto.WorkLogDTO;
import com.calman.domain.worklog.mapper.WorkLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 작업 로그 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkLogService {

  private final WorkLogMapper workLogMapper;

  /**
   * 새 작업 로그 생성
   * @param request 작업 로그 생성 요청 정보
   * @return 생성된 작업 로그 ID
   */
  @Transactional
  public Long createWorkLog(WorkLogDTO.CreateRequest request) {
    // 문자열 날짜를 LocalDateTime으로 변환
    LocalDateTime workDateTime = request.getWorkDatetimeAsLocalDateTime();

    if (workDateTime == null) {
      log.warn("날짜 변환 실패: {}", request.getWorkDatetime());
      // 변환 실패 시 현재 시간 사용 또는 오류 처리 결정
      workDateTime = LocalDateTime.now();
    }

    WorkLogDTO workLog = WorkLogDTO.builder()
        .workDatetime(workDateTime)  // LocalDateTime 사용
        .carModel(request.getCarModel())
        .productColor(request.getProductColor())
        .productCode(request.getProductCode())
        .productName(request.getProductName())
        .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
        .build();

    workLogMapper.insertWorkLog(workLog);
    return workLog.getId();
  }

  /**
   * ID로 작업 로그 조회
   * @param id 작업 로그 ID
   * @return 작업 로그 정보
   */
  public WorkLogDTO getWorkLogById(Long id) {
    return workLogMapper.selectWorkLogById(id);
  }

  /**
   * 상세 응답용 작업 로그 조회
   * @param id 작업 로그 ID
   * @return 상세 정보가 포함된 작업 로그 응답 DTO
   */
  public WorkLogDTO.DetailResponse getWorkLogDetailById(Long id) {
    WorkLogDTO workLog = workLogMapper.selectWorkLogById(id);
    if (workLog == null) {
      return null;
    }

    return new WorkLogDTO.DetailResponse(
        workLog.getId(),
        workLog.getWorkDatetime(),
        workLog.getCarModel(),
        workLog.getProductColor(),
        workLog.getProductCode(),
        workLog.getProductName(),
        workLog.getQuantity(),
        workLog.getCompletedAt(),
        workLog.getCreatedAt()
    );
  }

  /**
   * 필터링으로 작업 로그 목록 조회
   */
  public Map<String, Object> getWorkLogs(
      String carModel,
      String productCode,
      String status,
      LocalDateTime startDate,
      LocalDateTime endDate,
      String sortField,
      String sortDirection) {

    // 허용된 정렬 필드만 처리
    if (sortField != null && !Arrays.asList(
        "wl_work_datetime", "wl_car_model", "wl_product_color", "wl_product_code",
        "wl_product_name", "wl_quantity", "wl_created_at", "wl_completed_at").contains(sortField)) {
      sortField = "wl_work_datetime"; // 기본값
    }

    // 정렬 방향 기본값을 ASC로 설정
    if (sortDirection == null) {
      sortDirection = "ASC";
    }

    Map<String, Object> params = new HashMap<>();
    params.put("carModel", carModel);
    params.put("productCode", productCode);
    params.put("status", status);
    params.put("startDate", startDate);
    params.put("endDate", endDate);
    params.put("sortField", sortField);
    params.put("sortDirection", sortDirection);

    List<WorkLogDTO> workLogs = workLogMapper.selectWorkLogs(params);

    Map<String, Object> result = new HashMap<>();
    result.put("workLogs", workLogs);
    result.put("totalCount", workLogs.size());

    return result;
  }

  /**
   * 특정 날짜의 작업 로그 목록 조회
   * @param date 조회할 날짜
   * @param status 상태 필터 ('completed', 'incomplete', null)
   * @param sortField 정렬 필드
   * @param sortDirection 정렬 방향 ('ASC' 또는 'DESC')
   * @return 해당 날짜의 작업 로그 목록
   */
  public Map<String, Object> getWorkLogsByExactDate(LocalDate date, String status, String sortField, String sortDirection) {
    // 날짜 검증
    LocalDate validDate = date;
    if (validDate == null) {
      validDate = LocalDate.now();
      log.warn("서비스에서 날짜가 null입니다. 현재 날짜로 대체: {}", validDate);
    }

    // 날짜 범위 설정
    LocalDateTime[] range = DateTimeUtils.getDateTimeRange(validDate);

    // 해당 날짜의 시작과 끝 시간으로 조회 (상태, 정렬 필드와 방향 사용)
    return getWorkLogs(null, null, status, range[0], range[1], sortField, sortDirection);
  }

  // 기존 메소드도 오버로드하여 이전 코드와의 호환성 유지
  public Map<String, Object> getWorkLogsByExactDate(LocalDate date, String sortField, String sortDirection) {
    // 상태 필터 없이 호출 (null)
    return getWorkLogsByExactDate(date, null, sortField, sortDirection);
  }

  // 기존 메소드도 오버로드하여 이전 코드와의 호환성 유지
  public Map<String, Object> getWorkLogsByExactDate(LocalDate date) {
    // 기본값으로 작업시간 기준 오름차순 정렬, 상태 필터 없음
    return getWorkLogsByExactDate(date, null, "wl_work_datetime", "ASC");
  }

  /**
   * 작업 로그 업데이트
   * @param id 업데이트할 작업 로그 ID
   * @param request 업데이트 요청 정보
   * @return 성공 여부
   */
  @Transactional
  public boolean updateWorkLog(Long id, WorkLogDTO.UpdateRequest request) {
    WorkLogDTO existingWorkLog = workLogMapper.selectWorkLogById(id);
    if (existingWorkLog == null) {
      return false;
    }

    // 문자열 날짜를 LocalDateTime으로 변환
    LocalDateTime workDateTime = request.getWorkDatetimeAsLocalDateTime();

    if (workDateTime == null) {
      log.warn("날짜 변환 실패: {}", request.getWorkDatetime());
      // 변환 실패 시 기존 값 유지
      workDateTime = existingWorkLog.getWorkDatetime();
    }

    // 필드 업데이트
    existingWorkLog.setWorkDatetime(workDateTime);
    existingWorkLog.setCarModel(request.getCarModel());
    existingWorkLog.setProductColor(request.getProductColor());
    existingWorkLog.setProductCode(request.getProductCode());
    existingWorkLog.setProductName(request.getProductName());
    existingWorkLog.setQuantity(request.getQuantity());

    return workLogMapper.updateWorkLog(existingWorkLog) > 0;
  }

  /**
   * 작업 완료 상태 업데이트
   * @param id 작업 로그 ID
   * @param completed 완료 여부
   * @return 성공 여부
   */
  @Transactional
  public boolean updateWorkLogCompletionStatus(Long id, boolean completed) {
    LocalDateTime completedAt = completed ? LocalDateTime.now() : null;
    return workLogMapper.updateWorkLogCompletionStatus(id, completedAt) > 0;
  }

  /**
   * 작업 로그 삭제
   * @param id 삭제할 작업 로그 ID
   * @return 성공 여부
   */
  @Transactional
  public boolean deleteWorkLog(Long id) {
    return workLogMapper.deleteWorkLog(id) > 0;
  }

  /**
   * 날짜 범위로 작업 로그 조회 (개선된 버전)
   * @param startDate 시작 날짜
   * @param endDate 종료 날짜
   * @return 작업 로그 목록
   */
  public List<WorkLogDTO> getWorkLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    return workLogMapper.selectWorkLogsByDateRange(startDate, endDate);
  }

  /**
   * 차량 모델로 작업 로그 조회
   * @param carModel 차량 모델명
   * @return 작업 로그 목록
   */
  public List<WorkLogDTO> getWorkLogsByCarModel(String carModel) {
    return workLogMapper.selectWorkLogsByCarModel(carModel);
  }

  /**
   * 제품 코드로 작업 로그 조회
   * @param productCode 제품 코드
   * @return 작업 로그 목록
   */
  public List<WorkLogDTO> getWorkLogsByProductCode(String productCode) {
    return workLogMapper.selectWorkLogsByProductCode(productCode);
  }

  /**
   * 상태별 작업 로그 조회
   * @param status 상태 (completed, incomplete)
   * @return 작업 로그 목록
   */
  public List<WorkLogDTO> getWorkLogsByStatus(String status) {
    return workLogMapper.selectWorkLogsByStatus(status);
  }
}