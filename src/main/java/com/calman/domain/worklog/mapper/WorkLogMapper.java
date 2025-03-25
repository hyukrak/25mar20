package com.calman.domain.worklog.mapper;

import com.calman.domain.worklog.dto.WorkLogDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 작업계획 매퍼 인터페이스
 */
@Mapper
public interface WorkLogMapper {
  /**
   * 새 작업계획 생성
   * @param workLog 작업계획 정보
   * @return 영향받은 행 수
   */
  int insertWorkLog(WorkLogDTO workLog);

  /**
   * ID로 작업계획 조회
   * @param id 작업계획 ID
   * @return 작업계획 정보
   */
  WorkLogDTO selectWorkLogById(@Param("id") Long id);

  /**
   * 필터링으로 작업계획 목록 조회
   * @param params 검색 조건 (carModel, productCode, productColor, productName, status, startDate, endDate, sortField, sortDirection 등)
   * @return 작업계획 목록
   */
  List<WorkLogDTO> selectWorkLogs(Map<String, Object> params);

  /**
   * 작업계획 업데이트
   * @param workLog 수정할 정보
   * @return 영향받은 행 수
   */
  int updateWorkLog(WorkLogDTO workLog);

  /**
   * 작업계획 소프트 삭제 (isDeleted 플래그 설정)
   * @param id 삭제할 작업계획 ID
   * @return 영향받은 행 수
   */
  int deleteWorkLog(@Param("id") Long id);

  /**
   * 날짜 범위로 작업계획 조회
   * @param startDate 시작 날짜
   * @param endDate 종료 날짜
   * @return 작업계획 목록
   */
  List<WorkLogDTO> selectWorkLogsByDateRange(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate
  );

  /**
   * 정확한 날짜로 작업계획 조회
   * @param exactDate 조회할 날짜
   * @return 작업계획 목록
   */
  List<WorkLogDTO> selectWorkLogsByExactDate(@Param("exactDate") LocalDate exactDate);

  /**
   * 차량 모델로 작업계획 조회
   * @param carModel 차량 모델명
   * @return 작업계획 목록
   */
  List<WorkLogDTO> selectWorkLogsByCarModel(@Param("carModel") String carModel);

  /**
   * 제품 코드로 작업계획 조회
   * @param productCode 제품 코드
   * @return 작업계획 목록
   */
  List<WorkLogDTO> selectWorkLogsByProductCode(@Param("productCode") String productCode);

  /**
   * 상태별 작업계획 조회
   * @param status 상태 (completed, incomplete)
   * @return 작업계획 목록
   */
  List<WorkLogDTO> selectWorkLogsByStatus(@Param("status") String status);

  /**
   * 작업자 ID로 작업계획 조회
   * @param userId 작업자 ID
   * @return 작업계획 목록
   */
  List<WorkLogDTO> selectWorkLogsByUserId(@Param("userId") Long userId);

  /**
   * 작업계획 완료 상태 업데이트 (클라이언트 ID 포함)
   * @param id 작업계획 ID
   * @param completedAt 완료 시간 (미완료인 경우 null)
   * @param completedBy 완료 처리한 클라이언트/디바이스 ID
   * @return 영향받은 행 수
   */
  int updateWorkLogCompletionStatusWithBy(
      @Param("id") Long id,
      @Param("completedAt") LocalDateTime completedAt,
      @Param("completedBy") String completedBy
  );

  /**
   * 작업계획 완료 상태 업데이트
   * @param id 작업계획 ID
   * @param completedAt 완료 시간 (미완료인 경우 null)
   * @return 영향받은 행 수
   */
  int updateWorkLogCompletionStatus(
      @Param("id") Long id,
      @Param("completedAt") LocalDateTime completedAt
  );
}