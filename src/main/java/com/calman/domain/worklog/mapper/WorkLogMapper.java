package com.calman.domain.worklog.mapper;

import com.calman.domain.worklog.dto.WorkLogDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 작업 로그 매퍼 인터페이스
 */
@Mapper
public interface WorkLogMapper {
  /**
   * 새 작업 로그 생성
   * @param workLog 작업 로그 정보
   * @return 영향받은 행 수
   */
  int insertWorkLog(WorkLogDTO workLog);

  /**
   * ID로 작업 로그 조회
   * @param id 작업 로그 ID
   * @return 작업 로그 정보
   */
  WorkLogDTO selectWorkLogById(@Param("id") Long id);

  /**
   * 페이징 및 필터링으로 작업 로그 목록 조회
   * @param params 검색 조건 (carModel, materialCode, status, startDate, endDate, pageSize, pageNumber 등)
   * @return 작업 로그 목록
   */
  List<WorkLogDTO> selectWorkLogs(Map<String, Object> params);

  /**
   * 페이징을 위한 총 작업 로그 수 조회
   * @param params 검색 조건
   * @return 작업 로그 총 개수
   */
  int countWorkLogs(Map<String, Object> params);

  /**
   * 작업 로그 업데이트
   * @param workLog 수정할 정보
   * @return 영향받은
   */
  int updateWorkLog(WorkLogDTO workLog);

  /**
   * 작업 로그 소프트 삭제 (isDeleted 플래그 설정)
   * @param id 삭제할 작업 로그 ID
   * @return 영향받은 행 수
   */
  int deleteWorkLog(@Param("id") Long id);

  /**
   * 날짜 범위로 작업 로그 조회
   * @param startDate 시작 날짜
   * @param endDate 종료 날짜
   * @return 작업 로그 목록
   */
  List<WorkLogDTO> selectWorkLogsByDateRange(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate
  );

  /**
   * 차량 모델로 작업 로그 조회
   * @param carModel 차량 모델명
   * @return 작업 로그 목록
   */
  List<WorkLogDTO> selectWorkLogsByCarModel(@Param("carModel") String carModel);

  /**
   * 자재 코드로 작업 로그 조회
   * @param materialCode 자재 코드
   * @return 작업 로그 목록
   */
  List<WorkLogDTO> selectWorkLogsByMaterialCode(@Param("materialCode") String materialCode);

  /**
   * 상태별 작업 로그 조회
   * @param status 상태 (pending, in_progress, completed, rejected 등)
   * @return 작업 로그 목록
   */
  List<WorkLogDTO> selectWorkLogsByStatus(@Param("status") String status);

  /**
   * 작업자 ID로 작업 로그 조회
   * @param userId 작업자 ID
   * @return 작업 로그 목록
   */
  List<WorkLogDTO> selectWorkLogsByUserId(@Param("userId") Long userId);
}