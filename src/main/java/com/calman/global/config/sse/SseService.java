package com.calman.global.config.sse;

import com.calman.domain.worklog.dto.WorkLogDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SSE(Server-Sent Events) 서비스
 * - 클라이언트의 SSE 연결 관리
 * - 이벤트 발행 및 브로드캐스팅
 */
@Slf4j
@Service
public class SseService {

  // 각 연결마다 고유 ID 생성을 위한 카운터
  private final AtomicLong emitterIdGenerator = new AtomicLong();

  // 연결된 모든 이미터 관리 (thread-safe)
  private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

  // SSE 연결 타임아웃 (ms) - 3분
  private static final long SSE_TIMEOUT = 180000L;

  // 마지막 이벤트 캐싱
  private final List<SseEvent> eventCache = new CopyOnWriteArrayList<>();
  private static final int EVENT_CACHE_SIZE = 10;

  /**
   * 새로운 SSE 연결 구독 처리
   * @return 새로운 SseEmitter 인스턴스
   */
  public SseEmitter subscribe() {
    Long emitterId = emitterIdGenerator.getAndIncrement();
    SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

    // 완료, 타임아웃, 에러 발생 시 정리
    emitter.onCompletion(() -> {
      log.debug("SSE 연결 완료: ID={}", emitterId);
      emitters.remove(emitterId);
    });

    emitter.onTimeout(() -> {
      log.debug("SSE 연결 타임아웃: ID={}", emitterId);
      emitter.complete();
      emitters.remove(emitterId);
    });

    emitter.onError(e -> {
      log.warn("SSE 연결 에러: ID={}, 메시지={}", emitterId, e.getMessage());
      emitters.remove(emitterId);
    });

    // 최초 연결 시 연결 확인 이벤트 전송
    try {
      emitter.send(SseEmitter.event()
          .name("connect")
          .data("연결 성공 - ID: " + emitterId));

      // 캐시된 이벤트 전송 (새로운 클라이언트가 최근 이벤트를 받을 수 있도록)
      for (SseEvent event : eventCache) {
        emitter.send(SseEmitter.event()
            .name(event.getName())
            .data(event.getData()));
      }

      // 활성 연결 추가
      emitters.put(emitterId, emitter);
      log.debug("새로운 SSE 연결 등록: ID={}, 현재 연결 수={}", emitterId, emitters.size());

    } catch (IOException e) {
      log.error("초기 SSE 이벤트 전송 실패: {}", e.getMessage());
      emitter.completeWithError(e);
      return new SseEmitter();
    }

    return emitter;
  }

  /**
   * 작업 로그 업데이트 이벤트 발행
   * @param updatedWorkLog 업데이트된 작업 로그 정보
   */
  public void publishWorkLogUpdated(WorkLogDTO updatedWorkLog) {
    publish("worklog-updated", updatedWorkLog);
  }

  /**
   * 작업 로그 생성 이벤트 발행
   * @param createdWorkLog 생성된 작업 로그 정보
   */
  public void publishWorkLogCreated(WorkLogDTO createdWorkLog) {
    publish("worklog-created", createdWorkLog);
  }

  /**
   * 작업 로그 삭제 이벤트 발행
   * @param deletedId 삭제된 작업 로그 ID
   */
  public void publishWorkLogDeleted(Long deletedId) {
    publish("worklog-deleted", Map.of("id", deletedId));
  }

  /**
   * 일반 이벤트 발행 - 모든 클라이언트에게 전송
   * @param eventName 이벤트 이름
   * @param data 이벤트 데이터
   */
  public void publish(String eventName, Object data) {
    // 이벤트 캐싱
    cacheEvent(eventName, data);

    log.debug("SSE 이벤트 발행: name={}, 수신자 수={}", eventName, emitters.size());

    // 제거할 에미터 리스트
    List<Long> deadEmitters = new ArrayList<>();

    // 모든 클라이언트에게 이벤트 전송
    emitters.forEach((id, emitter) -> {
      try {
        emitter.send(SseEmitter.event()
            .name(eventName)
            .data(data));
        log.trace("SSE 이벤트 전송 성공: ID={}, event={}", id, eventName);
      } catch (IOException e) {
        log.warn("SSE 이벤트 전송 실패: ID={}, 오류={}", id, e.getMessage());
        deadEmitters.add(id);
      }
    });

    // 실패한 이미터 제거
    deadEmitters.forEach(emitters::remove);

    if (!deadEmitters.isEmpty()) {
      log.debug("실패한 SSE 연결 제거: 제거 수={}, 남은 연결 수={}",
          deadEmitters.size(), emitters.size());
    }
  }

  /**
   * 이벤트 캐싱
   */
  private void cacheEvent(String eventName, Object data) {
    // 캐시 크기 제한
    while (eventCache.size() >= EVENT_CACHE_SIZE) {
      eventCache.remove(0);
    }

    // 새 이벤트 추가
    eventCache.add(new SseEvent(eventName, data));
  }

  /**
   * 캐싱을 위한 이벤트 클래스
   */
  private static class SseEvent {
    private final String name;
    private final Object data;

    public SseEvent(String name, Object data) {
      this.name = name;
      this.data = data;
    }

    public String getName() {
      return name;
    }

    public Object getData() {
      return data;
    }
  }

  /**
   * 모든 SSE 연결 종료
   */
  public void closeAllEmitters() {
    emitters.forEach((id, emitter) -> {
      try {
        emitter.complete();
      } catch (Exception e) {
        log.warn("SSE 연결 종료 중 오류: ID={}, 오류={}", id, e.getMessage());
      }
    });
    emitters.clear();
    log.info("모든 SSE 연결 종료됨");
  }
}