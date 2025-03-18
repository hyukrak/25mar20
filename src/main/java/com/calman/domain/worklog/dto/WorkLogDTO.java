package com.calman.domain.worklog.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkLogDTO {

  private Long id;
  private LocalDateTime workDatetime;
  private String carModel;
  private String materialCode;
  private Integer quantity;
  private LocalDateTime createdAt;

  // 새 작업 로그 생성을 위한 요청 DTO
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateRequest {

    private LocalDateTime workDatetime;
    private String carModel;
    private String materialCode;
    private Integer quantity;
  }

  // 기존 작업 로그 업데이트를 위한 요청 DTO
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UpdateRequest {

    private LocalDateTime workDatetime;
    private String carModel;
    private String materialCode;
    private Integer quantity;
  }

  // 목록 뷰용 응답 DTO (더 적은 필드 포함)
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ListResponse {

    private Long id;
    private LocalDateTime workDatetime;
    private String carModel;
    private String materialCode;
    private Integer quantity;
  }

  /**
   * 상세 뷰용 응답 DTO (모든 필드 포함)
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DetailResponse {

    private Long id;                     // 고유 ID
    private LocalDateTime workDatetime;  // 작업 일시
    private String carModel;             // 차량 모델
    private String materialCode;         // 자재 코드
    private Integer quantity;            // 수량
    private LocalDateTime createdAt;     // 생성 일시
//  private Long userId;                 // 작업자 ID
//  private String status;               // 상태
//  private String notes;                // 추가 메모
//  private Long departmentId;           // 부서 ID
//  private Long lineId;                 // 라인 ID
//  private String lotNumber;            // 로트 번호
  }
}