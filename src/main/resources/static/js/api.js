/**
 * api.js - 작업 로그 시스템 API 호출 관련 함수
 * 서버와의 통신을 담당하는 함수들을 포함합니다.
 */

// API 네임스페이스 생성
const API = {
  /**
   * 작업 로그 데이터 가져오기
   * @returns {Promise} API 호출 결과 Promise
   */
  fetchWorkLogsData: function() {
    // 로딩 표시 시작
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

      // 오류 발생 시 더미 데이터 생성 (개발 중에만 사용)
      workLogData = Utils.generateDummyData(40);
      UI.renderWorkLogData(workLogData);
    })
    .finally(() => {
      // 로딩 표시 종료
      loading = false;
      document.getElementById('loading').style.display = 'none';
    });
  },

  /**
   * 특정 날짜의 작업 로그 데이터 가져오기
   * @param {string} date - 조회할 날짜 ('YY.MM.DD' 형식)
   * @returns {Promise} API 호출 결과 Promise
   */
  fetchWorkLogsByDate: function(date) {
    // 로딩 표시 시작
    loading = true;
    document.getElementById('loading').style.display = 'block';

    // YY.MM.DD 형식의 날짜 확인
    if (!Utils.isShortDateFormat(date)) {
      UI.showToast('날짜 형식이 올바르지 않습니다 (YY.MM.DD 형식이어야 합니다).', 'warning');
      loading = false;
      document.getElementById('loading').style.display = 'none';
      return Promise.reject(new Error('Invalid date format'));
    }

    // 날짜 형식 변환 (YY.MM.DD -> YYYY-MM-DD)
    const isoDate = Utils.convertToISODate(date);

    // API 호출 (정렬 방향 기본값 ASC 적용)
    const params = new URLSearchParams();
    params.append('sortDirection', 'ASC');

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
   * 필터링 및 정렬 조건으로 작업 로그 데이터 가져오기
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

    // 날짜 필터가 있으면 추가
    if (selectedDate && Utils.isShortDateFormat(selectedDate)) {
      // 검색용 날짜 변환 (YY.MM.DD -> YYYY-MM-DD)
      const isoDate = Utils.convertToISODate(selectedDate);

      // date/{date} 형식의 API를 사용
      return this.fetchWorkLogsByDate(selectedDate);
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
   * 작업 로그 생성
   * @param {Object} workLog - 생성할 작업 로그 데이터
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

      // 개발용 더미 데이터 추가 (실제 API 연동 전까지만 사용)
      Utils.addDummyData(
          workLog.workDatetime,
          workLog.carModel,
          workLog.productColor,
          workLog.productCode,
          workLog.productName,
          workLog.quantity
      );
      UI.showToast('작업 내역이 생성되었습니다.', 'success');

      throw error;
    });
  },

  /**
   * 작업 로그 수정
   * @param {Object} workLog - 수정할 작업 로그 데이터
   * @returns {Promise} API 호출 결과 Promise
   */
  updateWorkLog: function(workLog) {
    return fetch(`/api/worklogs/${workLog.id}`, {
      method: 'PUT',
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
      UI.showToast('작업 내역이 수정되었습니다.', 'success');
      return this.fetchWorkLogsData(); // 데이터 새로 불러오기
    })
    .catch(error => {
      console.error('수정 오류:', error);
      UI.showToast('수정 중 오류가 발생했습니다: ' + error.message, 'error');

      // 개발용 더미 데이터 수정 (실제 API 연동 전까지만 사용)
      Utils.updateDummyData(
          workLog.id,
          workLog.workDatetime,
          workLog.carModel,
          workLog.productColor,
          workLog.productCode,
          workLog.productName,
          workLog.quantity
      );
      UI.showToast('작업 내역이 수정되었습니다.', 'success');

      throw error;
    });
  },

  /**
   * 작업 로그 완료 상태 업데이트
   * @param {number} id - 작업 로그 ID
   * @param {boolean} completed - 완료 여부
   * @returns {Promise} API 호출 결과 Promise
   */
  updateWorkLogStatus: function(id, completed) {
    return fetch(`/api/worklogs/${id}/status`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
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
        UI.renderWorkLogData(workLogData);
        UI.showToast(`작업이 ${completed ? '완료' : '미완료'} 상태로 변경되었습니다.`, 'success');
      }

      throw error;
    });
  },

  /**
   * 작업 로그 삭제
   * @param {Array} ids - 삭제할 작업 로그 ID 배열
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
        UI.showToast('선택한 작업 로그가 삭제되었습니다.', 'success');
        return this.fetchWorkLogsData(); // 데이터 새로 불러오기
      } else {
        throw new Error('일부 항목 삭제 중 오류가 발생했습니다.');
      }
    })
    .catch(error => {
      console.error('삭제 오류:', error);
      UI.showToast('삭제 중 오류가 발생했습니다: ' + error.message, 'error');

      // 개발용 더미 데이터 삭제 (실제 API 연동 전까지만 사용)
      Utils.deleteDummyData(ids);
      UI.showToast('선택한 작업 로그가 삭제되었습니다.', 'success');

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