/**
 * sse.js - 작업계획 시스템 SSE(Server-Sent Events) 관련 기능
 * 서버의 실시간 이벤트를 수신하고 처리하는 기능을 제공합니다.
 */

// SSE 네임스페이스 생성
const SSE = {
  eventSource: null,
  reconnectAttempts: 0,
  maxReconnectAttempts: 5,
  reconnectDelay: 2000, // ms

  /**
   * SSE 연결 초기화
   * @returns {boolean} 연결 성공 여부
   */
  init: function() {
    if (!this.isSupported()) {
      console.error('이 브라우저는 Server-Sent Events를 지원하지 않습니다.');
      UI.showToast('브라우저가 실시간 업데이트를 지원하지 않습니다. 정기적으로 페이지를 새로고침 해주세요.', 'warning', 5000);
      return false;
    }

    // 이미 연결된 경우 재사용
    if (this.eventSource && this.eventSource.readyState !== EventSource.CLOSED) {
      console.log('SSE: 이미 연결되어 있습니다.');
      return true;
    }

    // 새 연결 생성
    try {
      this.eventSource = new EventSource('/api/sse/subscribe');
      console.log('SSE: 연결 시도...');

      // 연결 성공 이벤트
      this.eventSource.addEventListener('connect', (event) => {
        console.log('SSE: 서버 연결 성공', event.data);
        UI.showToast('실시간 업데이트 연결됨', 'info', 2000);
        this.reconnectAttempts = 0; // 재연결 카운터 초기화

        // 연결 상태 표시
        this.updateConnectionStatus(true);
      });

      // 작업계획 업데이트 이벤트
      this.eventSource.addEventListener('worklog-updated', (event) => {
        try {
          const workLog = JSON.parse(event.data);

          // 현재 데이터에서 해당 항목 찾기 및 업데이트
          const index = workLogData.findIndex(item => item.id === workLog.id);
          if (index !== -1) {
            workLogData[index] = workLog;
            UI.renderWorkLogData(workLogData);
            UI.showToast('작업계획가 업데이트되었습니다', 'info', 1500);
          } else {
            // 현재 데이터에 없는 경우 새로고침 (다른 필터 조건으로 보고 있을 수 있음)
            API.fetchWorkLogsData();
          }
        } catch (error) {
          console.error('SSE: 작업계획 업데이트 처리 중 오류', error);
        }
      });

      // 작업계획 생성 이벤트
      this.eventSource.addEventListener('worklog-created', (event) => {
        try {
          const workLog = JSON.parse(event.data);

          // 필터링 조건에 맞는지 확인 (날짜 필터가 설정된 경우)
          if (selectedDate) {
            const workDate = Utils.formatISOToDisplay(workLog.workDatetime);
            if (!workDate.startsWith(selectedDate)) {
              console.log('SSE: 현재 필터 조건에 맞지 않는 새 작업계획');
              return;
            }
          }

          // 새 항목 추가
          workLogData.unshift(workLog);
          UI.renderWorkLogData(workLogData);
          UI.showToast('새 작업계획가 추가되었습니다', 'info', 1500);
        } catch (error) {
          console.error('SSE: 작업계획 생성 처리 중 오류', error);
        }
      });

      // 작업계획 삭제 이벤트
      this.eventSource.addEventListener('worklog-deleted', (event) => {
        try {
          const data = JSON.parse(event.data);

          // 현재 데이터에서 해당 항목 제거
          const index = workLogData.findIndex(item => item.id === data.id);
          if (index !== -1) {
            workLogData.splice(index, 1);
            UI.renderWorkLogData(workLogData);
            UI.showToast('작업계획가 삭제되었습니다', 'info', 1500);
          }
        } catch (error) {
          console.error('SSE: 작업계획 삭제 처리 중 오류', error);
        }
      });

      // 오류 처리
      this.eventSource.addEventListener('error', (event) => {
        console.error('SSE: 연결 오류 발생', event);
        this.updateConnectionStatus(false);

        if (this.eventSource.readyState === EventSource.CLOSED) {
          console.log('SSE: 연결이 닫혔습니다. 재연결 시도...');
          this.reconnect();
        }
      });

      return true;
    } catch (error) {
      console.error('SSE: 초기화 중 오류 발생', error);
      UI.showToast('실시간 업데이트 연결 실패', 'error', 3000);
      return false;
    }
  },

  /**
   * SSE 연결 종료
   */
  close: function() {
    if (this.eventSource) {
      console.log('SSE: 연결 종료');
      this.eventSource.close();
      this.eventSource = null;
      this.updateConnectionStatus(false);
    }
  },

  /**
   * 재연결 시도
   */
  reconnect: function() {
    this.close();

    // 최대 재시도 횟수 확인
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.warn(`SSE: 최대 재연결 시도 횟수(${this.maxReconnectAttempts}회)를 초과했습니다.`);
      UI.showToast('실시간 업데이트 연결에 실패했습니다. 페이지를 새로고침해주세요.', 'error', 5000);
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * Math.pow(1.5, this.reconnectAttempts - 1); // 지수 백오프

    console.log(`SSE: ${delay}ms 후 재연결 시도 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
    setTimeout(() => {
      if (document.visibilityState === 'visible') {
        this.init();
      } else {
        console.log('SSE: 페이지가 백그라운드 상태입니다. 재연결을 연기합니다.');
        // 문서가 다시 보일 때 재연결 시도
        document.addEventListener('visibilitychange', function onVisibilityChange() {
          if (document.visibilityState === 'visible') {
            document.removeEventListener('visibilitychange', onVisibilityChange);
            SSE.init();
          }
        });
      }
    }, delay);
  },

  /**
   * 브라우저 호환성 확인
   * @returns {boolean} SSE 지원 여부
   */
  isSupported: function() {
    return !!window.EventSource;
  },

  /**
   * 연결 상태 업데이트 및 표시
   * @param {boolean} connected 연결 상태
   */
  updateConnectionStatus: function(connected) {
    const statusElement = document.getElementById('sseStatus');
    if (statusElement) {
      if (connected) {
        statusElement.classList.remove('disconnected');
        statusElement.classList.add('connected');
        statusElement.title = '실시간 업데이트가 연결되었습니다';
      } else {
        statusElement.classList.remove('connected');
        statusElement.classList.add('disconnected');
        statusElement.title = '실시간 업데이트가 연결되지 않았습니다';
      }
    }
  }
};

// 페이지 가시성 변경 감지 (탭 전환 등)
document.addEventListener('visibilitychange', function() {
  if (document.visibilityState === 'visible') {
    // 페이지가 보이면, SSE 연결 확인 및 복구
    if (!SSE.eventSource || SSE.eventSource.readyState === EventSource.CLOSED) {
      console.log('SSE: 페이지가 활성화됨, 연결 복구 시도');
      SSE.init();
    }
  }
});

// beforeunload 이벤트 (페이지 닫기 전)
window.addEventListener('beforeunload', function() {
  // 연결 정리
  SSE.close();
});