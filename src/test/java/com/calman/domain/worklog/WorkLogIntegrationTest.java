package com.calman.domain.worklog;

import com.calman.domain.worklog.dto.WorkLogDTO;
import com.calman.global.config.TestSQLiteConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 작업계획 통합 테스트 (SQLite 사용)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSQLiteConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkLogIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  // 테스트 간 공유 변수
  private static Long createdWorkLogId;

  @Test
  @Order(1)
  @DisplayName("1. 작업계획 생성 테스트")
  public void testCreateWorkLog() throws Exception {
    // given
    WorkLogDTO.CreateRequest request = new WorkLogDTO.CreateRequest();
    request.setWorkDatetime(LocalDateTime.now());
    request.setCarModel("Integration Test Model");
    request.setMaterialCode("IT-1000");
    request.setQuantity(5);

    // when & then
    MvcResult result = mockMvc.perform(post("/api/worklogs")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andReturn();

    // 생성된 ID 저장
    String responseJson = result.getResponse().getContentAsString();
    createdWorkLogId = objectMapper.readTree(responseJson).get("id").asLong();

    // 생성된 ID 확인
    assertThat(createdWorkLogId).isNotNull();
    assertThat(createdWorkLogId).isGreaterThan(0);
  }

  @Test
  @Order(2)
  @DisplayName("2. 생성된 작업계획 조회 테스트")
  public void testGetCreatedWorkLog() throws Exception {
    // 이전 테스트에서 생성된 ID가 있는지 확인
    assertThat(createdWorkLogId).isNotNull();

    // when & then
    mockMvc.perform(get("/api/worklogs/{id}", createdWorkLogId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(createdWorkLogId))
        .andExpect(jsonPath("$.carModel").value("Integration Test Model"))
        .andExpect(jsonPath("$.materialCode").value("IT-1000"))
        .andExpect(jsonPath("$.quantity").value(5));
  }

  @Test
  @Order(3)
  @DisplayName("3. 작업계획 목록 조회 테스트")
  public void testGetWorkLogs() throws Exception {
    // when & then
    mockMvc.perform(get("/api/worklogs")
            .param("page", "1")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.workLogs").isArray())
        .andExpect(jsonPath("$.totalCount").isNumber())
        .andExpect(jsonPath("$.totalPages").isNumber())
        .andExpect(jsonPath("$.currentPage").value(1));
  }

  @Test
  @Order(4)
  @DisplayName("4. 특정 차량 모델로 작업계획 필터링 테스트")
  public void testFilterWorkLogsByCarModel() throws Exception {
    // when & then
    mockMvc.perform(get("/api/worklogs")
            .param("carModel", "Integration Test Model")
            .param("page", "1")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.workLogs").isArray())
        .andExpect(jsonPath("$.workLogs[0].carModel").value("Integration Test Model"));
  }

  @Test
  @Order(5)
  @DisplayName("5. 작업계획 업데이트 테스트")
  public void testUpdateWorkLog() throws Exception {
    // given
    WorkLogDTO.UpdateRequest request = new WorkLogDTO.UpdateRequest();
    request.setWorkDatetime(LocalDateTime.now());
    request.setCarModel("Updated Integration Test Model");
    request.setMaterialCode("IT-2000");
    request.setQuantity(10);

    // when & then
    mockMvc.perform(put("/api/worklogs/{id}", createdWorkLogId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    // 업데이트 확인
    mockMvc.perform(get("/api/worklogs/{id}", createdWorkLogId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.carModel").value("Updated Integration Test Model"))
        .andExpect(jsonPath("$.materialCode").value("IT-2000"))
        .andExpect(jsonPath("$.quantity").value(10));
  }

  @Test
  @Order(6)
  @DisplayName("6. 날짜 범위로 작업계획 필터링 테스트")
  public void testFilterWorkLogsByDateRange() throws Exception {
    // given
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startDate = now.minusDays(1);
    LocalDateTime endDate = now.plusDays(1);

    String startDateStr = startDate.format(DateTimeFormatter.ISO_DATE_TIME);
    String endDateStr = endDate.format(DateTimeFormatter.ISO_DATE_TIME);

    // when & then
    mockMvc.perform(get("/api/worklogs/by-date-range")
            .param("startDate", startDateStr)
            .param("endDate", endDateStr))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  @Order(7)
  @DisplayName("7. 작업계획 삭제 테스트")
  public void testDeleteWorkLog() throws Exception {
    // when
    mockMvc.perform(delete("/api/worklogs/{id}", createdWorkLogId))
        .andExpect(status().isNoContent());

    // 삭제 확인 (404 응답 예상)
    mockMvc.perform(get("/api/worklogs/{id}", createdWorkLogId))
        .andExpect(status().isNotFound());
  }
}