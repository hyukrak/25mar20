/**
 * 작업 로그 API 클라이언트 - 프론트엔드 AJAX 호출용
 */
const WorkLogApi = {
  /**
   * 작업 로그 목록 조회 (필터링 및 페이징)
   * @param {Object} params - 쿼리 매개변수
   * @returns {Promise<Object>} 작업 로그 목록 및 페이징 정보
   */
  getWorkLogs: async (params = {}) => {
    // 쿼리 문자열 생성
    const queryParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        queryParams.append(key, value);
      }
    });

    const response = await fetch(`/api/worklogs?${queryParams.toString()}`);
    if (!response.ok) {
      throw new Error(`작업 로그 목록 조회 실패: ${response.statusText}`);
    }
    return response.json();
  },

  /**
   * ID로 작업 로그 상세 조회
   * @param {number} id - 작업 로그 ID
   * @returns {Promise<Object>} 작업 로그 상세 정보
   */
  getWorkLog: async (id) => {
    const response = await fetch(`/api/worklogs/${id}`);
    if (!response.ok) {
      throw new Error(`작업 로그 조회 실패: ${response.statusText}`);
    }
    return response.json();
  },

  /**
   * 새 작업 로그 생성
   * @param {Object} workLog - 작업 로그 데이터
   * @returns {Promise<Object>} 생성된 작업 로그 ID
   */
  createWorkLog: async (workLog) => {
    const response = await fetch('/api/worklogs', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(workLog)
    });

    if (!response.ok) {
      throw new Error(`작업 로그 생성 실패: ${response.statusText}`);
    }
    return response.json();
  },

  /**
   * 기존 작업 로그 업데이트
   * @param {number} id - 작업 로그 ID
   * @param {Object} workLog - 업데이트할 작업 로그 데이터
   * @returns {Promise<void>}
   */
  updateWorkLog: async (id, workLog) => {
    const response = await fetch(`/api/worklogs/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(workLog)
    });

    if (!response.ok) {
      throw new Error(`작업 로그 업데이트 실패: ${response.statusText}`);
    }
  },

  /**
   * 작업 로그 삭제
   * @param {number} id - 작업 로그 ID
   * @returns {Promise<void>}
   */
  deleteWorkLog: async (id) => {
    const response = await fetch(`/api/worklogs/${id}`, {
      method: 'DELETE'
    });

    if (!response.ok) {
      throw new Error(`작업 로그 삭제 실패: ${response.statusText}`);
    }
  },

  /**
   * 날짜 범위로 작업 로그 목록 조회
   * @param {string} startDate - ISO 날짜 문자열
   * @param {string} endDate - ISO 날짜 문자열
   * @returns {Promise<Array>} 작업 로그 목록
   */
  getWorkLogsByDateRange: async (startDate, endDate) => {
    const queryParams = new URLSearchParams({
      startDate,
      endDate
    });

    const response = await fetch(`/api/worklogs/by-date-range?${queryParams.toString()}`);
    if (!response.ok) {
      throw new Error(`날짜 범위로 작업 로그 조회 실패: ${response.statusText}`);
    }
    return response.json();
  },

  /**
   * 차량 모델로 작업 로그 목록 조회
   * @param {string} carModel - 차량 모델명
   * @returns {Promise<Array>} 작업 로그 목록
   */
  getWorkLogsByCarModel: async (carModel) => {
    const response = await fetch(`/api/worklogs/by-car-model/${encodeURIComponent(carModel)}`);
    if (!response.ok) {
      throw new Error(`차량 모델로 작업 로그 조회 실패: ${response.statusText}`);
    }
    return response.json();
  },

  /**
   * 제품 코드로 작업 로그 목록 조회
   * @param {string} productCode - 제품 코드
   * @returns {Promise<Array>} 작업 로그 목록
   */
  getWorkLogsByProductCode: async (productCode) => {
    const response = await fetch(`/api/worklogs/by-product-code/${encodeURIComponent(productCode)}`);
    if (!response.ok) {
      throw new Error(`제품 코드로 작업 로그 조회 실패: ${response.statusText}`);
    }
    return response.json();
  },

  /**
   * 상태별 작업 로그 목록 조회
   * @param {string} status - 상태 (pending, in_progress, completed, rejected 등)
   * @returns {Promise<Array>} 작업 로그 목록
   */
  getWorkLogsByStatus: async (status) => {
    const response = await fetch(`/api/worklogs/by-status/${encodeURIComponent(status)}`);
    if (!response.ok) {
      throw new Error(`상태별 작업 로그 조회 실패: ${response.statusText}`);
    }
    return response.json();
  },

  /**
   * 사용자 ID로 작업 로그 목록 조회
   * @param {number} userId - 사용자 ID
   * @returns {Promise<Array>} 작업 로그 목록
   */
  getWorkLogsByUserId: async (userId) => {
    const response = await fetch(`/api/worklogs/by-user/${userId}`);
    if (!response.ok) {
      throw new Error(`사용자별 작업 로그 조회 실패: ${response.statusText}`);
    }
    return response.json();
  },

  /**
   * 작업 로그 통계 데이터 조회
   * @param {Object} params - 쿼리 매개변수
   * @returns {Promise<Object>} 작업 로그 통계 데이터
   */
  getWorkLogStats: async (params = {}) => {
    const queryParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        queryParams.append(key, value);
      }
    });

    const response = await fetch(`/api/worklogs/stats?${queryParams.toString()}`);
    if (!response.ok) {
      throw new Error(`작업 로그 통계 조회 실패: ${response.statusText}`);
    }
    return response.json();
  }
};

// ES 모듈에서 사용
export default WorkLogApi;

// 비모듈 스크립트에서 사용
window.WorkLogApi = WorkLogApi;