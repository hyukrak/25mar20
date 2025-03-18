package com.calman;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 애플리케이션 메인 테스트
 * - 애플리케이션 컨텍스트가 정상적으로 로드되는지 테스트합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
public class ApplicationMainTest {

  @Test
  @DisplayName("애플리케이션 컨텍스트 로드 테스트")
  public void contextLoads() {
    // 애플리케이션 컨텍스트가 정상적으로 로드되면 성공
  }

}