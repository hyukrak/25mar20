/**
 * 테이블 스타일을 직접 JavaScript로 적용하는 함수
 * CSS가 제대로 적용되지 않을 때 사용
 */
function applyTableStyles() {
  // 제브라 스타일 적용
  document.querySelectorAll('.table tbody tr:nth-child(even)').forEach(row => {
    row.style.backgroundColor = '#f3f0ec'; // --light-beige
  });

  document.querySelectorAll('.table tbody tr:nth-child(odd)').forEach(row => {
    row.style.backgroundColor = '#ffffff';
  });

  // hover 이벤트를 위한 이벤트 리스너 추가
  document.querySelectorAll('.table tbody tr').forEach(row => {
    row.addEventListener('mouseenter', () => {
      row.style.backgroundColor = '#d9d9d9'; // --medium-gray
      row.style.cursor = 'pointer';
    });

    row.addEventListener('mouseleave', () => {
      if (row.selectorIndex % 2 === 0) {
        row.style.backgroundColor = '#f3f0ec'; // --light-beige for even rows
      } else {
        row.style.backgroundColor = '#ffffff'; // white for odd rows
      }
    });
  });
}

// 테이블 데이터가 갱신될 때마다 스타일을 다시 적용
document.addEventListener('DOMContentLoaded', function() {
  // 기존 로직 실행 후 MutationObserver 설정
  const observer = new MutationObserver(mutations => {
    mutations.forEach(mutation => {
      if (mutation.type === 'childList' && mutation.target.id === 'workLogTableBody') {
        applyTableStyles();
      }
    });
  });

  // 테이블 바디 변경 감시
  const tableBody = document.getElementById('workLogTableBody');
  if (tableBody) {
    observer.observe(tableBody, { childList: true });
  }
});/**
 * main.js - 작업 로그 시스템 메인 진입점
 * 애플리케이션 초기화 및 이벤트 리스너 설정을 담당합니다.
 */

// 전역 변수 선언
let workLogData = []; // API에서 가져온 데이터 저장
let currentPage = 1;
let pageSize = 20;
let loading = false;
let hasMoreData = true;
let selectedDate = null;
let editMode = false;

// 애플리케이션 초기화
document.addEventListener('DOMContentLoaded', function() {
  console.log('작업 로그 시스템이 초기화되었습니다.');

  // 모달 초기화
  initializeModals();

  // 날짜 선택기 초기화
  initializeDatePicker();

  // 버튼 이벤트 리스너 설정
  setupButtonListeners();

  // 테이블 이벤트 리스너 설정
  setupTableListeners();

  // 초기 데이터 로드
  API.fetchWorkLogsData();
});

/**
 * 모달 초기화 함수
 */
function initializeModals() {
  // 작업시간 입력 필드 초기화
  flatpickr('#workDatetime', {
    locale: 'ko',
    enableTime: true,
    dateFormat: 'Y-m-d H:i',
    time_24hr: true,
    onChange: function(selectedDates, dateStr, instance) {
      instance.close();
    }
  });

  // 테이블 스타일 직접 적용 - 스타일 문제 해결
  applyTableStyles();
}

/**
 * 날짜 선택기 초기화 함수
 */
function initializeDatePicker() {
  const dateBtn = document.getElementById('btnDate');
  flatpickr(dateBtn, {
    locale: 'ko',
    dateFormat: 'Y-m-d',
    onChange: function (selectedDates, dateStr) {
      selectedDate = dateStr;
      dateBtn.textContent = '날짜: ' + dateStr;
      // 선택한 날짜로 데이터 필터링
      UI.filterDataByDate(dateStr);
    },
  });
}

/**
 * 버튼 이벤트 리스너 설정 함수
 */
function setupButtonListeners() {
  // 업로드 버튼
  document.getElementById('btnUpload').addEventListener('click', function() {
    Modal.showUploadModal();
  });

  // 생성 버튼
  document.getElementById('btnCreate').addEventListener('click', function() {
    Modal.showCreateModal();
  });

  // 삭제 버튼
  document.getElementById('btnDelete').addEventListener('click', function() {
    Modal.confirmDelete();
  });

  // 작업 내역 폼 제출 버튼
  document.getElementById('btnSubmitWorklog').addEventListener('click', function() {
    Modal.submitWorklogForm();
  });

  // 파일 업로드 제출 버튼
  document.getElementById('btnSubmitUpload').addEventListener('click', function() {
    Modal.submitUploadForm();
  });

  // 전체 선택 체크박스
  document.getElementById('selectAll').addEventListener('change', function() {
    const checkboxes = document.querySelectorAll('input[name="selectedIds"]');
    checkboxes.forEach((checkbox) => {
      checkbox.checked = this.checked;
    });

    UI.updateDeleteButton();
  });

  // 개별 체크박스 변경 시 이벤트
  document.addEventListener('change', function(e) {
    if (e.target && e.target.name === 'selectedIds') {
      UI.updateDeleteButton();
    }
  });
}

/**
 * 테이블 이벤트 리스너 설정 함수
 */
function setupTableListeners() {
  // 테이블 행 클릭 처리 (수정 모달)
  document.getElementById('workLogTableBody').addEventListener('click', function(e) {
    // 체크박스 클릭은 무시
    if (e.target.type === 'checkbox') return;

    const row = e.target.closest('tr');
    if (row) {
      Modal.showEditModal(row);
    }
  });

  // 무한 스크롤 구현 (필요시 주석 해제)
  /*
  window.addEventListener('scroll', function() {
    if (loading || !hasMoreData) return;

    // 페이지 하단에 도달했는지 확인
    if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 200) {
      currentPage++;
      API.fetchMoreWorkLogsData(currentPage, pageSize);
    }
  });
  */
}