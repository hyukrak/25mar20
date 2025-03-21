/**
 * main.js - 작업 로그 시스템 메인 진입점
 * 애플리케이션 초기화 및 이벤트 리스너 설정을 담당합니다.
 */

// 전역 변수 선언
let workLogData = []; // API에서 가져온 데이터 저장
let loading = false;
let selectedDate = null;
let editMode = false;

/**
 * 테이블 스타일을 직접 JavaScript로 적용하는 함수
 * CSS가 제대로 적용되지 않을 때 사용
 */
function applyTableStyles() {
  // 제브라 스타일 적용
  document.querySelectorAll('.table tbody tr:nth-child(even)').forEach(row => {
    // status-completed 클래스가 없는 경우에만 제브라 스타일 적용
    if (!row.classList.contains('status-completed')) {
      row.style.backgroundColor = '#f3f0ec'; // --light-beige
    }
  });

  document.querySelectorAll('.table tbody tr:nth-child(odd)').forEach(row => {
    // status-completed 클래스가 없는 경우에만 제브라 스타일 적용
    if (!row.classList.contains('status-completed')) {
      row.style.backgroundColor = '#ffffff';
    }
  });

  // hover 이벤트를 위한 이벤트 리스너 추가
  document.querySelectorAll('.table tbody tr').forEach(row => {
    row.addEventListener('mouseenter', () => {
      row.style.backgroundColor = '#d9d9d9'; // --medium-gray
      row.style.cursor = 'pointer';
    });

    row.addEventListener('mouseleave', () => {
      // status-completed 클래스가 있으면 완료 상태 배경색 유지
      if (row.classList.contains('status-completed')) {
        row.style.backgroundColor = 'rgba(25, 135, 84, 0.1)';
      } else if (row.selectorIndex % 2 === 0) {
        row.style.backgroundColor = '#f3f0ec'; // --light-beige for even rows
      } else {
        row.style.backgroundColor = '#ffffff'; // white for odd rows
      }
    });
  });
}

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

  // 오늘 날짜를 기본값으로 설정
  const today = new Date();
  const formattedToday = Utils.formatDateForSearch(today);
  selectedDate = formattedToday;

  // 날짜 버튼 텍스트 업데이트
  document.getElementById('btnDate').innerHTML = '<i class="bi bi-calendar"></i> ' + formattedToday;

  // 오늘 날짜 데이터로 필터링
  UI.filterDataByDate(formattedToday);
});

/**
 * 모달 초기화 함수
 */
function initializeModals() {
  // 작업시간 입력 필드 초기화 - YY.MM.DD HH:MM 형식 사용
  flatpickr('#workDatetime', {
    locale: 'ko',
    enableTime: true,
    dateFormat: 'y.m.d H:i',
    altFormat: 'y.m.d H:i',
    time_24hr: true,
    onChange: function(selectedDates, dateStr, instance) {
      // 선택된 날짜가 있을 때만 닫기
      if (selectedDates.length > 0) {
        instance.close();
      }
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

  // YY.MM.DD 형식으로 날짜 선택
  flatpickr(dateBtn, {
    locale: 'ko',
    dateFormat: 'y.m.d',
    altFormat: 'y.m.d',
    defaultDate: new Date(), // 기본값을 오늘 날짜로 설정
    onChange: function (selectedDates, dateStr, instance) {
      // 선택된 날짜가 있을 때만 처리
      if (selectedDates.length > 0) {
        // YY.MM.DD 형식의 날짜 문자열로 변환
        const date = selectedDates[0];
        const formattedDate = Utils.formatDateForSearch(date);
        selectedDate = formattedDate;

        // 버튼 텍스트 업데이트
        dateBtn.innerHTML = '<i class="bi bi-calendar"></i> ' + formattedDate;

        // 선택한 날짜로 데이터 필터링
        UI.filterDataByDate(formattedDate);

        // 자동으로 인스턴스 닫기
        instance.close();
      }
    }
  });
}

/**
 * 상태 드롭다운 토글 함수
 */
function toggleStatusDropdown() {
  const dropdown = document.getElementById('statusDropdown');
  dropdown.classList.toggle('show');

  // 클릭 이벤트가 발생하면 드롭다운 숨기기
  document.addEventListener('click', function closeDropdown(e) {
    // 상태 헤더나 드롭다운 메뉴 자체를 클릭한 경우는 제외
    if (!e.target.closest('.status-header') && !e.target.closest('#statusDropdown')) {
      dropdown.classList.remove('show');
      document.removeEventListener('click', closeDropdown);
    }
  });
}

/**
 * 버튼 이벤트 리스너 설정 함수
 */
function setupButtonListeners() {
  // 날짜 초기화 버튼
  document.getElementById('btnResetDate').addEventListener('click', function() {
    selectedDate = null;
    document.getElementById('btnDate').innerHTML = '<i class="bi bi-calendar"></i> 날짜 검색';
    API.fetchWorkLogsData();
  });

  // 상태 필터 헤더 클릭 이벤트
  document.querySelector('.status-header').addEventListener('click', function(e) {
    // 이미 열려있는 다른 드롭다운 닫기
    document.querySelectorAll('.sort-dropdown-menu.show').forEach(menu => {
      menu.classList.remove('show');
    });

    // 상태 드롭다운 토글
    toggleStatusDropdown();
    e.stopPropagation();
  });

  // 상태 필터 드롭다운 항목 클릭 이벤트
  document.querySelectorAll('#statusDropdown .dropdown-item').forEach(item => {
    item.addEventListener('click', function(e) {
      e.preventDefault();
      e.stopPropagation();

      // 기존 활성 항목 제거
      document.querySelectorAll('#statusDropdown .dropdown-item').forEach(i => {
        i.classList.remove('active');
      });

      // 현재 항목 활성화
      this.classList.add('active');

      // 선택한 상태로 필터링
      const status = this.getAttribute('data-status');
      UI.filterDataByStatus(status);

      // 드롭다운 닫기
      document.getElementById('statusDropdown').classList.remove('show');
    });
  });

  // 정렬 헤더 클릭 이벤트
  document.querySelectorAll('.sortable').forEach(header => {
    header.addEventListener('click', function() {
      const field = this.getAttribute('data-field');
      const currentDirection = this.getAttribute('data-direction');

      // 현재 방향의 반대로 토글하거나, 처음 클릭 시 ASC로 설정
      // data-direction이 없는 경우는 처음 클릭하는 경우이므로 ASC 설정
      const newDirection = currentDirection === 'ASC' ? 'DESC' : 'ASC';

      UI.applySorting(field, newDirection);
    });
  });

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
    // 체크박스와 상태 아이콘 클릭은 무시
    if (e.target.type === 'checkbox' ||
        e.target.closest('.status-icon') ||
        e.target.classList.contains('bi-check-circle-fill') ||
        e.target.classList.contains('bi-x-circle')) {
      return;
    }

    const row = e.target.closest('tr');
    if (row) {
      Modal.showEditModal(row);
    }
  });
}