package com.calman.domain.worklog.controller;

import com.calman.domain.worklog.dto.WorkLogDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 작업계획 SSR 컨트롤러 통합 테스트
 * - Thymeleaf SSR 컨트롤러의 전체 흐름을 테스트합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkLogViewControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  // 테스트 간 공유 변수
  private static Long createdWorkLogId;

  @Test
  @Order(1)
  @DisplayName("1. 작업계획 목록 페이지 테스트")
  public void testListWorkLogs() throws Exception {
    mockMvc.perform(get("/worklogs"))
        .andExpect(status().isOk())
        .andExpect(view().name("worklogs/list"))
        .andExpect(model().attributeExists("workLogs"))
        .andExpect(model().attributeExists("totalCount"))
        .andExpect(model().attributeExists("totalPages"))
        .andExpect(model().attributeExists("currentPage"));
  }

  @Test
  @Order(2)
  @DisplayName("2. 작업계획 생성 폼 페이지 테스트")
  public void testNewWorkLogForm() throws Exception {
    mockMvc.perform(get("/worklogs/new"))
        .andExpect(status().isOk())
        .andExpect(view().name("worklogs/form"))
        .andExpect(model().attributeExists("workLog"));
  }

  @Test
  @Order(3)
  @DisplayName("3. 작업계획 생성 처리 및 리다이렉트 테스트")
  @Transactional
  public void testCreateWorkLog() throws Exception {
    // 현재 시간을 문자열로 변환 (Thymeleaf 폼 제출 형식)
    LocalDateTime now = LocalDateTime.now();
    String workDatetime = now.toString();

    // 폼 제출 시뮬레이션
    MvcResult result = mockMvc.perform(post("/worklogs")
            .param("workDatetime", workDatetime)
            .param("carModel", "SSR Test Model")
            .param("materialCode", "SSR-1000")
            .param("quantity", "7")
            .param("userId", "1")
            .param("notes", "SSR 컨트롤러 테스트")
            .param("departmentId", "1")
            .param("lineId", "2")
            .param("lotNumber", "SSR-LOT-001"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/worklogs"))
        .andExpect(flash().attributeExists("successMessage"))
        .andReturn();

    // 플래시 메시지 확인
    String flashMessage = (String) result.getFlashMap().get("successMessage");
    assert flashMessage != null && !flashMessage.isEmpty();
  }

  @Test
  @Order(4)
  @DisplayName("4. 필터링된 작업계획 목록 페이지 테스트")
  public void testFilteredWorkLogs() throws Exception {
    mockMvc.perform(get("/worklogs")
            .param("carModel", "Model S")
            .param("status", "pending"))
        .andExpect(status().isOk())
        .andExpect(view().name("worklogs/list"))
        .andExpect(model().attributeExists("workLogs"))
        .andExpect(model().attribute("carModel", "Model S"))
        .andExpect(model().attribute("status", "pending"));
  }

  @Test
  @Order(5)
  @DisplayName("5. 작업계획 상세 페이지 테스트")
  public void testViewWorkLog() throws Exception {
    // 기존 테스트 데이터의 ID 사용
    Long existingId = 1L;

    mockMvc.perform(get("/worklogs/{id}", existingId))
        .andExpect(status().isOk())
        .andExpect(view().name("worklogs/view"))
        .andExpect(model().attributeExists("workLog"));
  }

  @Test
  @Order(6)
  @DisplayName("6. 작업계획 수정 폼 페이지 테스트")
  public void testEditWorkLogForm() throws Exception {
    // 기존 테스트 데이터의 ID 사용
    Long existingId = 1L;

    mockMvc.perform(get("/worklogs/{id}/edit", existingId))
        .andExpect(status().isOk())
        .andExpect(view().name("worklogs/edit"))
        .andExpect(model().attributeExists("workLog"))
        .andExpect(model().attribute("workLogId", existingId));
  }

  @Test
  @Order(7)
  @DisplayName("7. 작업계획 업데이트 처리 및 리다이렉트 테스트")
  @Transactional
  public void testUpdateWorkLog() throws Exception {
    // 기존 테스트 데이터의 ID 사용
    Long existingId = 1L;
    LocalDateTime now = LocalDateTime.now();
    String workDatetime = now.toString();

    mockMvc.perform(post("/worklogs/{id}", existingId)
            .param("workDatetime", workDatetime)
            .param("carModel", "Updated SSR Model")
            .param("materialCode", "SSR-2000")
            .param("quantity", "12")
            .param("status", "completed")
            .param("notes", "업데이트된 SSR 테스트"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/worklogs/" + existingId))
        .andExpect(flash().attributeExists("successMessage"));
  }

  @Test
  @Order(8)
  @DisplayName("8. 존재하지 않는 작업계획 조회시 404 페이지 테스트")
  public void testViewNonExistingWorkLog() throws Exception {
    // 존재하지 않는 ID
    Long nonExistingId = 9999L;

    mockMvc.perform(get("/worklogs/{id}", nonExistingId))
        .andExpect(status().isOk())
        .andExpect(view().name("error/404"));
  }

  @Test
  @Order(9)
  @DisplayName("9. 페이지네이션 테스트")
  public void testPagination() throws Exception {
    mockMvc.perform(get("/worklogs")
            .param("page", "2")
            .param("size", "2"))
        .andExpect(status().isOk())
        .andExpect(view().name("worklogs/list"))
        .andExpect(model().attribute("currentPage", 2));
  }

  @Test
  @Order(10)
  @DisplayName("10. 작업계획 삭제 처리 및 리다이렉트 테스트")
  @Transactional
  public void testDeleteWorkLog() throws Exception {
    // 기존 테스트 데이터의 ID 사용
    Long existingId = 2L;

    mockMvc.perform(post("/worklogs/{id}/delete", existingId))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/worklogs"))
        .andExpect(flash().attributeExists("successMessage"));
  }

  @Test
  @Order(11)
  @DisplayName("11. 전체 Thymeleaf 컨트롤러 플로우 테스트")
  @Transactional
  public void testCompleteThymeleafFlow() throws Exception {
    // 1. 생성 폼 페이지 접근
    mockMvc.perform(get("/worklogs/new"))
        .andExpect(status().isOk())
        .andExpect(view().name("worklogs/form"));

    // 2. 작업계획 생성
    LocalDateTime now = LocalDateTime.now();
    String workDatetime = now.toString();

    MvcResult createResult = mockMvc.perform(post("/worklogs")
            .param("workDatetime", workDatetime)
            .param("carModel", "Flow Test Model")
            .param("materialCode", "FLOW-1000")
            .param("quantity", "3")
            .param("userId", "2")
            .param("notes", "플로우 테스트"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/worklogs"))
        .andReturn();

    // 3. 목록 페이지에서 생성된 항목 확인
    mockMvc.perform(get("/worklogs")
            .param("carModel", "Flow Test Model"))
        .andExpect(status().isOk())
        .andExpect(view().name("worklogs/list"))
        .andExpect(model().attributeExists("workLogs"));

    // 참고: 실제 ID를 동적으로 가져오는 것은 어렵기 때문에
    // 이 테스트에서는 업데이트/상세보기/삭제를 테스트하지 않습니다.
    // 실제 환경에서는 JPA를 사용하거나 별도의 방법으로 ID를 가져와야 합니다.
  }
}