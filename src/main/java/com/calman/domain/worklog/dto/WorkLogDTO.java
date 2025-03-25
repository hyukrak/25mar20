package com.calman.domain.worklog.dto;

import com.calman.global.util.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkLogDTO {

  private Long id;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime workDatetime;  // String에서 LocalDateTime으로 변경

  private String carModel;
  private String productColor;
  private String productCode;
  private String productName;
  private Integer quantity;
  private String completedBy;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime completedAt;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime createdAt;

  // 새 작업계획 생성을 위한 요청 DTO
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateRequest {

    @Setter
    private String workDatetimeStr;  // 문자열 형태 유지 (UI 호환성)
    private String carModel;
    private String productColor;
    private String productCode;
    private String productName;
    private Integer quantity;

    // 기존 메서드 (하위 호환성)
    public String getWorkDatetime() {
      return this.workDatetimeStr;
    }

    public void setWorkDatetime(String workDatetimeStr) {
      this.workDatetimeStr = workDatetimeStr;
    }

    // 내부 변환용 메서드
    public LocalDateTime getWorkDatetimeAsLocalDateTime() {
      return DateTimeUtils.parseFromDisplayFormat(this.workDatetimeStr);
    }
  }

  // 기존 작업계획 업데이트를 위한 요청 DTO
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UpdateRequest {
    private String workDatetime;  // UI 입력 형식 유지
    private String carModel;
    private String productColor;
    private String productCode;
    private String productName;
    private Integer quantity;

    // 내부 변환용 메서드
    public LocalDateTime getWorkDatetimeAsLocalDateTime() {
      return DateTimeUtils.parseFromDisplayFormat(this.workDatetime);
    }
  }

  // 목록 뷰용 응답 DTO (더 적은 필드 포함)
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ListResponse {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime workDatetime;

    private String carModel;
    private String productColor;
    private String productCode;
    private String productName;
    private Integer quantity;
    private String completedBy;
    private boolean completed;


    // 화면 표시용 문자열 리턴 추가
    public String getFormattedWorkDatetime() {
      return DateTimeUtils.formatForDisplay(workDatetime);
    }
  }

  // 상세 뷰용 응답 DTO
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DetailResponse {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime workDatetime;

    private String carModel;
    private String productColor;
    private String productCode;
    private String productName;
    private Integer quantity;


    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    private String completedBy;
    private boolean completed;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // 화면 표시용 문자열 리턴 추가
    public String getFormattedWorkDatetime() {
      return DateTimeUtils.formatForDisplay(workDatetime);
    }

    // 생성자 오버로드
    public DetailResponse(Long id, LocalDateTime workDatetime, String carModel, String productColor,
        String productCode, String productName, Integer quantity,
        LocalDateTime completedAt, LocalDateTime createdAt) {
      this.id = id;
      this.workDatetime = workDatetime;
      this.carModel = carModel;
      this.productColor = productColor;
      this.productCode = productCode;
      this.productName = productName;
      this.quantity = quantity;
      this.completedAt = completedAt;
      this.completedBy = completedBy;
      this.completed = (completedAt != null);
      this.createdAt = createdAt;
    }
  }


  // 상태 업데이트 요청
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StatusUpdateRequest {
    private boolean completed;
  }
}