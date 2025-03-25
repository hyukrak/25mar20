/**
 * api.js - 작업계획 시스템 API 호출 관련 함수
 * 서버와의 통신을 담당하는 함수들을 포함합니다.
 */

// 클라이언트 ID 생성 (페이지 로드마다 고유한 ID 생성)
const generateClientId = function() {
  // 기기명 + 랜덤 ID + 타임스탬프 조합으로 생성
  const deviceType = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
      ? 'mobile'
      : 'desktop';
  const randomId = Math.random().toString(36).substring(2, 10);
  const timestamp = new Date().getTime();
  return `${deviceType}-${randomId}-${timestamp}`;
};

// 현재 클라이언트 ID 저장 (세션 단위로 유지)
const CLIENT_ID = sessionStorage.getItem('clientId') || generateClientId();
sessionStorage.setItem('clientId', CLIENT_ID);

console.log(`현재 클라이언트 ID: ${CLIENT_ID}`);

// API 네임스페이스 생성
const API = {
  /**
   * 작업계획 데이터 가져오기
   * @returns {Promise} API 호출 결과 Promise
   */
  fetchWorkLogsData: function() {
    // 날짜 필터가 활성화되어 있는지 확인
    if (selectedDate && Utils.isShortDateFormat(selectedDate)) {
      // 날짜 필터가 있으면 fetchWorkLogsByDate 함수를 대신 사용
      return this.fetchWorkLogsByDate(selectedDate);
    }

    // 날짜 필터가 활성화되어 있지 않으면 일반적인 데이터 로딩 계속 진행
    loading = true;
    document.getElementById('loading').style.display = 'block';

    // 정렬 상태 유지 (기본 정렬은 ASC)
    const params = new URLSearchParams();
    if (currentSortField) params.append('sortField', currentSortField);
    params.append('sortDirection', currentSortDirection || 'ASC');
    if (currentStatus) params.append('status', currentStatus);

    // API 호출
    return fetch(`/api/worklogs?${params.toString()}`)
    .then(response => {
      if (!response.ok) {
        throw new Error('서버 응답 오류: ' + response.status);
      }
      return response.json();
    })
    .then(data => {
      // API 응답 데이터 처리
      if (data && Array.isArray(data)) {
        workLogData = data;
      } else if (data && data.workLogs && Array.isArray(data.workLogs)) {
        workLogData = data.workLogs;
      } else {
        workLogData = [];
        console.warn('API 응답 데이터 형식이 예상과 다릅니다:', data);
      }

      // 렌더링
      UI.renderWorkLogData(workLogData);
    })
    .catch(error => {
      console.error('데이터 로드 오류:', error);
      UI.showToast('데이터 로드 중 오류가 발생했습니다: ' + error.message, 'error');
    })
    .finally(() => {
      // 로딩 표시 종료
      loading = false;
      document.getElementById('loading').style.display = 'none';
    });
  },

  /**
   * 특정 날짜의 작업계획 데이터 가져오기
   * @param {string} date - 조회할 날짜 ('YY.MM.DD' 형식)
   * @param {string} sortField - 정렬 필드
   * @param {string} sortDirection - 정렬 방향 ('ASC' 또는 'DESC')
   * @param {string} status - 상태 필터 ('completed', 'incomplete', null)
   * @returns {Promise} API 호출 결과 Promise
   */
  fetchWorkLogsByDate: function(date, sortField, sortDirection, status) {
    // 로딩 표시 시작
    loading = true;
    document.getElementById('loading').style.display = 'block';

    // 날짜 파라미터 검증
    let validDate;

    if (!date || !Utils.isShortDateFormat(date)) {
      // 유효하지 않은 날짜 형식이면 현재 날짜 사용
      validDate = Utils.formatDateForSearch(new Date());
      console.warn('유효하지 않은 날짜 형식. 현재 날짜로 대체:', validDate);
      UI.showToast('날짜 형식이 올바르지 않습니다 (YY.MM.DD 형식이어야 합니다).', 'warning');
    } else {
      validDate = date;
    }

    // 날짜 형식 변환 (YY.MM.DD -> YYYY-MM-DD)
    const isoDate = Utils.convertToISODate(validDate);

    // API 호출 (정렬 필드와 방향을 파라미터에 추가)
    const params = new URLSearchParams();
    if (sortField) params.append('sortField', sortField);
    params.append('sortDirection', sortDirection || 'ASC');

    // 상태 필터 추가
    if (status) params.append('status', status);

    return fetch(`/api/worklogs/date/${isoDate}?${params.toString()}`)
    .then(response => {
      if (!response.ok) {
        throw new Error('서버 응답 오류: ' + response.status);
      }
      return response.json();
    })
    .then(data => {
      // API 응답 데이터 처리
      if (data && Array.isArray(data)) {
        workLogData = data;
      } else if (data && data.workLogs && Array.isArray(data.workLogs)) {
        workLogData = data.workLogs;
      } else {
        workLogData = [];
        console.warn('API 응답 데이터 형식이 예상과 다릅니다:', data);
      }

      UI.renderWorkLogData(workLogData);

      // 결과 메시지 표시
      if (workLogData.length > 0) {
        UI.showToast(`${date} 날짜의 작업 내역 ${workLogData.length}건이 조회되었습니다.`, 'info');
      } else {
        UI.showToast(`${date} 날짜의 작업 내역이 없습니다.`, 'info');
      }
    })
    .catch(error => {
      console.error('데이터 로드 오류:', error);

      if (error.message !== 'Invalid date format') {
        UI.showToast('날짜별 데이터 로드 중 오류가 발생했습니다: ' + error.message, 'error');
      }

      // 개발 단계에서는 모든 데이터에서 날짜에 해당하는 항목만 필터링
      const filteredData = workLogData.filter(item => {
        return item.workDatetime && item.workDatetime.startsWith(date);
      });

      workLogData = filteredData;
      UI.renderWorkLogData(filteredData);

      // 결과 메시지 표시
      if (filteredData.length > 0) {
        UI.showToast(`${date} 날짜의 작업 내역 ${filteredData.length}건이 조회되었습니다. (클라이언트 필터링)`, 'info');
      } else {
        UI.showToast(`${date} 날짜의 작업 내역이 없습니다.`, 'info');
      }
    })
    .finally(() => {
      // 로딩 표시 종료
      loading = false;
      document.getElementById('loading').style.display = 'none';
    });
  },

  /**
   * 필터링 및 정렬 조건으로 작업계획 데이터 가져오기
   * @param {string} status - 상태 필터 ('completed', 'incomplete', null)
   * @param {string} sortField - 정렬 필드
   * @param {string} sortDirection - 정렬 방향 ('ASC', 'DESC')
   * @returns {Promise} API 호출 결과 Promise
   */
  fetchFilteredWorkLogs: function(status, sortField, sortDirection) {
    // 로딩 표시 시작
    loading = true;
    document.getElementById('loading').style.display = 'block';

    // 쿼리 파라미터 구성 (정렬 방향 기본값 ASC 적용)
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (sortField) params.append('sortField', sortField);
    params.append('sortDirection', sortDirection || 'ASC');

    // 날짜 필터 검증
    if (selectedDate) {
      if (Utils.isShortDateFormat(selectedDate)) {
        // 유효한 형식이면 날짜 API 사용 (정렬 필드와 방향 및 상태 필터도 함께 전달)
        return this.fetchWorkLogsByDate(selectedDate, sortField, sortDirection, status);
      } else {
        // 유효하지 않은 형식이면 로그 기록하고 현재 날짜로 대체
        console.warn('유효하지 않은 selectedDate 형식:', selectedDate);
        UI.showToast('날짜 형식이 올바르지 않습니다 (YY.MM.DD 형식이어야 합니다).', 'warning');
        selectedDate = Utils.formatDateForSearch(new Date());
        return this.fetchWorkLogsByDate(selectedDate, sortField, sortDirection, status);
      }
    }

    // API 호출
    return fetch(`/api/worklogs?${params.toString()}`)
    .then(response => {
      if (!response.ok) {
        throw new Error('서버 응답 오류: ' + response.status);
      }
      return response.json();
    })
    .then(data => {
      // API 응답 데이터 처리
      if (data && Array.isArray(data)) {
        workLogData = data;
      } else if (data && data.workLogs && Array.isArray(data.workLogs)) {
        workLogData = data.workLogs;
      } else {
        workLogData = [];
        console.warn('API 응답 데이터 형식이 예상과 다릅니다:', data);
      }

      UI.renderWorkLogData(workLogData);
    })
    .catch(error => {
      console.error('데이터 로드 오류:', error);
      UI.showToast('필터링된 데이터 로드 중 오류가 발생했습니다: ' + error.message, 'error');

      // 개발 단계에서는 클라이언트 사이드 필터링 및 정렬 적용
      let filteredData = [...workLogData];

      // 상태 필터링
      if (status === 'completed') {
        filteredData = filteredData.filter(item => item.completedAt);
      } else if (status === 'incomplete') {
        filteredData = filteredData.filter(item => !item.completedAt);
      }

      // 날짜 필터링
      if (selectedDate) {
        filteredData = filteredData.filter(item => {
          return item.workDatetime && item.workDatetime.startsWith(selectedDate);
        });
      }

      // 정렬
      if (sortField) {
        filteredData.sort((a, b) => {
          let valA = a[sortField] || '';
          let valB = b[sortField] || '';

          // 날짜 또는 문자열 비교
          const compareResult = String(valA).localeCompare(String(valB));

          // 정렬 방향에 따라 결과 조정
          return sortDirection === 'ASC' ? compareResult : -compareResult;
        });
      }

      UI.renderWorkLogData(filteredData);
    })
    .finally(() => {
      // 로딩 표시 종료
      loading = false;
      document.getElementById('loading').style.display = 'none';
    });
  },

  /**
   * 작업계획 생성
   * @param {Object} workLog - 생성할 작업계획 데이터
   * @returns {Promise} API 호출 결과 Promise
   */
  createWorkLog: function(workLog) {
    return fetch('/api/worklogs', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(workLog)
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('서버 응답 오류: ' + response.status);
      }
      return response.json();
    })
    .then(data => {
      UI.showToast('작업 내역이 생성되었습니다.', 'success');
      return this.fetchWorkLogsData(); // 데이터 새로 불러오기
    })
    .catch(error => {
      console.error('생성 오류:', error);
      UI.showToast('생성 중 오류가 발생했습니다: ' + error.message, 'error');
      throw error;
    });
  },

  /**
   * 작업계획 수정
   * @param {Object} workLog - 수정할 작업계획 데이터
   * @returns {Promise} API 호출 결과 Promise
   */
  updateWorkLog: function(workLog) {
    return fetch(`/api/worklogs/${workLog.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'X-Client-ID': CLIENT_ID // 클라이언트 ID 헤더
      },
      body: JSON.stringify(workLog)
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('서버 응답 오류: ' + response.status);
      }
      return response.json();
    })
    .then(data => {
      UI.showToast('작업 내역이 수정되었습니다.', 'success');
      return this.fetchWorkLogsData(); // 데이터 새로 불러오기
    })
    .catch(error => {
      console.error('수정 오류:', error);
      UI.showToast('수정 중 오류가 발생했습니다: ' + error.message, 'error');
      throw error;
    });
  },

  /**
   * 작업계획 완료 상태 업데이트
   * @param {number} id - 작업계획 ID
   * @param {boolean} completed - 완료 여부
   * @returns {Promise} API 호출 결과 Promise
   */
  updateWorkLogStatus: function(id, completed) {
    return fetch(`/api/worklogs/${id}/status`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'X-Client-ID': CLIENT_ID // 클라이언트 ID 헤더
      },
      body: JSON.stringify({ completed: completed })
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('서버 응답 오류: ' + response.status);
      }
      return response.json();
    })
    .then(data => {
      UI.showToast(data.message || `작업이 ${completed ? '완료' : '미완료'} 상태로 변경되었습니다.`, 'success');
      return true;
    })
    .catch(error => {
      console.error('상태 업데이트 오류:', error);
      UI.showToast('상태 업데이트 중 오류가 발생했습니다: ' + error.message, 'error');

      // 개발용 더미 데이터 상태 업데이트 (실제 API 연동 전까지만 사용)
      const index = workLogData.findIndex(item => item.id == id);
      if (index !== -1) {
        workLogData[index].completedAt = completed ? new Date().toISOString() : null;
        workLogData[index].completedBy = CLIENT_ID; // 클라이언트 ID 추가
        UI.renderWorkLogData(workLogData);
        UI.showToast(`작업이 ${completed ? '완료' : '미완료'} 상태로 변경되었습니다.`, 'success');
      }

      throw error;
    });
  },

  /**
   * 작업계획 삭제
   * @param {Array} ids - 삭제할 작업계획 ID 배열
   * @returns {Promise} API 호출 결과 Promise
   */
  deleteWorkLogs: function(ids) {
    const deletePromises = ids.map(id =>
        fetch(`/api/worklogs/${id}`, {
          method: 'DELETE'
        })
    );

    return Promise.all(deletePromises)
    .then(responses => {
      const allSuccessful = responses.every(response => response.ok);
      if (allSuccessful) {
        UI.showToast('선택한 작업계획가 삭제되었습니다.', 'success');
        return this.fetchWorkLogsData(); // 데이터 새로 불러오기
      } else {
        throw new Error('일부 항목 삭제 중 오류가 발생했습니다.');
      }
    })
    .catch(error => {
      console.error('삭제 오류:', error);
      UI.showToast('삭제 중 오류가 발생했습니다: ' + error.message, 'error');
      throw error;
    });
  },

  /**
   * 파일 업로드
   * @param {FormData} formData - 업로드할 파일 데이터
   * @returns {Promise} API 호출 결과 Promise
   */
  uploadFile: function(formData) {
    UI.showToast('파일을 업로드 중입니다...', 'info', 10000); // 10초 타임아웃

    return fetch('/excel/upload', {
      method: 'POST',
      body: formData
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('서버 응답 오류: ' + response.status);
      }

      // 리다이렉트 응답이면
      if (response.redirected) {
        UI.showToast('파일이 성공적으로 업로드되었습니다.', 'success');
        setTimeout(() => {
          window.location.href = response.url;
        }, 1500);
        return null;
      }

      return response.json();
    })
    .then(data => {
      if (data) {
        if (data.success) {
          UI.showToast(data.message || '파일이 성공적으로 업로드되었습니다.', 'success');
          return this.fetchWorkLogsData(); // 데이터 새로고침
        } else {
          throw new Error(data.message || '업로드 중 오류가 발생했습니다.');
        }
      }
      return null;
    })
    .catch(error => {
      console.error('업로드 오류:', error);
      UI.showToast('서버 요청 중 오류가 발생했습니다: ' + error.message, 'error');
      throw error;
    });
  }
};