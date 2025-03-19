package com.calman.domain.worklog.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkLogDTO {

  private Long id;
  private String workDatetime;
  private String carModel;
  private String productColor;
  private String productCode;
  private String productName;
  private Integer quantity;
  private LocalDateTime createdAt;

  // 새 작업 로그 생성을 위한 요청 DTO
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateRequest {

    @Setter
    private String workDatetimeStr;
    private String carModel;
    private String productColor;
    private String productCode;
    private String productName;
    private Integer quantity;

    public String getWorkDatetime() {
      return this.workDatetimeStr;
    }

    public void setWorkDatetime(String workDatetimeStr) {
      this.workDatetimeStr = workDatetimeStr;
    }
  }

  // 기존 작업 로그 업데이트를 위한 요청 DTO
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UpdateRequest {

    private String workDatetime;
    private String carModel;
    private String productColor;
    private String productCode;
    private String productName;
    private Integer quantity;
  }

  // 목록 뷰용 응답 DTO (더 적은 필드 포함)
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ListResponse {

    private Long id;
    private String workDatetime;
    private String carModel;
    private String productColor;
    private String productCode;
    private String productName;
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
    private String workDatetime;  // 작업 일시
    private String carModel;             // 차량 모델
    private String productColor;         // 제품 색상
    private String productCode;          // 제품 코드
    private String productName;          // 제품 이름
    private Integer quantity;            // 수량
    private LocalDateTime createdAt;     // 생성 일시
  }
}