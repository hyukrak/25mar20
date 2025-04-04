/**
 * modal.js - 작업 로그 시스템 모달 관련 함수
 * 모달 창 표시 및 관련 작업을 처리하는 함수들을 포함합니다.
 */

// 전역 변수로 flatpickr 인스턴스 선언
let datePickerInstance = null;

// Modal 네임스페이스 생성
const Modal = {
  // 모달 인스턴스
  uploadModalInstance: null,
  worklogModalInstance: null,

  // 완료 상태 토글 요소
  completionStatusElement: null,
  completionStatusTextElement: null,

  /**
   * 모달 인스턴스 초기화
   */
  init: function() {
    this.uploadModalInstance = new bootstrap.Modal(document.getElementById('uploadModal'));
    this.worklogModalInstance = new bootstrap.Modal(document.getElementById('worklogModal'));

    // 완료 상태 토글 요소 참조
    this.completionStatusElement = document.getElementById('completionStatus');
    this.completionStatusTextElement = document.getElementById('completionStatusText');

    // 완료 상태 토글 이벤트 리스너
    if (this.completionStatusElement) {
      this.completionStatusElement.addEventListener('change', function() {
        const isChecked = this.checked;
        document.getElementById('completionStatusText').textContent = isChecked ? '완료' : '미완료';
      });
    }

    // 모달 이벤트 리스너 설정
    const worklogModal = document.getElementById('worklogModal');
    if (worklogModal) {
      worklogModal.addEventListener('shown.bs.modal', this.onModalShown);
      worklogModal.addEventListener('hidden.bs.modal', this.onModalHidden);
    }
  },

  /**
   * 모달이 표시된 후 호출되는 이벤트 핸들러
   */
  onModalShown: function() {
    // 기존 인스턴스가 있으면 제거
    if (datePickerInstance) {
      datePickerInstance.destroy();
      datePickerInstance = null;
    }

    // 날짜 선택기 초기화 (모달이 표시된 후에)
    const dateInput = document.getElementById('workDatetime');
    if (dateInput) {
      datePickerInstance = flatpickr(dateInput, {
        locale: 'ko',
        enableTime: true,
        dateFormat: 'y.m.d H:i',
        altFormat: 'y.m.d H:i',
        time_24hr: true,
        minuteIncrement: 1,        // 1분 단위로 변경
        allowInput: true,          // 키보드로 직접 입력 허용
        clickOpens: true,          // 클릭 시 달력 열기
        closeOnSelect: false,      // 날짜 선택 시 자동으로 닫히지 않음
        static: true               // 달력이 입력 필드 위에 고정됨
      });

      // 날짜 설정 (현재 입력 필드의 값 사용)
      const dateValue = dateInput.value.trim();
      if (dateValue) {
        try {
          // 날짜 문자열 파싱 (YY.MM.DD HH:MM 형식)
          const dateParts = dateValue.match(/(\d{2})\.(\d{2})\.(\d{2}) (\d{2}):(\d{2})/);
          if (dateParts) {
            const year = parseInt('20' + dateParts[1]); // 2자리 연도를 4자리로 확장
            const month = parseInt(dateParts[2]) - 1;   // 월은 0-11
            const day = parseInt(dateParts[3]);
            const hour = parseInt(dateParts[4]);
            const minute = parseInt(dateParts[5]);

            const date = new Date(year, month, day, hour, minute);
            console.log('날짜 설정:', date);
            datePickerInstance.setDate(date, true);
          }
        } catch (e) {
          console.error('날짜 파싱 오류:', e);
        }
      }

      // 날짜 확인/취소 버튼 관련 코드 제거 (요청에 따라 버튼을 제거했으므로)
    }
  },

  /**
   * 모달이 닫힌 후 호출되는 이벤트 핸들러
   */
  onModalHidden: function() {
    // flatpickr 인스턴스 제거
    if (datePickerInstance) {
      datePickerInstance.destroy();
      datePickerInstance = null;
    }
  },

  /**
   * 업로드 모달 표시
   */
  showUploadModal: function() {
    if (!this.uploadModalInstance) {
      this.init();
    }
    // 폼 초기화
    document.getElementById('file').value = '';
    document.getElementById('carModelSelect').value = '';

    this.uploadModalInstance.show();
  },

  /**
   * 생성 모달 표시
   */
  showCreateModal: function() {
    if (!this.worklogModalInstance) {
      this.init();
    }

    editMode = false;
    document.getElementById('worklogModalLabel').textContent = '작업 내역 생성';
    document.getElementById('worklogId').value = '';
    document.getElementById('worklogForm').reset();

    // 기본 날짜와 시간 설정 (현재 날짜/시간)
    const now = new Date();
    const formattedDateTime = Utils.formatDateTime(now);
    document.getElementById('workDatetime').value = formattedDateTime;

    // 완료 상태 초기화 (생성 시에는 미완료 상태로 초기화)
    if (this.completionStatusElement) {
      this.completionStatusElement.checked = false;
      this.completionStatusTextElement.textContent = '미완료';
    }

    // 완료 상태 필드 숨기기 (생성 시에는 상태 변경을 허용하지 않음)
    document.getElementById('completionStatusContainer').style.display = 'none';

    this.worklogModalInstance.show();
  },

  /**
   * 수정 모달 표시
   * @param {HTMLElement} row - 클릭한 테이블 행 요소
   */
  showEditModal: function(row) {
    if (!this.worklogModalInstance) {
      this.init();
    }

    editMode = true;
    const cells = row.cells;
    const id = row.dataset.id;

    // 완료 상태 확인 (row 클래스에서 status-completed가 있는지 확인)
    const isCompleted = row.classList.contains('status-completed');

    document.getElementById('worklogModalLabel').textContent = '작업 내역 수정';
    document.getElementById('worklogId').value = id;

    // 작업시간 설정 (flatpickr는 모달이 표시된 후 이벤트에서 초기화됨)
    const workDatetimeValue = cells[1].textContent.trim();
    document.getElementById('workDatetime').value = workDatetimeValue;

    document.getElementById('carModel').value = cells[2].textContent.trim();
    document.getElementById('productColor').value = cells[3].textContent.trim();
    document.getElementById('productCode').value = cells[4].textContent.trim();
    document.getElementById('productName').value = cells[5].textContent.trim();
    document.getElementById('quantity').value = cells[6].textContent.trim();

    // 완료 상태 설정
    if (this.completionStatusElement) {
      this.completionStatusElement.checked = isCompleted;
      this.completionStatusTextElement.textContent = isCompleted ? '완료' : '미완료';
    }

    // 완료 상태 필드 표시 (수정 시에는 상태 변경 허용)
    document.getElementById('completionStatusContainer').style.display = 'block';

    this.worklogModalInstance.show();
  },

  /**
   * 작업 내역 폼 제출 처리
   */
  submitWorklogForm: function() {
    const form = document.getElementById('worklogForm');
    if (!form.checkValidity()) {
      form.reportValidity();
      return;
    }

    // 입력값 유효성 검사
    let workDatetime = document.getElementById('workDatetime').value.trim();

    // YY.MM.DD HH:MM 형식인지 확인
    // 만약 YYYY-MM-DD HH:MM 형식이라면 YY.MM.DD HH:MM 형식으로 변환
    if (workDatetime.match(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/)) {
      const datePart = workDatetime.substring(0, 10); // YYYY-MM-DD
      const timePart = workDatetime.substring(11); // HH:MM

      // YYYY-MM-DD -> YY.MM.DD 변환
      const year = datePart.substring(2, 4);  // YY
      const month = datePart.substring(5, 7); // MM
      const day = datePart.substring(8, 10);  // DD

      workDatetime = `${year}.${month}.${day} ${timePart}`;
    }

    const workLogData = {
      id: document.getElementById('worklogId').value,
      workDatetime: workDatetime,
      carModel: document.getElementById('carModel').value,
      productColor: document.getElementById('productColor').value,
      productCode: document.getElementById('productCode').value,
      productName: document.getElementById('productName').value,
      quantity: document.getElementById('quantity').value
    };


    // 수정 모드에서는 완료 상태도 함께 업데이트
    if (editMode) {
      const completionStatus = document.getElementById('completionStatus').checked;

      // 기본 데이터 업데이트
      API.updateWorkLog(workLogData)
      .then(() => {
        // 상태 업데이트
        return API.updateWorkLogStatus(workLogData.id, completionStatus);
      })
      .then(() => {
        if (this.worklogModalInstance) {
          this.worklogModalInstance.hide();
        }
      })
      .catch(error => {
        console.error('작업 내역 업데이트 오류:', error);
      });
    } else {
      // 생성 API 호출
      API.createWorkLog(workLogData)
      .then(() => {
        if (this.worklogModalInstance) {
          this.worklogModalInstance.hide();
        }
      })
      .catch(error => {
        console.error('작업 내역 생성 오류:', error);
      });
    }
  },

  /**
   * 파일 업로드 폼 제출 처리
   */
  submitUploadForm: function() {
    const fileInput = document.getElementById('file');
    const carModelSelect = document.getElementById('carModelSelect');

    if (fileInput.files.length === 0) {
      UI.showToast('업로드할 파일을 선택해주세요.', 'warning');
      return;
    }

    if (!carModelSelect.value) {
      UI.showToast('차종을 선택해주세요.', 'warning');
      return;
    }

    // 폼 데이터 생성
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    formData.append('carModel', carModelSelect.value);

    // API 호출
    API.uploadFile(formData)
    .then(() => {
      if (this.uploadModalInstance) {
        this.uploadModalInstance.hide();
      }
    })
    .catch(error => {
      console.error('파일 업로드 오류:', error);
    });
  },

  /**
   * 삭제 확인 및 처리
   */
  confirmDelete: function() {
    const selectedCheckboxes = document.querySelectorAll(
        'input[name="selectedIds"]:checked'
    );
    const selectedIds = Array.from(selectedCheckboxes).map(
        (checkbox) => checkbox.value
    );

    if (selectedIds.length > 0) {
      if (confirm('선택한 작업 로그를 삭제하시겠습니까?')) {
        API.deleteWorkLogs(selectedIds);
      }
    }
  }
};

// DOM이 로드되면 모달 초기화
document.addEventListener('DOMContentLoaded', function() {
  Modal.init();
});