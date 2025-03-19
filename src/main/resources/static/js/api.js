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

    // API 호출
    return fetch('/api/worklogs')
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

      // 필터링된 날짜가 있으면 적용, 없으면 전체 데이터 렌더링
      if (selectedDate) {
        UI.filterDataByDate(selectedDate);
      } else {
        UI.renderWorkLogData(workLogData);
      }
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
   * 추가 작업 로그 데이터 가져오기 (무한 스크롤용)
   * @param {number} page - 페이지 번호
   * @param {number} size - 페이지 크기
   * @returns {Promise} API 호출 결과 Promise
   */
  fetchMoreWorkLogsData: function(page, size) {
    loading = true;
    document.getElementById('loading').style.display = 'block';

    return fetch(`/api/worklogs?page=${page}&size=${size}`)
    .then(response => response.json())
    .then(data => {
      if (data && Array.isArray(data) && data.length > 0) {
        workLogData = [...workLogData, ...data];
        UI.renderWorkLogData(workLogData);
      } else if (data && data.workLogs && Array.isArray(data.workLogs) && data.workLogs.length > 0) {
        workLogData = [...workLogData, ...data.workLogs];
        UI.renderWorkLogData(workLogData);
      } else {
        // 더 이상 데이터가 없음
        hasMoreData = false;
      }
    })
    .catch(error => {
      console.error('추가 데이터 로드 오류:', error);
      UI.showToast('추가 데이터 로드 중 오류가 발생했습니다.', 'error');
    })
    .finally(() => {
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
    // 실제 API 구현 시 주석 해제
    /*
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
    */

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
    return Promise.resolve();
  },

  /**
   * 작업 로그 수정
   * @param {Object} workLog - 수정할 작업 로그 데이터
   * @returns {Promise} API 호출 결과 Promise
   */
  updateWorkLog: function(workLog) {
    // 실제 API 구현 시 주석 해제
    /*
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
      throw error;
    });
    */

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
    return Promise.resolve();
  },

  /**
   * 작업 로그 삭제
   * @param {Array} ids - 삭제할 작업 로그 ID 배열
   * @returns {Promise} API 호출 결과 Promise
   */
  deleteWorkLogs: function(ids) {
    // 실제 API 구현 시 주석 해제
    /*
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
        throw error;
      });
    */

    // 개발용 더미 데이터 삭제 (실제 API 연동 전까지만 사용)
    Utils.deleteDummyData(ids);
    UI.showToast('선택한 작업 로그가 삭제되었습니다.', 'error');
    return Promise.resolve();
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

      // 리다이렉트 응답이면 페이지 새로고침
      if (response.redirected) {
        window.location.href = response.url;
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