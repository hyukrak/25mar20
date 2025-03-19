package com.calman.domain.worklog.controller;

import com.calman.domain.worklog.dto.WorkLogDTO;
import com.calman.domain.worklog.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 엑셀 파일 업로드 및 처리를 위한 컨트롤러
 */
@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelUploadController {

  private final WorkLogService workLogService;

  /**
   * 엑셀 파일을 업로드하고 내용을 DB에 저장
   *
   * @param file 업로드할 엑셀 파일
   * @param sheetName 처리할 시트 이름 (없으면 첫 번째 시트)
   * @return 처리 결과
   */
  @PostMapping("/upload")
  public ResponseEntity<Map<String, Object>> uploadExcel(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "sheetName", required = false) String sheetName) {

    Map<String, Object> result = new HashMap<>();
    List<String> errors = new ArrayList<>();
    int successCount = 0;

    if (file.isEmpty()) {
      result.put("success", false);
      result.put("message", "업로드된 파일이 없습니다.");
      return ResponseEntity.badRequest().body(result);
    }

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      // 시트 선택
      Sheet sheet;
      if (sheetName != null && !sheetName.trim().isEmpty()) {
        sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
          result.put("success", false);
          result.put("message", "요청한 시트 '" + sheetName + "'를 찾을 수 없습니다.");
          return ResponseEntity.badRequest().body(result);
        }
      } else {
        sheet = workbook.getSheetAt(0); // 첫 번째 시트
      }

      // 헤더 행 건너뛰기 (첫 번째 행은 헤더로 간주)
      Iterator<Row> rowIterator = sheet.rowIterator();
      if (rowIterator.hasNext()) {
        rowIterator.next(); // 헤더 행 스킵
      }

      // 각 행 처리
      while (rowIterator.hasNext()) {
        Row row = rowIterator.next();
        try {
          WorkLogDTO.CreateRequest request = processRow(row);
          if (request != null) {
            workLogService.createWorkLog(request);
            successCount++;
          }
        } catch (Exception e) {
          errors.add("행 " + (row.getRowNum() + 1) + ": " + e.getMessage());
        }
      }

      result.put("success", true);
      result.put("message", "엑셀 파일이 성공적으로 처리되었습니다.");
      result.put("totalProcessed", successCount);
      if (!errors.isEmpty()) {
        result.put("errors", errors);
      }

      return ResponseEntity.ok(result);

    } catch (IOException e) {
      result.put("success", false);
      result.put("message", "파일 처리 중 오류가 발생했습니다: " + e.getMessage());
      return ResponseEntity.status(500).body(result);
    }
  }

  /**
   * 엑셀 행을 처리하여 WorkLogDTO.CreateRequest 객체로 변환
   *
   * @param row 엑셀 행
   * @return 생성 요청 객체
   */
  private WorkLogDTO.CreateRequest processRow(Row row) {
    // 빈 행 건너뛰기
    if (row == null || isEmptyRow(row)) {
      return null;
    }

    WorkLogDTO.CreateRequest request = new WorkLogDTO.CreateRequest();

    // 날짜/시간 열 (0번 인덱스)
    Cell dateCell = row.getCell(0);
    if (dateCell != null) {
      if (dateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dateCell)) {
        Date date = dateCell.getDateCellValue();
        request.setWorkDatetime(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
      } else {
        // 날짜가 아닌 경우 현재 시간 사용
        request.setWorkDatetime(LocalDateTime.now());
      }
    } else {
      request.setWorkDatetime(LocalDateTime.now());
    }

    // 차량 모델 열 (1번 인덱스)
    Cell carModelCell = row.getCell(1);
    request.setCarModel(getCellValueAsString(carModelCell));

    // 제품 색상 열 (2번 인덱스)
    Cell colorCell = row.getCell(2);
    request.setProductColor(getCellValueAsString(colorCell));

    // 제품 코드 열 (3번 인덱스)
    Cell codeCell = row.getCell(3);
    request.setProductCode(getCellValueAsString(codeCell));

    // 제품 이름 열 (4번 인덱스)
    Cell nameCell = row.getCell(4);
    request.setProductName(getCellValueAsString(nameCell));

    // 수량 열 (5번 인덱스)
    Cell quantityCell = row.getCell(5);
    if (quantityCell != null) {
      if (quantityCell.getCellType() == CellType.NUMERIC) {
        request.setQuantity((int) quantityCell.getNumericCellValue());
      } else {
        try {
          request.setQuantity(Integer.parseInt(getCellValueAsString(quantityCell)));
        } catch (NumberFormatException e) {
          request.setQuantity(1); // 기본값
        }
      }
    } else {
      request.setQuantity(1); // 기본값
    }

    return request;
  }

  /**
   * 셀 값을 문자열로 추출
   *
   * @param cell 엑셀 셀
   * @return 셀 값 문자열
   */
  private String getCellValueAsString(Cell cell) {
    if (cell == null) {
      return "";
    }

    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue();
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          return cell.getLocalDateTimeCellValue().toString();
        } else {
          return String.valueOf((int) cell.getNumericCellValue());
        }
      case BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());
      case FORMULA:
        try {
          return String.valueOf(cell.getNumericCellValue());
        } catch (Exception e) {
          try {
            return cell.getStringCellValue();
          } catch (Exception ex) {
            return "";
          }
        }
      default:
        return "";
    }
  }

  /**
   * 행이 비어 있는지 확인
   *
   * @param row 엑셀 행
   * @return 비어 있으면 true
   */
  private boolean isEmptyRow(Row row) {
    for (int i = 0; i < 6; i++) { // 첫 6개 열 확인
      Cell cell = row.getCell(i);
      if (cell != null && cell.getCellType() != CellType.BLANK) {
        return false;
      }
    }
    return true;
  }
}