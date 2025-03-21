/**
 * utils.js - 작업 로그 시스템 유틸리티 함수
 * 날짜 포맷팅 및 기타 유틸리티 함수들을 포함합니다.
 */

// Utils 네임스페이스 생성
const Utils = {
  /**
   * 날짜 포맷팅 함수 - YY.MM.DD HH:MM 형식
   * @param {Date} date - 포맷팅할 날짜 객체
   * @returns {string} 포맷팅된 날짜 문자열 (YY.MM.DD HH:MM 형식)
   */
  formatDateTime: function(date) {
    const year = String(date.getFullYear()).slice(-2); // 마지막 두 자리만 추출 (2025 -> 25)
    const month = String(date.getMonth() + 1).padStart(2, '0'); // 월 (0-11이므로 +1)
    const day = String(date.getDate()).padStart(2, '0'); // 일
    const hours = String(date.getHours()).padStart(2, '0'); // 시간 (24시간제)
    const minutes = String(date.getMinutes()).padStart(2, '0'); // 분

    return `${year}.${month}.${day} ${hours}:${minutes}`;
  },

  /**
   * 날짜 포맷팅 함수 - YY.MM.DD 형식 (검색용)
   * @param {Date} date - 포맷팅할 날짜 객체
   * @returns {string} 포맷팅된 날짜 문자열 (YY.MM.DD 형식)
   */
  formatDateForSearch: function(date) {
    const year = String(date.getFullYear()).slice(-2); // 마지막 두 자리만 추출 (2025 -> 25)
    const month = String(date.getMonth() + 1).padStart(2, '0'); // 월 (0-11이므로 +1)
    const day = String(date.getDate()).padStart(2, '0'); // 일

    return `${year}.${month}.${day}`;
  },

  /**
   * 오늘 날짜 반환 함수 (YY.MM.DD 형식)
   * @returns {string} 오늘 날짜 문자열
   */
  getTodayString: function() {
    const today = new Date();
    return this.formatDateForSearch(today);
  },

  /**
   * 날짜 문자열 변환 함수 (YY.MM.DD -> YYYY-MM-DD)
   * @param {string} dateStr - 변환할 날짜 문자열 (YY.MM.DD 형식)
   * @returns {string} 변환된 날짜 문자열 (YYYY-MM-DD 형식)
   */
  convertToISODate: function(dateStr) {
    if (!dateStr) return '';

    // YY.MM.DD 형식인 경우
    const match = dateStr.match(/^(\d{2})\.(\d{2})\.(\d{2})$/);
    if (match) {
      const year = parseInt(match[1]) + 2000; // 2자리 연도를 4자리로 변환 (25 -> 2025)
      const month = match[2];
      const day = match[3];
      return `${year}-${month}-${day}`;
    }

    return dateStr; // 변환할 수 없는 경우 원래 문자열 반환
  },

  /**
   * 날짜 문자열 변환 함수 (YYYY-MM-DD -> YY.MM.DD)
   * @param {string} dateStr - 변환할 날짜 문자열 (YYYY-MM-DD 형식)
   * @returns {string} 변환된 날짜 문자열 (YY.MM.DD 형식)
   */
  convertFromISODate: function(dateStr) {
    if (!dateStr) return '';

    // YYYY-MM-DD 형식인 경우
    const match = dateStr.match(/^(\d{4})-(\d{2})-(\d{2})$/);
    if (match) {
      const year = String(match[1]).slice(-2); // 마지막 두 자리만 추출 (2025 -> 25)
      const month = match[2];
      const day = match[3];
      return `${year}.${month}.${day}`;
    }

    return dateStr; // 변환할 수 없는 경우 원래 문자열 반환
  },

  /**
   * 날짜 형식 검사 함수
   * @param {string} dateStr - 검사할 날짜 문자열
   * @returns {boolean} YY.MM.DD 형식이면 true, 아니면 false
   */
  isShortDateFormat: function(dateStr) {
    if (!dateStr) return false;

    // YY.MM.DD 형식 확인
    return /^\d{2}\.\d{2}\.\d{2}$/.test(dateStr);
  },

  // =====================================================
  // 개발용 더미 데이터 관련 함수들 - API 연동 완료 후 제거
  // =====================================================

  /**
   * 더미 데이터 생성 함수 (API 연동 전까지만 사용)
   * @param {number} count - 생성할 데이터 개수
   * @returns {Array} 생성된 더미 데이터 배열
   */
  generateDummyData: function(count) {
    const data = [];
    const carModels = ['MX5a-분리', 'AR1 조립', 'ON SUB', 'ON 조립'];
    const productColors = ['RED', 'BLUE', 'BLACK', 'WHITE', 'SILVER'];
    const productCodes = [
      '82650R6400PE2',
      '62510Q5000',
      '86350H1000',
      '97250J9000',
      '82651R2100',
      '92101Q5000',
      '86150R6000',
      '64400Q7000',
      '85790Q7100',
    ];
    const productNames = [
      '프론트 범퍼',
      '라디에이터 서포트',
      '사이드 미러',
      '트렁크 힌지',
      '리어 범퍼',
      '헤드라이트 어셈블리',
      '와이퍼 어셈블리',
      '콘솔 어셈블리',
      '기어 노브',
    ];

    for (let i = 1; i <= count; i++) {
      const date = new Date();
      date.setDate(date.getDate() - Math.floor(Math.random() * 30)); // 최근 30일 내
      date.setHours(Math.floor(Math.random() * 24));
      date.setMinutes(Math.floor(Math.random() * 60));

      const workDate = new Date(date);
      const createdAt = new Date(date);
      createdAt.setHours(
          createdAt.getHours() + Math.floor(Math.random() * 5)
      );

      const codeIndex = Math.floor(Math.random() * productCodes.length);

      // 30% 확률로 완료 상태 설정
      const isCompleted = Math.random() < 0.3;
      const completedAt = isCompleted ? new Date(date.getTime() + (Math.random() * 86400000)) : null;

      data.push({
        id: i,
        workDatetime: this.formatDateTime(workDate),
        carModel: carModels[Math.floor(Math.random() * carModels.length)],
        productColor: productColors[Math.floor(Math.random() * productColors.length)],
        productCode: productCodes[codeIndex],
        productName: productNames[codeIndex],
        quantity: Math.floor(Math.random() * 50) + 1,
        completedAt: completedAt ? completedAt.toISOString() : null,
        createdAt: createdAt.toISOString(),
      });
    }

    return data;
  },

  /**
   * 더미 데이터 업데이트 함수 (API 연동 전까지만 사용)
   * @param {*} id - 업데이트할 항목의 ID
   * @param {string} workDatetime - 작업 시간
   * @param {string} carModel - 차종
   * @param {string} productColor - 제품 색상
   * @param {string} productCode - 제품 코드
   * @param {string} productName - 제품명
   * @param {number} quantity - 수량
   */
  updateDummyData: function(
      id,
      workDatetime,
      carModel,
      productColor,
      productCode,
      productName,
      quantity
  ) {
    const index = workLogData.findIndex((item) => item.id == id);
    if (index !== -1) {
      workLogData[index].workDatetime = workDatetime;
      workLogData[index].carModel = carModel;
      workLogData[index].productColor = productColor;
      workLogData[index].productCode = productCode;
      workLogData[index].productName = productName;
      workLogData[index].quantity = quantity;

      // 완료 상태는 별도로 업데이트하기 위해 유지

      // 화면 갱신
      UI.renderWorkLogData(workLogData);
    }
  },

  /**
   * 더미 데이터 추가 함수 (API 연동 전까지만 사용)
   * @param {string} workDatetime - 작업 시간
   * @param {string} carModel - 차종
   * @param {string} productColor - 제품 색상
   * @param {string} productCode - 제품 코드
   * @param {string} productName - 제품명
   * @param {number} quantity - 수량
   */
  addDummyData: function(
      workDatetime,
      carModel,
      productColor,
      productCode,
      productName,
      quantity
  ) {
    const newId =
        workLogData.length > 0
            ? Math.max(...workLogData.map((item) => item.id)) + 1
            : 1;
    const now = new Date();

    workLogData.unshift({
      id: newId,
      workDatetime: workDatetime,
      carModel: carModel,
      productColor: productColor,
      productCode: productCode,
      productName: productName,
      quantity: quantity,
      completedAt: null, // 생성 시 미완료 상태
      createdAt: now.toISOString(),
    });

    // 화면 갱신
    UI.renderWorkLogData(workLogData);
  },

  /**
   * 더미 데이터 삭제 함수 (API 연동 전까지만 사용)
   * @param {Array} ids - 삭제할 ID 배열
   */
  deleteDummyData: function(ids) {
    ids.forEach((id) => {
      const index = workLogData.findIndex((item) => item.id == id);
      if (index !== -1) {
        workLogData.splice(index, 1);
      }
    });

    // 화면 갱신
    UI.renderWorkLogData(workLogData);
  },

  /**
   * ISO 형식의 날짜 문자열을 화면 표시용 'YY.MM.DD HH:mm' 형식으로 변환
   * @param {string} isoDateStr - ISO 형식 날짜 문자열 (예: "2025-03-21T14:30:00")
   * @returns {string} 변환된 날짜 문자열 (예: "25.03.21 14:30")
   */
  formatISOToDisplay: function(isoDateStr) {
    if (!isoDateStr) return '';

    try {
      const date = new Date(isoDateStr);

      // 유효한 날짜인지 확인
      if (isNaN(date.getTime())) {
        return isoDateStr; // 변환 실패 시 원본 반환
      }

      const year = String(date.getFullYear()).slice(-2); // 2자리 연도
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');

      return `${year}.${month}.${day} ${hours}:${minutes}`;
    } catch (e) {
      console.error('날짜 변환 오류:', e);
      return isoDateStr; // 오류 시 원본 반환
    }
  },

  /**
   * 'YY.MM.DD' 형식의 날짜 문자열을 ISO 형식으로 변환 (검색용)
   * @param {string} shortDateStr - 'YY.MM.DD' 형식 문자열
   * @returns {string} ISO 날짜 문자열 (YYYY-MM-DD)
   */
  shortDateToISO: function(shortDateStr) {
    if (!shortDateStr) return '';

    // YY.MM.DD 형식 체크
    const match = shortDateStr.match(/^(\d{2})\.(\d{2})\.(\d{2})$/);
    if (!match) return shortDateStr;

    const year = parseInt('20' + match[1]); // 2자리 연도를 4자리로 확장
    const month = match[2];
    const day = match[3];

    return `${year}-${month}-${day}`;
  }
};