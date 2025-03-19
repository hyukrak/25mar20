package com.calman.domain.worklog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 테스트 데이터 관리를 위한 컨트롤러
 * - 테스트 데이터 로딩
 * - 데이터 전체 삭제
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestDummyController {

  private final JdbcTemplate jdbcTemplate;

  /**
   * 테스트 데이터 삽입 엔드포인트
   * data-sqlite.sql 파일의 INSERT 구문을 실행합니다.
   *
   * @return 삽입 결과 정보
   */
  @GetMapping("/testdummy")
  public ResponseEntity<Map<String, Object>> insertTestData() {
    Map<String, Object> result = new HashMap<>();
    List<String> executedQueries = new ArrayList<>();
    int totalInserted = 0;

    try {
      // SQL 파일 읽기
      ClassPathResource resource = new ClassPathResource("data/data-sqlite.sql");
      String sqlContent = new BufferedReader(
          new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
          .lines()
          .collect(Collectors.joining("\n"));

      // INSERT 문으로 분할 (;로 구분)
      String[] queries = sqlContent.split(";");

      // 각 INSERT 문 실행
      for (String query : queries) {
        String trimmedQuery = query.trim();
        if (trimmedQuery.isEmpty()) {
          continue;
        }

        // INSERT 문만 실행 (INSERT로 시작하는 문장만)
        if (trimmedQuery.toUpperCase().startsWith("INSERT")) {
          jdbcTemplate.execute(trimmedQuery);
          executedQueries.add(trimmedQuery);
          totalInserted++;
        }
      }

      result.put("success", true);
      result.put("message", "테스트 데이터가 성공적으로 삽입되었습니다.");
      result.put("totalInserted", totalInserted);
      result.put("queries", executedQueries);

      return ResponseEntity.ok(result);
    } catch (IOException e) {
      result.put("success", false);
      result.put("error", "SQL 파일을 읽는 중 오류가 발생했습니다: " + e.getMessage());
      return ResponseEntity.status(500).body(result);
    } catch (Exception e) {
      result.put("success", false);
      result.put("error", "SQL 실행 중 오류가 발생했습니다: " + e.getMessage());
      return ResponseEntity.status(500).body(result);
    }
  }

  /**
   * 모든 작업 로그 데이터 삭제 엔드포인트
   * work_logs 테이블의 모든 데이터를 삭제합니다.
   *
   * @return 삭제 결과 정보
   */
  @GetMapping("/deleteall")
  public ResponseEntity<Map<String, Object>> deleteAllData() {
    Map<String, Object> result = new HashMap<>();

    try {
      // DELETE 쿼리 실행
      int deletedCount = jdbcTemplate.update("DELETE FROM work_logs");

      result.put("success", true);
      result.put("message", "모든 작업 로그 데이터가 성공적으로 삭제되었습니다.");
      result.put("deletedCount", deletedCount);

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      result.put("success", false);
      result.put("error", "데이터 삭제 중 오류가 발생했습니다: " + e.getMessage());
      return ResponseEntity.status(500).body(result);
    }
  }
}