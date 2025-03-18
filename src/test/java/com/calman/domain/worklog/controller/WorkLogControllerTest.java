package com.calman.domain.worklog.controller;

import com.calman.domain.worklog.dto.WorkLogDTO;
import com.calman.domain.worklog.service.WorkLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 작업 로그 Thymeleaf 컨트롤러 테스트
 */
@WebMvcTest(controllers = WorkLogController.class)
public class WorkLogControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private WorkLogService workLogService;

  @Test
  @DisplayName("작업 로그 목록 페이지 테스트")
  public void testListWorkLogs() throws Exception {
    // given
    List<WorkLogDTO> mockWorkLogs = Arrays.asList(
        createMockWorkLog(1L, "Model A", "pending"),
        createMockWorkLog(2L, "Model B", "completed")
    );

    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("workLogs", mockWorkLogs);
    responseMap.put("totalCount", 2);
    responseMap.put("totalPages", 1);
    responseMap.put("currentPage", 1);

    given(workLogService.getWorkLogs(
        anyString(), anyString(), anyString(),
        any(), any(), anyInt(), anyInt(),
        anyString(), anyString()))
        .willReturn(responseMap);

    // when & then
    mockMvc.perform(get("/worklogs"))
        .andExpect(status().isOk())
        .andExpect(view().name("worklogs/list"))
        .andExpect(model().attribute("workLogs", mockWorkLogs))
        .andExpect(model().attribute("totalCount", 2))
        .andExpect(model().attribute("totalPages", 1))
        .andExpect(model().attribute("currentPage", 1));
  }

  @Test
  @DisplayName("작업 로그 생성 폼 페이지 테스트")
  public void testNewWorkLogForm() throws Exception {
    // when & then
    mockMvc.perform(get("/worklogs/new"))
        .andExpect(status().isOk())
        .andExpect(view().name("worklogs/form"))
        .andExpect(model().attributeExists("workLog"));
  }

  @Test
  @DisplayName("작업 로그 상세 조회 페이지 테스트")
  public void testViewWorkLog() throws Exception {
    // given
    Long id = 1L;
    WorkLogDTO.DetailResponse mockResponse = new WorkLogDTO.DetailResponse();
    mockResponse.setId(id);
    mockResponse.setCarModel("Test Model");
    mockResponse.setMaterialCode("T-1000");

    given(workLogService.getWorkLogDetailById(id)).willReturn(mockResponse);

    // when & then
    mockMvc.perform(get("/worklogs/{id}", id))
        .andExpect(status().isOk())
        .andExpect(view().name("worklogs/view"))
        .andExpect(model().attribute("workLog", mockResponse));
  }

  @Test
  @DisplayName("존재하지 않는 작업 로그 조회 시 404 페이지 테스트")
  public void testViewNonExistingWorkLog() throws Exception {
    // given
    Long id = 999L;
    given(workLogService.getWorkLogDetailById(id)).willReturn(null);

    // when & then
    mockMvc.perform(get("/worklogs/{id}", id))
        .andExpect(status().isOk())
        .andExpect(view().name("error/404"));
  }

  @Test
  @DisplayName("작업 로그 수정 폼 페이지 테스트")
  public void testEditWorkLogForm() throws Exception {
    // given
    Long id = 1L;
    WorkLogDTO mockWorkLog = new WorkLogDTO();
    mockWorkLog.setId(id);
    mockWorkLog.setWorkDatetime(LocalDateTime.now());
    mockWorkLog.setCarModel("Test Model");
    mockWorkLog.setMaterialCode("T-1000");

    given(workLogService.getWorkLogById(id)).willReturn(mockWorkLog);

    // when & then
    mockMvc.perform(get("/worklogs/{id}/edit", id))
        .andExpect(status().isOk())
        .andExpect(view().name("worklogs/edit"))
        .andExpect(model().attributeExists("workLog"))
        .andExpect(model().attribute("workLogId", id));
  }

  @Test
  @DisplayName("작업 로그 생성 처리 테스트")
  public void testCreateWorkLog() throws Exception {
    // given
    given(workLogService.createWorkLog(any(WorkLogDTO.CreateRequest.class))).willReturn(1L);

    // when & then
    mockMvc.perform(post("/worklogs")
            .param("workDatetime", "2024-03-18T10:00:00")
            .param("carModel", "Test Model")
            .param("materialCode", "T-1000")
            .param("quantity", "10")
            .param("userId", "1"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/worklogs"))
        .andExpect(flash().attributeExists("successMessage"));

    verify(workLogService).createWorkLog(any(WorkLogDTO.CreateRequest.class));
  }

  @Test
  @DisplayName("작업 로그 업데이트 처리 테스트")
  public void testUpdateWorkLog() throws Exception {
    // given
    Long id = 1L;
    given(workLogService.updateWorkLog(eq(id), any(WorkLogDTO.UpdateRequest.class))).willReturn(true);

    // when & then
    mockMvc.perform(post("/worklogs/{id}", id)
            .param("workDatetime", "2024-03-18T10:00:00")
            .param("carModel", "Updated Model")
            .param("materialCode", "T-1000")
            .param("quantity", "20")
            .param("status", "completed"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/worklogs/" + id))
        .andExpect(flash().attributeExists("successMessage"));

    verify(workLogService).updateWorkLog(eq(id), any(WorkLogDTO.UpdateRequest.class));
  }

  @Test
  @DisplayName("작업 로그 삭제 처리 테스트")
  public void testDeleteWorkLog() throws Exception {
    // given
    Long id = 1L;
    given(workLogService.deleteWorkLog(id)).willReturn(true);

    // when & then
    mockMvc.perform(post("/worklogs/{id}/delete", id))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/worklogs"))
        .andExpect(flash().attributeExists("successMessage"));

    verify(workLogService).deleteWorkLog(id);
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