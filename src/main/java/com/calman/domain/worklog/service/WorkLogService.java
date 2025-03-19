package com.calman.domain.worklog.service;

import com.calman.domain.worklog.mapper.WorkLogMapper;
import com.calman.domain.worklog.dto.WorkLogDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 작업 로그 서비스
 */
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
    WorkLogDTO workLog = WorkLogDTO.builder()
        .workDatetime(request.getWorkDatetime())
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
        workLog.getCreatedAt()
    );
  }


  /**
   * 필터링으로 작업 로그 목록 조회 (페이징 제거)
   * @param carModel 차량 모델 필터
   * @param productCode 제품 코드 필터
   * @param status 상태 필터
   * @param startDate 시작 날짜 필터
   * @param endDate 종료 날짜 필터
   * @param sortField 정렬 필드
   * @param sortDirection 정렬 방향
   * @return 작업 로그 목록
   */
  public Map<String, Object> getWorkLogs(
      String carModel,
      String productCode,
      String status,
      LocalDateTime startDate,
      LocalDateTime endDate,
      Integer pageNumber,
      Integer pageSize,
      String sortField,
      String sortDirection) {

    Map<String, Object> params = new HashMap<>();
    params.put("carModel", carModel);
    params.put("productCode", productCode);
    params.put("status", status);
    params.put("startDate", startDate);
    params.put("endDate", endDate);
    params.put("sortField", sortField);
    params.put("sortDirection", sortDirection != null ? sortDirection : "DESC");

    List<WorkLogDTO> workLogs = workLogMapper.selectWorkLogs(params);
    int totalCount = workLogs.size();

    Map<String, Object> result = new HashMap<>();
    result.put("workLogs", workLogs);
    result.put("totalCount", totalCount);

    return result;
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

    // 필드 업데이트
    existingWorkLog.setWorkDatetime(request.getWorkDatetime());
    existingWorkLog.setCarModel(request.getCarModel());
    existingWorkLog.setProductColor(request.getProductColor());
    existingWorkLog.setProductCode(request.getProductCode());
    existingWorkLog.setProductName(request.getProductName());
    existingWorkLog.setQuantity(request.getQuantity());

    return workLogMapper.updateWorkLog(existingWorkLog) > 0;
  }

  /**
   * 작업 로그 삭제 (소프트 삭제)
   * @param id 삭제할 작업 로그 ID
   * @return 성공 여부
   */
  @Transactional
  public boolean deleteWorkLog(Long id) {
    return workLogMapper.deleteWorkLog(id) > 0;
  }

  /**
   * 날짜 범위로 작업 로그 조회
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
   * @param status 상태
   * @return 작업 로그 목록
   */
  public List<WorkLogDTO> getWorkLogsByStatus(String status) {
    return workLogMapper.selectWorkLogsByStatus(status);
  }

  /**
   * 작업자 ID로 작업 로그 조회
   * @param userId 작업자 ID
   * @return 작업 로그 목록
   */
  public List<WorkLogDTO> getWorkLogsByUserId(Long userId) {
    return workLogMapper.selectWorkLogsByUserId(userId);
  }
}