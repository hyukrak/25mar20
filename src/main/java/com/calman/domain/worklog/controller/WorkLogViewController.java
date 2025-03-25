package com.calman.domain.worklog.controller;

import com.calman.domain.worklog.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 작업계획 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class WorkLogViewController {

  private final WorkLogService workLogService;

  /**
   * 작업계획 대시보드 페이지
   */
  @GetMapping("/worklogs")
  public String forwardToWorklogsPage() {
    return "forward:/worklogs.html";
  }

/* ##################################################### */
/* ############### DEPRECATED 2025.03.25 ############### */
/* ##################################################### */

//  /**
//   * 작업계획 목록 페이지
//   */
//  @GetMapping
//  public String listWorkLogs(
//      @RequestParam(required = false) String carModel,
//      @RequestParam(required = false) String productCode,
//      @RequestParam(required = false) String status,
//      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
//      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
//      @RequestParam(required = false) String sortField,
//      @RequestParam(required = false, defaultValue = "DESC") String sortDirection,
//      Model model
//  ) {
//    Map<String, Object> result = workLogService.getWorkLogs(
//        carModel, productCode, status, startDate, endDate,
//        sortField, sortDirection
//    );
//    model.addAttribute("workLogs", result.get("workLogs"));
//    model.addAttribute("totalCount", result.get("totalCount"));
//
//    // 상태 유지를 위해 필터 매개변수를 모델에 추가
//    model.addAttribute("carModel", carModel);
//    model.addAttribute("productCode", productCode);
//    model.addAttribute("status", status);
//    model.addAttribute("startDate", startDate);
//    model.addAttribute("endDate", endDate);
//    model.addAttribute("sortField", sortField);
//    model.addAttribute("sortDirection", sortDirection);
//
//    return "worklogs";
//  }
//
//  /**
//   * 특정 날짜의 작업계획만 조회
//   */
//  @GetMapping("/date/{date}")
//  public String listWorkLogsByDate(
//      @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
//      @RequestParam(required = false) String sortField,
//      @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
//      Model model
//  ) {
//    Map<String, Object> result = workLogService.getWorkLogsByExactDate(date);
//    model.addAttribute("workLogs", result.get("workLogs"));
//    model.addAttribute("totalCount", result.get("totalCount"));
//    model.addAttribute("selectedDate", date);
//    model.addAttribute("sortField", sortField);
//    model.addAttribute("sortDirection", sortDirection);
//
//    return "worklogs";
//  }
//
//  /**
//   * 새 작업계획 생성 폼
//   */
//  @GetMapping("/new")
//  public String newWorkLogForm(Model model) {
//    model.addAttribute("workLog", new CreateRequest());
//    return "worklogs/form";
//  }
//
//  /**
//   * 작업계획 생성 처리
//   */
//  @PostMapping
//  public String createWorkLog(@ModelAttribute CreateRequest workLog, RedirectAttributes redirectAttributes) {
//    Long id = workLogService.createWorkLog(workLog);
//    redirectAttributes.addFlashAttribute("successMessage", "작업계획가 성공적으로 생성되었습니다.");
//    return "redirect:/worklogs";
//  }
//
//  /**
//   * 작업계획 상세 조회
//   */
//  @GetMapping("/{id}")
//  public String viewWorkLog(@PathVariable Long id, Model model) {
//    DetailResponse workLog = workLogService.getWorkLogDetailById(id);
//    if (workLog == null) {
//      return "error/404";
//    }
//    model.addAttribute("workLog", workLog);
//    return "worklogs/view";
//  }
//
//  /**
//   * 작업계획 수정 폼
//   */
//  @GetMapping("/{id}/edit")
//  public String editWorkLogForm(@PathVariable Long id, Model model) {
//    WorkLogDTO workLog = workLogService.getWorkLogById(id);
//    if (workLog == null) {
//      return "error/404";
//    }
//
//    // 편집을 위해 DTO를 UpdateRequest로 변환
//    WorkLogDTO.UpdateRequest updateRequest = new WorkLogDTO.UpdateRequest();
//    updateRequest.setWorkDatetime(DateTimeUtils.formatForDisplay(workLog.getWorkDatetime()));
//    updateRequest.setCarModel(workLog.getCarModel());
//    updateRequest.setProductColor(workLog.getProductColor());
//    updateRequest.setProductCode(workLog.getProductCode());
//    updateRequest.setProductName(workLog.getProductName());
//    updateRequest.setQuantity(workLog.getQuantity());
//
//    model.addAttribute("workLog", updateRequest);
//    model.addAttribute("workLogId", id);
//    return "worklogs/edit";
//  }
//
//  /**
//   * 작업계획 수정 처리
//   */
//  @PostMapping("/{id}")
//  public String updateWorkLog(
//      @PathVariable Long id,
//      @ModelAttribute WorkLogDTO.UpdateRequest workLog,
//      RedirectAttributes redirectAttributes
//  ) {
//    boolean updated = workLogService.updateWorkLog(id, workLog);
//    if (updated) {
//      redirectAttributes.addFlashAttribute("successMessage", "작업계획가 성공적으로 수정되었습니다.");
//    } else {
//      redirectAttributes.addFlashAttribute("errorMessage", "작업계획 수정에 실패했습니다.");
//    }
//    return "redirect:/worklogs/" + id;
//  }
//
//  /**
//   * 작업계획 완료 상태 업데이트 처리
//   */
//  @PostMapping("/{id}/status")
//  public String updateWorkLogStatus(
//      @PathVariable Long id,
//      @ModelAttribute StatusUpdateRequest statusRequest,
//      RedirectAttributes redirectAttributes
//  ) {
//    boolean updated = workLogService.updateWorkLogCompletionStatus(id, statusRequest.isCompleted());
//    if (updated) {
//      String message = statusRequest.isCompleted() ?
//          "작업이 완료 상태로 변경되었습니다." : "작업이 미완료 상태로 변경되었습니다.";
//      redirectAttributes.addFlashAttribute("successMessage", message);
//    } else {
//      redirectAttributes.addFlashAttribute("errorMessage", "작업계획 상태 변경에 실패했습니다.");
//    }
//    return "redirect:/worklogs/" + id;
//  }
//
//  /**
//   * 작업계획 삭제 처리
//   */
//  @PostMapping("/{id}/delete")
//  public String deleteWorkLog(@PathVariable Long id, RedirectAttributes redirectAttributes) {
//    boolean deleted = workLogService.deleteWorkLog(id);
//    if (deleted) {
//      redirectAttributes.addFlashAttribute("successMessage", "작업계획가 성공적으로 삭제되었습니다.");
//    } else {
//      redirectAttributes.addFlashAttribute("errorMessage", "작업계획 삭제에 실패했습니다.");
//    }
//    return "redirect:/worklogs";
//  }
}