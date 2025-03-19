package com.calman.domain.worklog.controller;

import com.calman.domain.worklog.dto.WorkLogDTO;
import com.calman.domain.worklog.service.WorkLogService;
import java.text.SimpleDateFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.DateValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 엑셀 파일 업로드 및 처리를 위한 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class ExcelUploadController {

  private final WorkLogService workLogService;

  private static final int MAIN_SHEET_INDEX = 3; // 4번째 시트 (인덱스 3)
  private static final int PRODUCT_NAME_SHEET_INDEX = 2; // 3번째 시트 (인덱스 2)

  private static final int START_ROW = 7; // 8번째 행 (인덱스 7)
  private static final int END_ROW = 199; // 200번째 행 (인덱스 199)

  private static final int DATETIME_COL = 2; // C열 (인덱스 2)
  private static final int COLOR_COL = 1; // B열 (인덱스 1)
  private static final int PRODUCT_NAME_COL = 4; // E열 (인덱스 4)

  private static final int QUANTITY_START_COL = 8; // I열 (인덱스 8)
  private static final int QUANTITY_END_COL = 191; // GJ열 (인덱스 191)

  private static final int PRODUCT_CODE_START_ROW = 8; // 9번째 행 (인덱스 8)
  private static final int PRODUCT_CODE_END_ROW = 188; // GG번째 행 (인덱스 188)
  private static final int PRODUCT_CODE_COL = 6; // G열 (인덱스 6)

  /**
   * 엑셀 파일을 업로드하고 내용을 DB에 저장
   *
   * @param file 업로드할 엑셀 파일
   * @param carModel 대상 차종
   * @return 처리 결과
   */
  @PostMapping("/upload")
  public ResponseEntity<Map<String, Object>> uploadExcel(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "carModel", required = true) String carModel) {

    Map<String, Object> result = new HashMap<>();
    List<String> errors = new ArrayList<>();
    int successCount = 0;

    if (file.isEmpty()) {
      result.put("success", false);
      result.put("message", "업로드된 파일이 없습니다.");
      return ResponseEntity.badRequest().body(result);
    }

    log.info("파일 업로드 시작: 파일명={}, 크기={}bytes, 차종={}",
        file.getOriginalFilename(), file.getSize(), carModel);

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      // 시트 존재 확인
      if (workbook.getNumberOfSheets() <= MAIN_SHEET_INDEX ||
          workbook.getNumberOfSheets() <= PRODUCT_NAME_SHEET_INDEX) {
        log.error("필요한 시트가 없습니다. 필요: 4번째, 3번째 시트, 실제 시트 개수: {}",
            workbook.getNumberOfSheets());
        result.put("success", false);
        result.put("message", "필요한 시트가 엑셀 파일에 없습니다.");
        return ResponseEntity.badRequest().body(result);
      }

      Sheet mainSheet = workbook.getSheetAt(MAIN_SHEET_INDEX);
      Sheet productNameSheet = workbook.getSheetAt(PRODUCT_NAME_SHEET_INDEX);

      log.info("메인 시트 이름: {}, 제품명 시트 이름: {}",
          mainSheet.getSheetName(), productNameSheet.getSheetName());

      // 제품 코드 맵 구성
      Map<Integer, String> productCodeMap = buildProductCodeMap(mainSheet);
      log.info("제품 코드 맵 구성 결과: {} 개의 코드 매핑됨", productCodeMap.size());

      // 월일 정보를 3번째 시트의 L6 셀에서 가져옴
      Row headerRow = productNameSheet.getRow(5); // 6번째 행 (인덱스 5)
      Cell monthDayCell = headerRow != null ? headerRow.getCell(11) : null; // L열 (인덱스 11)
      Date evaluatedDate = DateUtil.getJavaDate(monthDayCell.getNumericCellValue());

      // 날짜 형식 확인 및 변환
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy.MM.dd");   // 포맷 지정
        String formattedDate = simpleDateFormat.format(evaluatedDate);     //

      // 가장 이른 시간부터 처리하기 위해 시간 기준으로 정렬
      List<RowTimeInfo> rowTimeInfoList = new ArrayList<>();

      // 각 행의 시간 정보 수집
      for (int rowIdx = START_ROW; rowIdx <= END_ROW; rowIdx++) {
        Row row = mainSheet.getRow(rowIdx);
        if (row == null) {
          continue;
        }

        // 시작 시간, 색상 확인
        Cell timeCell = row.getCell(DATETIME_COL); // C열
        Cell colorCell = row.getCell(COLOR_COL); // B열

        // FormulaEvaluator 생성
        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

        // 셀 값 평가
        CellValue evalueatedCell = formulaEvaluator.evaluate(timeCell);

        // 시작 시간이나 색상이 비어 있으면 더 이상 처리하지 않음
        boolean timeEmpty = (timeCell == null || timeCell.getCellType() == CellType.BLANK);
        boolean colorEmpty = (colorCell == null || colorCell.getCellType() == CellType.BLANK);

        if (timeEmpty || colorEmpty) {
          log.info("행 {}에서 빈 값 발견. 시작시간 빈칸: {}, 색상 빈칸: {}. 이후 행 검색 중단.",
              rowIdx + 1, timeEmpty, colorEmpty);
          break;
        }

        String timeStr = getCellValueAsString(timeCell).trim().substring(11);     // 1899-12-31T09:34 에서 11번째 문자부터 자르기
        rowTimeInfoList.add(new RowTimeInfo(rowIdx, timeStr));
      }

      // 시간을 기준으로 오름차순 정렬 (가장 이른 시간부터)
      rowTimeInfoList.sort(Comparator.comparing(RowTimeInfo::getTimeStr));
      log.info("정렬된 행 수: {}", rowTimeInfoList.size());

      // 정렬된 순서대로 처리
      for (RowTimeInfo rowTimeInfo : rowTimeInfoList) {
        int rowIdx = rowTimeInfo.getRowIdx();
        String timeStr = rowTimeInfo.getTimeStr();

        Row row = mainSheet.getRow(rowIdx);
        Cell colorCell = row.getCell(COLOR_COL); // B열

        log.debug("행 {} 처리 중: 시간={}, 색상={}",
            rowIdx + 1, timeStr,
            getCellValueAsString(colorCell));

        // 제품명은 3번째 시트에서 가져오기
        Row productNameRow = productNameSheet.getRow(rowIdx);
        Cell productNameCell = productNameRow != null ? productNameRow.getCell(PRODUCT_NAME_COL) : null; // E열

        // 작업시간 문자열 생성 (yy.MM.dd HH:mm 형식)
        String workDatetimeStr = formattedDate + " " + timeStr;

        // 수량 찾기: I열부터 GJ열까지 확인
        for (int colIdx = QUANTITY_START_COL; colIdx <= QUANTITY_END_COL; colIdx++) {
          Cell quantityCell = row.getCell(colIdx);

          // 수량이 있는 경우만 처리
          if (quantityCell != null && quantityCell.getCellType() != CellType.BLANK) {
            int quantity = getNumericCellValue(quantityCell);
            log.debug("행 {}, 열 {}: 수량 {} 발견",
                rowIdx + 1,
                CellReference.convertNumToColString(colIdx),
                quantity);

            if (quantity <= 0) {
              log.debug("수량이 0 이하임. 건너뜁니다.");
              continue;
            }

            try {
              // 제품 코드 가져오기
              String productCode = productCodeMap.get(colIdx);

              if (productCode == null || productCode.trim().isEmpty()) {
                log.warn("행 {}, 열 {}: 제품 코드를 찾을 수 없음",
                    rowIdx + 1,
                    CellReference.convertNumToColString(colIdx));
                errors.add("행 " + (rowIdx + 1) + ", 열 " +
                    CellReference.convertNumToColString(colIdx) +
                    ": 해당 위치의 제품 코드를 찾을 수 없습니다.");
                continue;
              }

              // CreateRequest 생성
              WorkLogDTO.CreateRequest request = new WorkLogDTO.CreateRequest();

              // 작업시간 설정 (문자열 형식 그대로 설정)
              request.setWorkDatetime(workDatetimeStr);
              request.setCarModel(carModel);
              request.setProductColor(getCellValueAsString(colorCell));
              request.setProductCode(productCode);
              request.setProductName(getCellValueAsString(productNameCell));
              request.setQuantity(quantity);

              log.info("작업 로그 생성 요청: 시간={}, 차종={}, 색상={}, 코드={}, 이름={}, 수량={}",
                  workDatetimeStr,
                  request.getCarModel(),
                  request.getProductColor(),
                  request.getProductCode(),
                  request.getProductName(),
                  request.getQuantity());

              // 서비스를 통해 데이터 저장
              Long savedId = workLogService.createWorkLog(request);
              if (savedId != null) {
                log.info("작업 로그 생성 성공: ID={}", savedId);
                successCount++;
              } else {
                log.error("작업 로그 생성 실패");
                errors.add("행 " + (rowIdx + 1) + ", 열 " +
                    CellReference.convertNumToColString(colIdx) +
                    ": 저장 실패");
              }
            } catch (Exception e) {
              log.error("작업 로그 처리 중 오류 발생: 행={}, 열={}",
                  rowIdx + 1,
                  CellReference.convertNumToColString(colIdx),
                  e);
              errors.add("행 " + (rowIdx + 1) + ", 열 " +
                  CellReference.convertNumToColString(colIdx) +
                  ": " + e.getMessage());
            }
          }
        }
      }

      log.info("파일 처리 완료: 성공={}, 오류={}", successCount, errors.size());

      result.put("success", true);
      result.put("message", successCount + "개의 항목이 성공적으로 처리되었습니다.");
      result.put("totalProcessed", successCount);
      if (!errors.isEmpty()) {
        result.put("errors", errors);
      }

      return ResponseEntity.ok(result);

    } catch (IOException e) {
      log.error("엑셀 파일 처리 중 오류 발생", e);
      result.put("success", false);
      result.put("message", "파일 처리 중 오류가 발생했습니다: " + e.getMessage());
      return ResponseEntity.status(500).body(result);
    }
  }

  /**
   * 행 번호와 시간 정보를 저장하는 내부 클래스
   */
  private static class RowTimeInfo {
    private final int rowIdx;
    private final String timeStr;

    public RowTimeInfo(int rowIdx, String timeStr) {
      this.rowIdx = rowIdx;
      this.timeStr = timeStr;
    }

    public int getRowIdx() {
      return rowIdx;
    }

    public String getTimeStr() {
      return timeStr;
    }
  }

  /**
   * 제품 코드 맵 구성
   * 열 인덱스를 키로, 제품 코드를 값으로 하는 맵 생성
   *
   * @param sheet 메인 시트
   * @return 열 인덱스 -> 제품 코드 매핑
   */
  private Map<Integer, String> buildProductCodeMap(Sheet sheet) {
    Map<Integer, String> productCodeMap = new HashMap<>();
    log.info("제품 코드 맵 구성 시작: I열({})~GJ열({})", QUANTITY_START_COL, QUANTITY_END_COL);

    // 각 열(I~GJ)에 대해 제품 코드 찾기
    for (int colIdx = QUANTITY_START_COL; colIdx <= QUANTITY_END_COL; colIdx++) {
      // 샘플 구현: 해당 열의 헤더 행(7행, 인덱스 6)에서 제품 코드 찾기
      Row headerRow = sheet.getRow(6); // 7행 (인덱스 6)
      if (headerRow != null) {
        Cell codeCell = headerRow.getCell(colIdx);
        if (codeCell != null && codeCell.getCellType() != CellType.BLANK) {
          String code = getCellValueAsString(codeCell).trim();
          if (!code.isEmpty()) {
            log.debug("열 {}: 헤더에서 제품 코드 '{}' 발견",
                CellReference.convertNumToColString(colIdx), code);
            productCodeMap.put(colIdx, code);
            continue;
          }
        }
      }

      // 헤더에서 코드를 찾지 못한 경우 G열의 9~GG행 중에서 해당하는 코드 찾기
      log.debug("열 {}: 헤더에서 제품 코드를 찾지 못함, G열에서 검색",
          CellReference.convertNumToColString(colIdx));

      boolean foundCode = false;
      for (int rowIdx = PRODUCT_CODE_START_ROW; rowIdx <= PRODUCT_CODE_END_ROW; rowIdx++) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) continue;

        Cell cell = row.getCell(PRODUCT_CODE_COL); // G열
        if (cell != null && cell.getCellType() != CellType.BLANK) {
          String cellValue = getCellValueAsString(cell).trim();
          if (!cellValue.isEmpty()) {
            log.debug("G열, 행 {}: 값 '{}'", rowIdx + 1, cellValue);

            // 현재 열에 매핑되는 제품 코드인지 확인하는 로직이 필요함
            // 임시 구현: 배열 인덱스로 간단히 매핑
            if ((rowIdx - PRODUCT_CODE_START_ROW) == (colIdx - QUANTITY_START_COL)) {
              log.info("열 {}: G{}에서 제품 코드 '{}' 매핑됨",
                  CellReference.convertNumToColString(colIdx),
                  rowIdx + 1,
                  cellValue);
              productCodeMap.put(colIdx, cellValue);
              foundCode = true;
              break;
            }
          }
        }
      }

      if (!foundCode) {
        log.warn("열 {} ({}): 제품 코드를 찾지 못함",
            colIdx, CellReference.convertNumToColString(colIdx));
      }
    }

    return productCodeMap;
  }

  /**
   * 숫자 셀에서 정수 값 추출
   *
   * @param cell 숫자 셀
   * @return 정수 값
   */
  private int getNumericCellValue(Cell cell) {
    if (cell == null) {
      return 0;
    }

    if (cell.getCellType() == CellType.NUMERIC) {
      return (int) cell.getNumericCellValue();
    } else if (cell.getCellType() == CellType.FORMULA) {
      try {
        return (int) cell.getNumericCellValue();
      } catch (Exception e) {
        log.error("수식 셀 결과를 숫자로 변환 실패", e);
      }
    }

    try {
      return Integer.parseInt(getCellValueAsString(cell).trim());
    } catch (NumberFormatException e) {
      return 0;
    }
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
          // 숫자를 문자열로 변환할 때 소수점 제거
          double numValue = cell.getNumericCellValue();
          if (numValue == Math.floor(numValue)) {
            return String.valueOf((int) numValue);
          } else {
            return String.valueOf(numValue);
          }
        }
      case BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());
      case FORMULA:
        // 수식의 결과 타입에 따라 적절히 처리
        try {
          CellType resultType = cell.getCachedFormulaResultType();
          switch (resultType) {
            case NUMERIC:
              if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toString();
              } else {
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                  return String.valueOf((int) numValue);
                } else {
                  return String.valueOf(numValue);
                }
              }
            case STRING:
              return cell.getStringCellValue();
            case BOOLEAN:
              return String.valueOf(cell.getBooleanCellValue());
            default:
              // 그 외 타입은 빈 문자열 반환
              return "";
          }
        } catch (Exception e) {
          log.error("수식 결과 처리 중 오류", e);
          return "";
        }
      default:
        return "";
    }
  }
}