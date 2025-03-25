package com.calman.global.config.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 연결 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {

  private final SseService sseService;

  /**
   * SSE 구독 엔드포인트
   * 클라이언트가 이 엔드포인트로 GET 요청을 보내면 SSE 스트림이 시작됨
   * @return SseEmitter 인스턴스
   */
  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe() {
    log.info("SSE 구독 요청 받음");
    return sseService.subscribe();
  }

  /**
   * SSE 상태 확인 엔드포인트
   * SSE 서비스가 정상적으로 동작하는지 확인하는 간단한 테스트 엔드포인트
   * @return 상태 메시지
   */
  @GetMapping("/status")
  public String status() {
    return "SSE 서비스 정상 동작 중";
  }
}