package com.calman.global.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

/**
 * 테스트용 설정 클래스
 */
@TestConfiguration
public class TestConfig {

  /**
   * 테스트용 인메모리 데이터베이스 구성
   */
  @Bean
  public DataSource dataSource() {
    // SQLite 대신 H2 인메모리 데이터베이스를 사용하여 테스트 속도 향상
    return new EmbeddedDatabaseBuilder()
        .setType(EmbeddedDatabaseType.H2)
        .addScript("classpath:schema/worklogs.sql") // 스키마 초기화 스크립트
        .addScript("classpath:data/test-data-sqlite.sql") // 테스트 데이터 스크립트
        .build();
  }
}