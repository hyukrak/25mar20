package com.calman.domain.worklog.mapper;

import com.calman.domain.worklog.dto.WorkLogDTO;
import com.calman.global.config.TestSQLiteConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 작업 로그 매퍼 테스트 (SQLite 사용)
 */
@MybatisTest
@ActiveProfiles("test")
@Import(TestSQLiteConfig.class)
public class WorkLogMapperTest {

  @Autowired
  private WorkLogMapper workLogMapper;

  @Test
  @DisplayName("ID로 작업 로그 조회 테스트")
  public void testSelectWorkLogById() {
    // given
    Long id = 1L;

    // when
    WorkLogDTO workLog = workLogMapper.selectWorkLogById(id);

    // then
    assertThat(workLog).isNotNull();
    assertThat(workLog.getId()).isEqualTo(id);
    assertThat(workLog.getCarModel()).isEqualTo("Model S");
    assertThat(workLog.getMaterialCode()).isEqualTo("M-1001");
    assertThat(workLog.getQuantity()).isEqualTo(5);
  }

  @Test
  @DisplayName("작업 로그 목록 조회 테스트")
  public void testSelectWorkLogs() {
    // given
    Map<String, Object> params = new HashMap<>();
    params.put("pageNumber", 1);
    params.put("pageSize", 10);

    // when
    List<WorkLogDTO> workLogs = workLogMapper.selectWorkLogs(params);
    int count = workLogMapper.countWorkLogs(params);

    // then
    assertThat(workLogs).isNotNull();
    assertThat(workLogs).hasSizeGreaterThanOrEqualTo(1);
    assertThat(count).isGreaterThanOrEqualTo(1);
  }

  @Test
  @DisplayName("차량 모델별 작업 로그 필터링 테스트")
  public void testSelectWorkLogsByCarModel() {
    // given
    String carModel = "Model S";

    // when
    List<WorkLogDTO> workLogs = workLogMapper.selectWorkLogsByCarModel(carModel);

    // then
    assertThat(workLogs).isNotNull();
    assertThat(workLogs).hasSizeGreaterThanOrEqualTo(1);
    assertThat(workLogs.get(0).getCarModel()).isEqualTo(carModel);
  }

  @Test
  @DisplayName("날짜 범위별 작업 로그 필터링 테스트")
  public void testSelectWorkLogsByDateRange() {
    // given
    LocalDateTime startDate = LocalDateTime.of(2024, 3, 1, 0, 0);
    LocalDateTime endDate = LocalDateTime.of(2024, 3, 31, 23, 59);

    // when
    List<WorkLogDTO> workLogs = workLogMapper.selectWorkLogsByDateRange(startDate, endDate);

    // then
    assertThat(workLogs).isNotNull();
    assertThat(workLogs).hasSizeGreaterThanOrEqualTo(1);
    // 모든 날짜가 범위 내에 있는지 확인
    assertThat(workLogs).allMatch(workLog ->
        !workLog.getWorkDatetime().isBefore(startDate) &&
            !workLog.getWorkDatetime().isAfter(endDate)
    );
  }

  @Test
  @DisplayName("작업 로그 생성 테스트")
  public void testInsertWorkLog() {
    // given
    WorkLogDTO workLog = new WorkLogDTO();
    workLog.setWorkDatetime(LocalDateTime.now());
    workLog.setCarModel("Test Model");
    workLog.setMaterialCode("T-1000");
    workLog.setQuantity(10);

    // when
    int result = workLogMapper.insertWorkLog(workLog);
    WorkLogDTO inserted = workLogMapper.selectWorkLogById(workLog.getId());

    // then
    assertThat(result).isEqualTo(1);
    assertThat(inserted).isNotNull();
    assertThat(inserted.getCarModel()).isEqualTo("Test Model");
    assertThat(inserted.getMaterialCode()).isEqualTo("T-1000");
  }

  @Test
  @DisplayName("작업 로그 업데이트 테스트")
  public void testUpdateWorkLog() {
    // given
    Long id = 2L; // 기존 테스트 데이터 ID
    WorkLogDTO workLog = workLogMapper.selectWorkLogById(id);
    assertThat(workLog).isNotNull();

    workLog.setQuantity(15);
    workLog.setCarModel("Updated Model");

    // when
    int result = workLogMapper.updateWorkLog(workLog);
    WorkLogDTO updated = workLogMapper.selectWorkLogById(id);

    // then
    assertThat(result).isEqualTo(1);
    assertThat(updated.getQuantity()).isEqualTo(15);
    assertThat(updated.getCarModel()).isEqualTo("Updated Model");
  }

  @Test
  @DisplayName("작업 로그 삭제 테스트")
  public void testDeleteWorkLog() {
    // given
    Long id = 3L; // 기존 테스트 데이터 ID
    WorkLogDTO original = workLogMapper.selectWorkLogById(id);
    assertThat(original).isNotNull(); // 삭제 전 확인

    // when
    int result = workLogMapper.deleteWorkLog(id);
    WorkLogDTO deleted = workLogMapper.selectWorkLogById(id);

    // then
    assertThat(result).isEqualTo(1);
    assertThat(deleted).isNull(); // 실제 삭제 확인
  }
}