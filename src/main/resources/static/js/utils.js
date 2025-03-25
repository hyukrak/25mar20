/**
 * utils.js - 작업계획 시스템 유틸리티 함수
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