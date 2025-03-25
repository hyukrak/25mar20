package com.calman.domain.worklog.controller;

import com.calman.global.util.DateTimeUtils;
import com.calman.domain.worklog.dto.WorkLogDTO.CreateRequest;
import com.calman.domain.worklog.service.WorkLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * 엑셀 파일 업로드 및 처리를 위한 컨트롤러 - 개선된 버전
 * 3번 시트(HDL계획)에서 직접 데이터를 처리하도록 최적화
 * 4번 시트의 E, F, G, H 열도 추가로 처리
 */
@Slf4j
@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class ExcelUploadController {

  private final WorkLogService workLogService;

  // 시트 인덱스 상수
  private static final int PRODUCT_PLAN_SHEET_INDEX = 2; // 3번째 시트 (인덱스 2) - HDL계획
  private static final int MAIN_SHEET_INDEX = 3; // 4번째 시트 (인덱스 3) - 메인 시트
  private static final int QUANTITY_SHEET_INDEX = 3; // 4번째 시트 (인덱스 3) - 수량 시트 (메인 시트와 동일)

  // HDL계획 시트(3번 시트)의 데이터 열 상수
  private static final int MIN_START_ROW = 7; // 최소 시작 행 (인덱스 7, 8번째 행부터)

  private static final int PRODUCT_CODE_COL_C = 2; // C열 - 제품코드 매칭용 (인덱스 2)
  private static final int COLOR_CODE_COL_D = 3; // D열 - 색상코드 (인덱스 3)
  private static final int PRODUCT_NAME_COL_E = 4; // E열 - 제품명 (인덱스 4)

  private static final int QUANTITY_COL_H = 7; // H열 - 수량1 (인덱스 7)
  private static final int QUANTITY_COL_I = 8; // I열 - 수량2 (인덱스 8)
  private static final int QUANTITY_COL_J = 9; // J열 - 수량3 (인덱스 9)

  private static final int TIME_COL_L = 11; // L열 - 시작시간 (인덱스 11)

  // 4번째 시트의 제품 수량 열 상수
  private static final int COLOR_COL_B = 1; // B열 - 색상 (인덱스 1)
  private static final int TIME_COL_C = 2; // C열 - 작업 시간 (인덱스 2)
  private static final int QUANTITY_COL_E = 4; // E열 - 첫 번째 제품 수량 (인덱스 4)
  private static final int QUANTITY_COL_F = 5; // F열 - 두 번째 제품 수량 (인덱스 5)
  private static final int QUANTITY_COL_G = 6; // G열 - 세 번째 제품 수량 (인덱스 6)
  private static final int QUANTITY_COL_H_MAIN = 7; // H열 - 네 번째 제품 수량 (인덱스 7)

  // 제품 코드 상수 (4번째 시트 E, F, G, H 열)
  private static final String PRODUCT_CODE_E = "77112AR110 SC";
  private static final String PRODUCT_CODE_F = "78112AR110 SC";
  private static final String PRODUCT_CODE_G = "77112AR110 SA";
  private static final String PRODUCT_CODE_H = "78112AR110 SA";

  // 제품 이름 상수 (4번째 시트 E, F, G, H 열)
  private static final String PRODUCT_NAME_E = "FL CAPA";
  private static final String PRODUCT_NAME_F = "FR CAPA";
  private static final String PRODUCT_NAME_G = "FL CUSHION";
  private static final String PRODUCT_NAME_H = "FR CUSHION";

  // 메인 시트(4번 시트)의 헤더 상수
  private static final int HEADER_ROW = 6; // 7번째 행 (인덱스 6)
  private static final int HEADER_START_COL = 8; // I열부터 시작 (인덱스 8)

  // 날짜 정보 상수
  private static final int DATE_HEADER_ROW = 5; // 6번째 행 (인덱스 5)
  private static final int DATE_COL = 11; // L열 (인덱스 11)

  /**
   * 엑셀 파일을 업로드하고 내용을 DB에 저장
   * 개선된 버전: 3번 시트(HDL계획)에서 직접 데이터를 처리하도록 최적화
   * 4번 시트의 E, F, G, H 열도 추가로 처리
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
    AtomicInteger successCount = new AtomicInteger(0);

    if (file.isEmpty()) {
      result.put("success", false);
      result.put("message", "업로드된 파일이 없습니다.");
      return ResponseEntity.badRequest().body(result);
    }

    log.info("파일 업로드 시작: 파일명={}, 크기={}bytes, 차종={}",
        file.getOriginalFilename(), file.getSize(), carModel);

    // 입력 스트림을 미리 가져와서 예외 처리를 간소화
    try (InputStream fileInputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(fileInputStream)) {

      // 필요한 시트 존재 확인
      if (workbook.getNumberOfSheets() <= MAIN_SHEET_INDEX ||
          workbook.getNumberOfSheets() <= PRODUCT_PLAN_SHEET_INDEX) {
        log.error("필요한 시트가 없습니다. 필요: 3번째, 4번째 시트, 실제 시트 개수: {}",
            workbook.getNumberOfSheets());
        result.put("success", false);
        result.put("message", "필요한 시트가 엑셀 파일에 없습니다.");
        return ResponseEntity.badRequest().body(result);
      }

      Sheet mainSheet = workbook.getSheetAt(MAIN_SHEET_INDEX);
      Sheet productPlanSheet = workbook.getSheetAt(PRODUCT_PLAN_SHEET_INDEX);

      log.info("메인 시트 이름: {}, 제품계획 시트 이름: {}",
          mainSheet.getSheetName(), productPlanSheet.getSheetName());

      // 데이터가 있는 마지막 행 동적 감지
      int lastDataRow = findLastDataRow(productPlanSheet);
      log.info("데이터가 있는 마지막 행: {} (총 {}행)",
          lastDataRow + 1, lastDataRow - MIN_START_ROW + 1);

      // 헤더 열의 마지막 인덱스 동적 감지
      int lastHeaderCol = findLastHeaderColumn(mainSheet);
      log.info("헤더가 있는 마지막 열: {} ({})",
          lastHeaderCol + 1, CellReference.convertNumToColString(lastHeaderCol));

      // 제품 코드 헤더 매핑 구성 (4번째 시트의 7번째 행)
      Map<String, String> productCodeMap = buildProductCodeMap(mainSheet, lastHeaderCol);
      log.info("제품 코드 맵 구성 결과: {} 개의 코드 매핑됨", productCodeMap.size());

      // 기준 날짜 가져오기 (3번 시트의 L6 셀)
      Date baseDate = extractBaseDate(productPlanSheet);
      log.info("기준 날짜: {}", baseDate);

      // 1. 3번 시트(HDL계획) 데이터 처리 시작
      log.info("3번 시트 데이터 처리 시작...");

      // 효율적인 메모리 사용을 위해 필요한 데이터만 미리 로드
      List<RowData> validRowData = preloadRowData(productPlanSheet, MIN_START_ROW, lastDataRow);
      log.info("유효한 데이터 행 수: {}, 범위: {} ~ {}",
          validRowData.size(), MIN_START_ROW + 1, lastDataRow + 1);

      // 병렬 처리로 성능 향상 (독립적인 행 처리에 적합)
      validRowData.parallelStream().forEach(rowData -> {
        try {
          processRowData(rowData, productCodeMap, baseDate, carModel, successCount, errors);
        } catch (Exception e) {
          log.error("행 {} 처리 중 예외 발생", rowData.rowIndex + 1, e);
          synchronized (errors) {
            errors.add("행 " + (rowData.rowIndex + 1) + ": " + e.getMessage());
          }
        }
      });

      // 중간 결과 로깅
      int hdl_plan_success = successCount.get();
      log.info("3번 시트 처리 완료: 성공 항목 {}개", hdl_plan_success);

      // 2. 4번 시트 E, F, G, H 열 제품 수량 처리
      log.info("4번 시트 E, F, G, H 열 제품 수량 처리 시작...");
      int quantityRowsProcessed = processQuantityData(mainSheet, MIN_START_ROW, lastDataRow,
          baseDate, carModel, successCount, errors);

      int main_sheet_success = successCount.get() - hdl_plan_success;
      log.info("4번 시트 처리 완료: {}개 행 중 {}개 항목 성공",
          quantityRowsProcessed, main_sheet_success);

      log.info("파일 처리 완료: 성공={} 항목 (3번 시트: {}, 4번 시트: {}), 오류={} 항목",
          successCount.get(), hdl_plan_success, main_sheet_success, errors.size());

      result.put("success", true);
      result.put("message", successCount.get() + "개의 항목이 성공적으로 처리되었습니다.");
      result.put("totalProcessed", successCount.get());
      result.put("sheet3Processed", hdl_plan_success);
      result.put("sheet4Processed", main_sheet_success);
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
   * 행 데이터를 저장하는 내부 클래스
   */
  private static class RowData {
    final int rowIndex;
    final String productCodeKey;
    final String colorCode;
    final String productName;
    final LocalDateTime workDateTime;
    final Map<Integer, Integer> quantities; // 열 인덱스 -> 수량 매핑
    int successCount = 0; // 이 행에서 성공한 처리 수

    RowData(int rowIndex, String productCodeKey, String colorCode, String productName,
        LocalDateTime workDateTime, Map<Integer, Integer> quantities) {
      this.rowIndex = rowIndex;
      this.productCodeKey = productCodeKey;
      this.colorCode = colorCode;
      this.productName = productName;
      this.workDateTime = workDateTime;
      this.quantities = quantities;
    }
  }

  /**
   * 데이터가 포함된 행을 미리 로드하여 효율적으로 처리
   */
  private List<RowData> preloadRowData(Sheet sheet, int startRow, int endRow) {
    List<RowData> result = new ArrayList<>();

    for (int rowIdx = startRow; rowIdx <= endRow; rowIdx++) {
      Row row = sheet.getRow(rowIdx);
      if (row == null) {
        continue;
      }

      // 필수 셀 확인
      Cell productCodeCellC = row.getCell(PRODUCT_CODE_COL_C);
      Cell colorCodeCellD = row.getCell(COLOR_CODE_COL_D);
      Cell timeCell = row.getCell(TIME_COL_L);
      Cell productNameCell = row.getCell(PRODUCT_NAME_COL_E);

      // 필수 데이터 검증
      if (productCodeCellC == null || colorCodeCellD == null || timeCell == null ||
          isEmptyCell(productCodeCellC) || isEmptyCell(colorCodeCellD) || isEmptyCell(timeCell)) {
        continue;
      }

      // 제품 코드 키 추출
      String productCodeKey = getCellValueAsString(productCodeCellC);
      if (productCodeKey.isEmpty()) {
        continue;
      }

      // 시간 데이터 추출 및 변환
      LocalDateTime workDateTime = extractDateTime(timeCell, extractBaseDate(sheet));
      if (workDateTime == null) {
        log.warn("행 {}: 시간 데이터 추출 실패", rowIdx + 1);
        continue;
      }

      // 색상 정보 추출 (오른쪽 3글자)
      String colorCode = extractColorCode(colorCodeCellD);

      // 제품명 추출
      String productName = getCellValueAsString(productNameCell);

      // 수량 정보 추출 (H,I,J열)
      Map<Integer, Integer> quantities = new HashMap<>();
      quantities.put(QUANTITY_COL_H, getNumericCellValue(row.getCell(QUANTITY_COL_H)));
      quantities.put(QUANTITY_COL_I, getNumericCellValue(row.getCell(QUANTITY_COL_I)));
      quantities.put(QUANTITY_COL_J, getNumericCellValue(row.getCell(QUANTITY_COL_J)));

      // 유효한 데이터를 가진 행만 추가
      result.add(new RowData(rowIdx, productCodeKey, colorCode, productName, workDateTime, quantities));
    }

    return result;
  }

  /**
   * 행 데이터 처리
   */
  private void processRowData(RowData rowData, Map<String, String> productCodeMap,
      Date baseDate, String carModel,
      AtomicInteger successCount, List<String> errors) {

    // 각 제품 코드에 대해 처리
    productCodeMap.entrySet().stream()
        .filter(entry -> rowData.productCodeKey.equals(entry.getKey())) // 코드 키가 일치하는 것만 필터링
        .forEach(entry -> {
          String productCode = entry.getValue();

          // 총 수량 계산 (H+I+J 열 합계)
          int totalQuantity = rowData.quantities.values().stream().mapToInt(Integer::intValue).sum();

          // 수량이 0 이하면 건너뛰기
          if (totalQuantity <= 0) {
            return;
          }

          log.debug("행 {}: 매칭 성공 - 코드키={}, 제품코드={}, 색상={}, 수량={}",
              rowData.rowIndex + 1, rowData.productCodeKey, productCode, rowData.colorCode, totalQuantity);

          try {
            // 작업계획 생성 요청 준비
            CreateRequest request = new CreateRequest();

            // 날짜시간 문자열 변환 (UI 호환성)
            String formattedDateTime = DateTimeUtils.formatForDisplay(rowData.workDateTime);

            request.setWorkDatetime(formattedDateTime);
            request.setCarModel(carModel);
            request.setProductColor(rowData.colorCode);
            request.setProductCode(productCode);
            request.setProductName(rowData.productName);
            request.setQuantity(totalQuantity);

            // 데이터 저장
            Long savedId = workLogService.createWorkLog(request);
            if (savedId != null) {
              log.debug("작업계획 생성 성공: ID={}", savedId);
              successCount.incrementAndGet();
              rowData.successCount++; // 행 단위 성공 카운트 증가
            } else {
              log.error("작업계획 생성 실패: 행={}", rowData.rowIndex + 1);
              synchronized (errors) {
                errors.add("행 " + (rowData.rowIndex + 1) + ": 저장 실패");
              }
            }
          } catch (Exception e) {
            log.error("작업계획 처리 중 오류 발생: 행={}", rowData.rowIndex + 1, e);
            synchronized (errors) {
              errors.add("행 " + (rowData.rowIndex + 1) + ": " + e.getMessage());
            }
          }
        });
  }

  /**
   * 4번 시트의 E, F, G, H 열의 제품 수량 데이터 처리
   *
   * @param sheet 수량 시트 (4번 시트)
   * @param startRow 시작 행
   * @param endRow 종료 행
   * @param baseDate 기준 날짜
   * @param carModel 차종
   * @param successCount 성공 카운터
   * @param errors 오류 목록
   * @return 처리된 행 수
   */
  private int processQuantityData(Sheet sheet, int startRow, int endRow, Date baseDate,
      String carModel, AtomicInteger successCount, List<String> errors) {

    // 처리한 행 수 카운트
    int processedRows = 0;
    int successRows = 0;

    // 각 행 처리
    for (int rowIdx = startRow; rowIdx <= endRow; rowIdx++) {
      Row row = sheet.getRow(rowIdx);
      if (row == null) {
        continue;
      }

      // 시간과 색상 셀 가져오기
      Cell timeCell = row.getCell(TIME_COL_C);
      Cell colorCell = row.getCell(COLOR_COL_B);

      // 필수 데이터 확인
      if (timeCell == null || colorCell == null ||
          isEmptyCell(timeCell) || isEmptyCell(colorCell)) {
        continue;
      }

      // 시간 데이터 추출
      LocalDateTime workDateTime = extractDateTime(timeCell, baseDate);
      if (workDateTime == null) {
        log.warn("행 {}: 시간 데이터 추출 실패", rowIdx + 1);
        continue;
      }

      // 색상 코드 가져오기
      String colorCode = getCellValueAsString(colorCell).trim();
      if (colorCode.isEmpty()) {
        continue;
      }

      // 이 행의 처리 성공 여부
      boolean rowSuccess = false;

      // E, F, G, H 열 처리
      rowSuccess |= processQuantityCell(row, QUANTITY_COL_E, PRODUCT_CODE_E, PRODUCT_NAME_E,
          colorCode, workDateTime, carModel, successCount, errors, rowIdx);

      rowSuccess |= processQuantityCell(row, QUANTITY_COL_F, PRODUCT_CODE_F, PRODUCT_NAME_F,
          colorCode, workDateTime, carModel, successCount, errors, rowIdx);

      rowSuccess |= processQuantityCell(row, QUANTITY_COL_G, PRODUCT_CODE_G, PRODUCT_NAME_G,
          colorCode, workDateTime, carModel, successCount, errors, rowIdx);

      rowSuccess |= processQuantityCell(row, QUANTITY_COL_H_MAIN, PRODUCT_CODE_H, PRODUCT_NAME_H,
          colorCode, workDateTime, carModel, successCount, errors, rowIdx);

      processedRows++;
      if (rowSuccess) {
        successRows++;
      }
    }

    log.info("4번 시트 제품 수량 처리 결과: {}개 행 중 {}개 행 성공 ({}%)",
        processedRows, successRows,
        processedRows > 0 ? (successRows * 100 / processedRows) : 0);

    return processedRows;
  }

  /**
   * 제품 수량 셀 처리
   *
   * @param row 현재 행
   * @param colIdx 열 인덱스
   * @param productCode 제품 코드
   * @param productName 제품 이름
   * @param colorCode 색상 코드
   * @param workDateTime 작업 시간
   * @param carModel 차종
   * @param successCount 성공 카운터
   * @param errors 오류 목록
   * @param rowIdx 행 인덱스 (오류 메시지용)
   * @return 처리 성공 여부
   */
  private boolean processQuantityCell(Row row, int colIdx, String productCode, String productName,
      String colorCode, LocalDateTime workDateTime, String carModel,
      AtomicInteger successCount, List<String> errors, int rowIdx) {

    Cell quantityCell = row.getCell(colIdx);
    if (quantityCell == null || isEmptyCell(quantityCell)) {
      return false;
    }

    int quantity = getNumericCellValue(quantityCell);
    if (quantity <= 0) {
      return false;
    }

    try {
      // 작업계획 생성 요청 준비
      CreateRequest request = new CreateRequest();

      // 날짜시간 문자열 변환 (UI 호환성)
      String formattedDateTime = DateTimeUtils.formatForDisplay(workDateTime);

      request.setWorkDatetime(formattedDateTime);
      request.setCarModel(carModel);
      request.setProductColor(colorCode);
      request.setProductCode(productCode);
      request.setProductName(productName);
      request.setQuantity(quantity);

      // 로그
      log.debug("4번시트 작업계획: 행={}, 열={}, 제품코드={}, 제품명={}, 색상={}, 수량={}",
          rowIdx + 1, CellReference.convertNumToColString(colIdx),
          productCode, productName, colorCode, quantity);

      // 데이터 저장
      Long savedId = workLogService.createWorkLog(request);
      if (savedId != null) {
        log.debug("작업계획 생성 성공: 행={}, 열={}, ID={}",
            rowIdx + 1, CellReference.convertNumToColString(colIdx), savedId);
        successCount.incrementAndGet();
        return true;
      } else {
        log.error("작업계획 생성 실패: 행={}, 열={}",
            rowIdx + 1, CellReference.convertNumToColString(colIdx));
        synchronized (errors) {
          errors.add(String.format("행 %d, 열 %s: 저장 실패",
              rowIdx + 1, CellReference.convertNumToColString(colIdx)));
        }
        return false;
      }
    } catch (Exception e) {
      log.error("작업계획 처리 중 오류 발생: 행={}, 열={}",
          rowIdx + 1, CellReference.convertNumToColString(colIdx), e);
      synchronized (errors) {
        errors.add(String.format("행 %d, 열 %s: %s",
            rowIdx + 1, CellReference.convertNumToColString(colIdx), e.getMessage()));
      }
      return false;
    }
  }

  /**
   * 데이터가 있는 마지막 행 찾기
   */
  private int findLastDataRow(Sheet sheet) {
    int lastRowNum = sheet.getLastRowNum();
    int lastDataRowNum = MIN_START_ROW; // 최소한 최소 시작 행은 확인
    log.debug("시트의 물리적 마지막 행: {}", lastRowNum + 1);

    // 마지막 행부터 역방향으로 데이터가 있는 행 찾기
    for (int i = lastRowNum; i >= MIN_START_ROW; i--) {
      Row row = sheet.getRow(i);
      if (row != null) {
        Cell productCodeCell = row.getCell(PRODUCT_CODE_COL_C);
        Cell colorCodeCell = row.getCell(COLOR_CODE_COL_D);
        Cell timeCell = row.getCell(TIME_COL_L);

        if (productCodeCell != null && colorCodeCell != null && timeCell != null &&
            !isEmptyCell(productCodeCell) && !isEmptyCell(colorCodeCell) && !isEmptyCell(timeCell)) {
          lastDataRowNum = i;
          log.debug("데이터가 있는 마지막 행 발견: {}", lastDataRowNum + 1);
          break;
        }
      }
    }

    // 안전을 위해 최대 300행까지만 처리
    return Math.min(lastDataRowNum, 300);
  }

  /**
   * 헤더가 있는 마지막 열 찾기
   */
  private int findLastHeaderColumn(Sheet sheet) {
    Row headerRow = sheet.getRow(HEADER_ROW);
    if (headerRow == null) {
      log.warn("헤더 행(7번째 행)을 찾을 수 없음. 기본값 사용");
      return HEADER_START_COL + 20; // 기본값 제공
    }

    int lastCol = HEADER_START_COL;
    int maxCol = Math.min(headerRow.getLastCellNum(), 300); // 안전을 위한 최대값
    int emptyCount = 0; // 빈 열 연속 카운트

    log.debug("헤더 행의 물리적 마지막 열: {} ({})",
        headerRow.getLastCellNum(),
        CellReference.convertNumToColString(headerRow.getLastCellNum() - 1));

    // 헤더 행에서 데이터가 있는 마지막 열 찾기
    for (int i = HEADER_START_COL; i < maxCol; i++) {
      Cell cell = headerRow.getCell(i);
      if (cell == null || isEmptyCell(cell)) {
        emptyCount++;
        // 연속해서 빈 셀이 5개 이상이면 중단
        if (emptyCount >= 5) {
          log.debug("5개 이상의 빈 열이 연속됨. 검색 중단");
          break;
        }
      } else {
        lastCol = i;
        emptyCount = 0; // 데이터가 있는 열을 찾았으므로 카운터 리셋
        log.debug("헤더 데이터가 있는 열 발견: {} ({}), 값: {}",
            i + 1, CellReference.convertNumToColString(i), getCellValueAsString(cell));
      }
    }

    // 안전을 위해 200열까지만 처리
    int maxAllowedCol = Math.min(lastCol, 200);
    if (maxAllowedCol < lastCol) {
      log.info("안전을 위해 최대 열 제한 적용: {} → {}",
          CellReference.convertNumToColString(lastCol),
          CellReference.convertNumToColString(maxAllowedCol));
    }

    return maxAllowedCol;
  }

  /**
   * 색상 코드 추출 - D열에서 오른쪽 3글자 추출
   *
   * @param colorCodeCell 색상 코드 셀
   * @return 추출된 3글자 색상 코드
   */
  private String extractColorCode(Cell colorCodeCell) {
    String fullCode = getCellValueAsString(colorCodeCell);
    if (fullCode.length() >= 3) {
      return fullCode.substring(fullCode.length() - 3);
    }
    return fullCode;
  }

  /**
   * 기준 날짜 추출 - 개선된 버전
   *
   * @param planSheet 제품계획 시트 (3번 시트)
   * @return 기준 날짜
   */
  private Date extractBaseDate(Sheet planSheet) {
    // 캐싱을 위한 정적 맵 - 시트별 날짜 정보 저장
    Row headerRow = planSheet.getRow(DATE_HEADER_ROW);
    if (headerRow != null) {
      Cell dateCell = headerRow.getCell(DATE_COL);
      if (dateCell != null && dateCell.getCellType() != CellType.BLANK) {
          return DateUtil.getJavaDate(dateCell.getNumericCellValue());
      }
    }

    // 날짜를 찾을 수 없는 경우 현재 날짜 사용
    log.warn("시트에서 기준 날짜를 찾을 수 없습니다. 현재 날짜를 사용합니다.");
    return new Date();
  }

  /**
   * 제품 코드 매핑 구성 - 4번 시트의 헤더 행에서 제품 코드 정보 추출
   *
   * @param mainSheet 메인 시트 (4번 시트)
   * @param lastHeaderCol 헤더의 마지막 열 인덱스
   * @return 제품 코드 키 -> 제품 코드 매핑
   */
  private Map<String, String> buildProductCodeMap(Sheet mainSheet, int lastHeaderCol) {
    Map<String, String> productCodeMap = new HashMap<>();

    // 4번 시트의 헤더 행 (7번째 행, 인덱스 6)
    Row headerRow = mainSheet.getRow(HEADER_ROW);
    if (headerRow == null) {
      log.warn("메인 시트에서 헤더 행을 찾을 수 없습니다.");
      return productCodeMap;
    }

    // I열부터 동적으로 감지된 마지막 열까지 헤더 검사
    IntStream.rangeClosed(HEADER_START_COL, lastHeaderCol)
        .parallel() // 병렬 처리로 성능 향상
        .forEach(colIdx -> {
          Cell headerCell = headerRow.getCell(colIdx);
          if (headerCell != null && !isEmptyCell(headerCell)) {
            String productCode = getCellValueAsString(headerCell).trim();
            if (!productCode.isEmpty()) {
              // 제품 코드 키를 제품 코드 값으로 사용
              synchronized (productCodeMap) {
                productCodeMap.put(productCode, productCode);
              }
              log.debug("열 {}: 제품 코드 '{}' 추출됨",
                  CellReference.convertNumToColString(colIdx), productCode);
            }
          }
        });

    return productCodeMap;
  }

  /**
   * 셀이 비어있는지 확인 (최적화된 버전)
   *
   * @param cell 확인할 셀
   * @return 비어있으면 true, 아니면 false
   */
  private boolean isEmptyCell(Cell cell) {
    if (cell == null) {
      return true;
    }

    switch (cell.getCellType()) {
      case BLANK:
        return true;
      case STRING:
        return cell.getStringCellValue() == null || cell.getStringCellValue().trim().isEmpty();
      case NUMERIC:
        // 0 값은 비어있지 않은 것으로 간주
        return false;
      case BOOLEAN:
        return false;
      case FORMULA:
        try {
          CellType resultType = cell.getCachedFormulaResultType();
          if (resultType == CellType.BLANK) {
            return true;
          } else if (resultType == CellType.STRING) {
            String value = cell.getStringCellValue();
            return value == null || value.trim().isEmpty();
          }
          return false;
        } catch (Exception e) {
          return true;
        }
      default:
        return true;
    }
  }

  /**
   * 엑셀 셀에서 날짜/시간 데이터 추출하여 LocalDateTime 생성
   *
   * @param timeCell 시간 정보가 있는 셀
   * @param baseDate 기준 날짜
   * @return 변환된 LocalDateTime
   */
  private LocalDateTime extractDateTime(Cell timeCell, Date baseDate) {
    if (timeCell == null) {
      return null;
    }

    try {
      // 가장 일반적인 케이스 먼저 처리 (성능 최적화)
      if (timeCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(timeCell)) {
        double timeValue = timeCell.getNumericCellValue();
        return DateTimeUtils.combineExcelDateTime(baseDate, timeValue);
      }

      // 수식인 경우 결과 타입 확인
      if (timeCell.getCellType() == CellType.FORMULA) {
        try {
          if (timeCell.getCachedFormulaResultType() == CellType.NUMERIC) {
            double timeValue = timeCell.getNumericCellValue();
            return DateTimeUtils.combineExcelDateTime(baseDate, timeValue);
          }
        } catch (Exception e) {
          log.debug("  -> 수식 결과 처리 실패: {}", e.getMessage());
        }
      }

      // 문자열인 경우 직접 파싱 시도
      String cellValue = getCellValueAsString(timeCell).trim();
      if (!cellValue.isEmpty()) {
        log.debug("시간 셀 값(텍스트): {}", cellValue);
      }

      // 모든 변환 시도 실패 시
      log.warn("시간 데이터를 추출할 수 없습니다. 셀 값: '{}'", cellValue);
      return null;
    } catch (Exception e) {
      log.error("날짜/시간 추출 오류", e);
      return null;
    }
  }

  /**
   * 숫자 셀에서 정수 값 추출 (최적화된 버전)
   *
   * @param cell 숫자 셀
   * @return 정수 값
   */
  private int getNumericCellValue(Cell cell) {
    if (cell == null) {
      return 0;
    }

    try {
      // 가장 일반적인 케이스 먼저 처리 (성능 최적화)
      if (cell.getCellType() == CellType.NUMERIC) {
        return (int) cell.getNumericCellValue();
      }

      // 수식인 경우
      if (cell.getCellType() == CellType.FORMULA) {
        if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
          return (int) cell.getNumericCellValue();
        }
      }

      // 문자열이나 다른 타입인 경우 문자열로 변환 후 파싱 시도
      String strValue = getCellValueAsString(cell).trim();
      if (!strValue.isEmpty()) {
        try {
          return Integer.parseInt(strValue);
        } catch (NumberFormatException ignored) {
          // 무시하고 계속 진행
        }

        // 소수점 형태의 문자열 처리
        try {
          return (int) Double.parseDouble(strValue);
        } catch (NumberFormatException ignored) {
          // 무시
        }
      }
    } catch (Exception e) {
      log.debug("숫자 변환 실패: {}", e.getMessage());
    }

    return 0;
  }

  /**
   * 셀 값을 문자열로 추출 (최적화된 버전)
   *
   * @param cell 엑셀 셀
   * @return 셀 값 문자열
   */
  private String getCellValueAsString(Cell cell) {
    if (cell == null) {
      return "";
    }

    // 문자열 빌더 사용으로 문자열 연산 최적화
    StringBuilder result = new StringBuilder();

    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue();

      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          return cell.getLocalDateTimeCellValue().toString();
        } else {
          double numValue = cell.getNumericCellValue();
          // 정수 확인
          if (numValue == Math.floor(numValue)) {
            return String.valueOf((int) numValue);
          } else {
            return String.valueOf(numValue);
          }
        }

      case BOOLEAN:
        return Boolean.toString(cell.getBooleanCellValue());

      case FORMULA:
        try {
          CellType resultType = cell.getCachedFormulaResultType();
          switch (resultType) {
            case NUMERIC:
              double numValue = cell.getNumericCellValue();
              if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toString();
              } else if (numValue == Math.floor(numValue)) {
                return String.valueOf((int) numValue);
              } else {
                return String.valueOf(numValue);
              }
            case STRING:
              return cell.getStringCellValue();
            case BOOLEAN:
              return Boolean.toString(cell.getBooleanCellValue());
            default:
              return "";
          }
        } catch (Exception e) {
          // 안전하게 원래 수식 반환
          return cell.getCellFormula();
        }

      case BLANK:
        return "";

      default:
        return "";
    }
  }
}