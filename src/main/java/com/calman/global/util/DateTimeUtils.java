package com.calman.global.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * 날짜 및 시간 처리 유틸리티 클래스
 */
public class DateTimeUtils {
  // 형식 상수
  private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yy.MM.dd HH:mm");
  private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  /**
   * YY.MM.DD HH:mm 형식 문자열을 LocalDateTime으로 변환
   */
  public static LocalDateTime parseFromDisplayFormat(String dateTimeStr) {
    if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
      return null;
    }

    try {
      return LocalDateTime.parse(dateTimeStr, DISPLAY_FORMAT);
    } catch (Exception e) {
      // 로깅 추가
      return null;
    }
  }

  /**
   * LocalDateTime을 YY.MM.DD HH:mm 형식 문자열로 변환 (화면 표시용)
   */
  public static String formatForDisplay(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(DISPLAY_FORMAT);
  }

  /**
   * LocalDateTime을 ISO 형식 문자열로 변환 (API 응답용)
   */
  public static String formatISO(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(ISO_FORMAT);
  }

  /**
   * 날짜의 시작시간과 종료시간 반환
   */
  public static LocalDateTime[] getDateTimeRange(LocalDate date) {
    if (date == null) {
      return new LocalDateTime[]{null, null};
    }

    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);

    return new LocalDateTime[]{startOfDay, endOfDay};
  }

  /**
   * 엑셀에서 추출한 날짜와 시간값을 LocalDateTime으로 변환
   */
  public static LocalDateTime combineExcelDateTime(Date baseDate, double timeValue) {
    if (baseDate == null) {
      return null;
    }

    // 날짜 기본 설정
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(baseDate);

    // 추가 일자 계산 (1.25 -> 1일 추가)
    int additionalDays = (int) Math.floor(timeValue);
    if (additionalDays > 0) {
      calendar.add(Calendar.DAY_OF_MONTH, additionalDays);
    }

    // 시간 계산 (1.25에서 소수 부분 0.25 -> 6시간)
    double hoursPart = timeValue - Math.floor(timeValue);
    int hours = (int) (hoursPart * 24);
    int minutes = (int) Math.round((hoursPart * 24 - hours) * 60);

    // 분이 60이면 시간 조정
    if (minutes == 60) {
      hours++;
      minutes = 0;
    }

    calendar.set(Calendar.HOUR_OF_DAY, hours);
    calendar.set(Calendar.MINUTE, minutes);
    calendar.set(Calendar.SECOND, 0);

    // Calendar를 LocalDateTime으로 변환
    return LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
  }
}