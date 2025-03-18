package com.calman.domain.worklog.service;

import com.calman.domain.worklog.dto.WorkLogDTO;
import com.calman.domain.worklog.mapper.WorkLogMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * 작업 로그 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
public class WorkLogServiceTest {

  @Mock
  private WorkLogMapper workLogMapper;

  @InjectMocks
  private WorkLogService workLogService;

  @Test
  @DisplayName("새 작업 로그 생성 테스트")
  public void testCreateWorkLog() {
    // given
    WorkLogDTO.CreateRequest request = new WorkLogDTO.CreateRequest();
    request.setWorkDatetime(LocalDateTime.now());
    request.setCarModel("Test Model");
    request.setMaterialCode("T-1000");
    request.setQuantity(10);

    // Mockito에게 insertWorkLog가 호출될 때 1을 반환하도록 설정
    when(workLogMapper.insertWorkLog(any(WorkLogDTO.class))).thenAnswer(invocation -> {
      WorkLogDTO dto = invocation.getArgument(0);
      dto.setId(1L); // ID 설정
      return 1; // 영향받은 행 수
    });

    // when
    Long id = workLogService.createWorkLog(request);

    // then
    assertThat(id).isEqualTo(1L);
  }

  @Test
  @DisplayName("ID로 작업 로그 조회 테스트")
  public void testGetWorkLogById() {
    // given
    Long id = 1L;
    WorkLogDTO mockWorkLog = new WorkLogDTO();
    mockWorkLog.setId(id);
    mockWorkLog.setCarModel("Test Model");
    mockWorkLog.setMaterialCode("T-1000");
    mockWorkLog.setQuantity(10);

    when(workLogMapper.selectWorkLogById(id)).thenReturn(mockWorkLog);

    // when
    WorkLogDTO result = workLogService.getWorkLogById(id);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getCarModel()).isEqualTo("Test Model");
  }

  @Test
  @DisplayName("ID로 작업 로그 상세 조회 테스트")
  public void testGetWorkLogDetailById() {
    // given
    Long id = 1L;
    WorkLogDTO mockWorkLog = new WorkLogDTO();
    mockWorkLog.setId(id);
    mockWorkLog.setWorkDatetime(LocalDateTime.now());
    mockWorkLog.setCarModel("Test Model");
    mockWorkLog.setMaterialCode("T-1000");
    mockWorkLog.setQuantity(10);

    when(workLogMapper.selectWorkLogById(id)).thenReturn(mockWorkLog);

    // when
    WorkLogDTO.DetailResponse result = workLogService.getWorkLogDetailById(id);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getCarModel()).isEqualTo("Test Model");
  }

  @Test
  @DisplayName("작업 로그 목록 조회 테스트")
  public void testGetWorkLogs() {
    // given
    List<WorkLogDTO> mockWorkLogs = Arrays.asList(
        createMockWorkLog(1L, "Model A", "pending"),
        createMockWorkLog(2L, "Model B", "completed")
    );

    when(workLogMapper.selectWorkLogs(anyMap())).thenReturn(mockWorkLogs);
    when(workLogMapper.countWorkLogs(anyMap())).thenReturn(2);

    // when
    Map<String, Object> result = workLogService.getWorkLogs(
        null, null, null, null, null, 1, 10, null, null);

    // then
    assertThat(result).isNotNull();
    assertThat(result.get("workLogs")).isEqualTo(mockWorkLogs);
    assertThat(result.get("totalCount")).isEqualTo(2);
    assertThat(result.get("totalPages")).isEqualTo(1);
  }

  @Test
  @DisplayName("작업 로그 업데이트 테스트")
  public void testUpdateWorkLog() {
    // given
    Long id = 1L;
    WorkLogDTO.UpdateRequest request = new WorkLogDTO.UpdateRequest();
    request.setWorkDatetime(LocalDateTime.now());
    request.setCarModel("Updated Model");
    request.setMaterialCode("Updated-Code");
    request.setQuantity(20);

    WorkLogDTO existingWorkLog = new WorkLogDTO();
    existingWorkLog.setId(id);
    existingWorkLog.setCarModel("Original Model");

    when(workLogMapper.selectWorkLogById(id)).thenReturn(existingWorkLog);
    when(workLogMapper.updateWorkLog(any(WorkLogDTO.class))).thenReturn(1);

    // when
    boolean result = workLogService.updateWorkLog(id, request);

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("존재하지 않는 작업 로그 업데이트 테스트")
  public void testUpdateNonExistingWorkLog() {
    // given
    Long id = 999L;
    WorkLogDTO.UpdateRequest request = new WorkLogDTO.UpdateRequest();

    when(workLogMapper.selectWorkLogById(id)).thenReturn(null);

    // when
    boolean result = workLogService.updateWorkLog(id, request);

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("작업 로그 삭제 테스트")
  public void testDeleteWorkLog() {
    // given
    Long id = 1L;
    when(workLogMapper.deleteWorkLog(id)).thenReturn(1);

    // when
    boolean result = workLogService.deleteWorkLog(id);

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("차량 모델별 작업 로그 조회 테스트")
  public void testGetWorkLogsByCarModel() {
    // given
    String carModel = "Model S";
    List<WorkLogDTO> mockWorkLogs = Arrays.asList(
        createMockWorkLog(1L, carModel, "pending"),
        createMockWorkLog(2L, carModel, "completed")
    );

    when(workLogMapper.selectWorkLogsByCarModel(carModel)).thenReturn(mockWorkLogs);

    // when
    List<WorkLogDTO> result = workLogService.getWorkLogsByCarModel(carModel);

    // then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getCarModel()).isEqualTo(carModel);
  }

  // 테스트용 WorkLogDTO 객체 생성 헬퍼 메서드
  private WorkLogDTO createMockWorkLog(Long id, String carModel, String status) {
    WorkLogDTO workLog = new WorkLogDTO();
    workLog.setId(id);
    workLog.setWorkDatetime(LocalDateTime.now());
    workLog.setCarModel(carModel);
    workLog.setMaterialCode("M-" + id);
    workLog.setQuantity(10);
    return workLog;
  }
}