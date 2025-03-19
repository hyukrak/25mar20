/**
 * ui.js - 작업 로그 시스템 UI 관련 함수
 * UI 렌더링 및 업데이트 관련 함수들을 포함합니다.
 */

// 전역 변수
let currentSortField = 'wl_work_datetime';
let currentSortDirection = 'ASC'; // 기본값을 'ASC'로 변경
let currentStatus = null;

// UI 네임스페이스 생성
const UI = {
  /**
   * 작업 로그 데이터 렌더링 함수
   * @param {Array} data - 렌더링할 작업 로그 데이터 배열
   */
  renderWorkLogData: function(data) {
    const tableBody = document.getElementById('workLogTableBody');
    tableBody.innerHTML = '';

    // 정렬 아이콘 초기화
    this.updateSortIcons();

    // 데이터가 없을 경우
    const tableResponsive = document.querySelector('.table-responsive');
    const emptyMessage = document.getElementById('emptyMessage');

    if (!data || data.length === 0) {
      tableResponsive.style.display = 'none';
      emptyMessage.style.display = 'block';
    } else {
      emptyMessage.style.display = 'none';
      tableResponsive.style.display = 'block';

      // 현재 시간
      const now = new Date();

      data.forEach((workLog) => {
        const row = document.createElement('tr');
        row.dataset.id = workLog.id;

        // 작업이 완료 상태인 경우 행 스타일 적용
        if (workLog.completedAt) {
          row.classList.add('status-completed');
        }

        // 체크박스 셀
        const checkboxCell = document.createElement('td');
        checkboxCell.className = 'text-center';
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.name = 'selectedIds';
        checkbox.value = workLog.id;
        checkbox.className = 'form-check-input';
        checkbox.addEventListener('change', this.updateDeleteButton);
        checkbox.addEventListener('click', function(e) {
          e.stopPropagation(); // 행 클릭 이벤트 전파 방지
        });
        checkboxCell.appendChild(checkbox);
        row.appendChild(checkboxCell);

        // 작업시간 셀 (상태에 따른 색상 적용)
        const workDatetimeCell = document.createElement('td');
        workDatetimeCell.textContent = workLog.workDatetime;

        // 작업시간과 현재 시간 비교를 위한 날짜 파싱
        // YY.MM.DD HH:MM 형식 파싱
        const workDateStr = workLog.workDatetime;
        let workDate = null;

        if (workDateStr) {
          const parts = workDateStr.split(' ');
          if (parts.length === 2) {
            const dateParts = parts[0].split('.');
            const timeParts = parts[1].split(':');

            if (dateParts.length === 3 && timeParts.length === 2) {
              // 20YY로 연도 변환 (2자리->4자리)
              const year = parseInt(dateParts[0]) + 2000; // 25로 시작하면 2025년
              const month = parseInt(dateParts[1]) - 1; // JavaScript의 월은 0부터 시작
              const day = parseInt(dateParts[2]);
              const hour = parseInt(timeParts[0]);
              const minute = parseInt(timeParts[1]);

              workDate = new Date(year, month, day, hour, minute);
            }
          }
        }

        // 작업일이 오늘이거나 과거인 경우 passed 스타일 적용
        if (workDate) {
          // 시간 부분을 제외하고 날짜만 비교
          const workDateOnly = new Date(workDate.getFullYear(), workDate.getMonth(), workDate.getDate());
          const nowDateOnly = new Date(now.getFullYear(), now.getMonth(), now.getDate());

          if (workDateOnly <= nowDateOnly) {
            workDatetimeCell.classList.add('status-passed');  // 지난 작업
            row.classList.add('passed-task');
          } else {
            workDatetimeCell.classList.add('status-normal');  // 남은 작업
          }
        }

        row.appendChild(workDatetimeCell);

        // 차종 셀
        const carModelCell = document.createElement('td');
        carModelCell.textContent = workLog.carModel;
        row.appendChild(carModelCell);

        // 색상 셀
        const colorCell = document.createElement('td');
        colorCell.textContent = workLog.productColor || '';
        row.appendChild(colorCell);

        // 제품 코드 셀
        const productCodeCell = document.createElement('td');
        productCodeCell.textContent = workLog.productCode || '';
        row.appendChild(productCodeCell);

        // 제품명 셀
        const productNameCell = document.createElement('td');
        productNameCell.textContent = workLog.productName || '';
        row.appendChild(productNameCell);

        // 수량 셀
        const quantityCell = document.createElement('td');
        quantityCell.textContent = workLog.quantity;
        row.appendChild(quantityCell);

        // 생성일 셀
        const createdAtCell = document.createElement('td');

        // 날짜 포맷을 원하는 형식으로 변환
        let formattedDate = new Intl.DateTimeFormat('ko-KR', {
          year: '2-digit',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          hour12: false, // 24시간제
        }).format(new Date(workLog.createdAt));

        // "25. 03. 20. 04:01" → "25.03.20 04:01"
        formattedDate = formattedDate.replace(/\. /g, '.');                             // 1. 모든 ". "를 "."로 변경 → "25.03.20.04:01"
        formattedDate = formattedDate.replace(/\.([0-9]{2}:[0-9]{2})$/, ' $1');         // 2. 마지막 점(.)을 공백으로 변경 → "25.03.20 04:01"

        createdAtCell.textContent = formattedDate;
        row.appendChild(createdAtCell);

        // 완료 상태 셀
        const statusCell = document.createElement('td');
        const statusIcon = document.createElement('div');
        statusIcon.className = 'status-icon ' +
            (workLog.completedAt ? 'status-complete-icon' : 'status-incomplete-icon');

        // 완료/미완료 아이콘 설정
        const iconElement = document.createElement('i');
        if (workLog.completedAt) {
          iconElement.className = 'bi bi-check-circle-fill';
          iconElement.setAttribute('title', '완료');
        } else {
          iconElement.className = 'bi bi-x-circle';
          iconElement.setAttribute('title', '미완료');
        }

        statusIcon.appendChild(iconElement);

        // 완료 상태 토글 이벤트
        statusIcon.addEventListener('click', function(e) {
          e.stopPropagation(); // 행 클릭 이벤트 전파 방지
          const isCompleted = workLog.completedAt !== null;

          // API 호출하여 상태 토글
          API.updateWorkLogStatus(workLog.id, !isCompleted)
          .then(() => {
            // 데이터 새로고침
            API.fetchWorkLogsData();
          })
          .catch(error => {
            console.error('상태 업데이트 오류:', error);
            UI.showToast('상태 업데이트 중 오류가 발생했습니다.', 'error');
          });
        });

        statusCell.appendChild(statusIcon);
        row.appendChild(statusCell);

        tableBody.appendChild(row);
      });
    }

    // 테이블 스타일 적용 (제브라 스타일과 호버 효과)
    if (typeof applyTableStyles === 'function') {
      applyTableStyles();
    }

    // 전체 선택 체크박스 초기화
    document.getElementById('selectAll').checked = false;
    this.updateDeleteButton();

    // 상태 필터 표시 업데이트
    this.updateStatusFilterDisplay();
  },

  /**
   * 상태 필터 표시 업데이트 함수
   */
  updateStatusFilterDisplay: function() {
    // 모든 드롭다운 항목에서 active 클래스 제거
    document.querySelectorAll('#statusDropdown .dropdown-item').forEach(item => {
      item.classList.remove('active');
    });

    // 현재 상태에 맞는 항목에 active 클래스 추가
    const activeItemSelector = currentStatus === null ?
        '[data-status="all"]' :
        `[data-status="${currentStatus}"]`;

    const activeItem = document.querySelector(`#statusDropdown ${activeItemSelector}`);
    if (activeItem) {
      activeItem.classList.add('active');
    }
  },

  /**
   * 날짜별 데이터 필터링 함수
   * @param {string} dateStr - 필터링할 날짜 (YY.MM.DD 형식)
   */
  filterDataByDate: function(dateStr) {
    // API 호출로 특정 날짜의 데이터 조회
    API.fetchWorkLogsByDate(dateStr);
  },

  /**
   * 상태별 데이터 필터링 함수
   * @param {string} status - 필터링할 상태 ('completed', 'incomplete', 'all')
   */
  filterDataByStatus: function(status) {
    // 전역 상태 변수 업데이트
    currentStatus = status === 'all' ? null : status;

    // 상태 필터 표시 업데이트
    this.updateStatusFilterDisplay();

    // API 호출로 필터링된 데이터 조회
    API.fetchFilteredWorkLogs(currentStatus, currentSortField, currentSortDirection);
  },

  /**
   * 정렬 적용 함수
   * @param {string} field - 정렬 필드
   * @param {string} direction - 정렬 방향 ('ASC' 또는 'DESC')
   */
  applySorting: function(field, direction) {
    // 전역 정렬 변수 업데이트
    currentSortField = field;
    currentSortDirection = direction;

    // 정렬 헤더 업데이트
    this.updateSortIcons();

    // API 호출로 정렬된 데이터 조회
    API.fetchFilteredWorkLogs(currentStatus, field, direction);
  },

  /**
   * 정렬 아이콘 업데이트
   */
  updateSortIcons: function() {
    // 모든 정렬 헤더 초기화
    document.querySelectorAll('.sortable').forEach(header => {
      const field = header.getAttribute('data-field');
      const icon = header.querySelector('.sort-icon');

      header.classList.remove('sort-active');
      header.removeAttribute('data-direction');

      // 현재 정렬 중인 필드인 경우
      if (field === currentSortField) {
        header.classList.add('sort-active');
        header.setAttribute('data-direction', currentSortDirection);

        // 아이콘 방향 설정
        if (currentSortDirection === 'ASC') {
          icon.classList.remove('bi-sort-down');
          icon.classList.add('bi-sort-up');
        } else {
          icon.classList.remove('bi-sort-up');
          icon.classList.add('bi-sort-down');
        }
      } else {
        // 기본 아이콘으로 초기화
        icon.classList.remove('bi-sort-up');
        icon.classList.add('bi-sort-down');
      }
    });
  },

  /**
   * 삭제 버튼 상태 업데이트 함수
   */
  updateDeleteButton: function() {
    const selectedCount = document.querySelectorAll(
        'input[name="selectedIds"]:checked'
    ).length;
    document.getElementById('btnDelete').disabled = selectedCount === 0;
  },

  /**
   * Toast 메시지 표시 함수
   * @param {string} message - 표시할 메시지
   * @param {string} type - 메시지 타입 ('info', 'success', 'warning', 'error')
   * @param {number} duration - 표시 지속 시간 (밀리초)
   * @returns {Object} 토스트 인스턴스
   */
  showToast: function(message, type = 'info', duration = 1500) {
    const toastEl = document.getElementById('liveToast');
    // 기존 상태 관련 클래스 제거 및 hide-animation 제거(초기화)
    toastEl.classList.remove('toast-info', 'toast-success', 'toast-warning', 'toast-error', 'hide-animation');

    // type에 따라 해당 클래스 추가
    switch (type) {
      case 'success':
        toastEl.classList.add('toast-success');
        break;
      case 'warning':
        toastEl.classList.add('toast-warning');
        break;
      case 'error':
        toastEl.classList.add('toast-error');
        break;
      default:
        toastEl.classList.add('toast-info');
    }

    toastEl.querySelector('.toast-body').textContent = message;
    const toastInstance = new bootstrap.Toast(toastEl, { autohide: false });
    toastInstance.show();

    // duration 후에 hide-animation 클래스를 추가하고, 애니메이션이 끝난 후에 실제로 hide
    setTimeout(() => {
      toastEl.classList.add('hide-animation');
      // 500ms는 애니메이션 지속시간과 동일
      setTimeout(() => {
        toastInstance.hide();
        // 다음 사용을 위해 hide-animation 클래스를 제거
        toastEl.classList.remove('hide-animation');
      }, 500);
    }, duration);

    return toastInstance;
  }
};