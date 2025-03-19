/**
 * modal.js - 작업 로그 시스템 모달 관련 함수
 * 모달 창 표시 및 관련 작업을 처리하는 함수들을 포함합니다.
 */

// Modal 네임스페이스 생성
const Modal = {
  // 모달 인스턴스
  uploadModalInstance: null,
  worklogModalInstance: null,

  /**
   * 모달 인스턴스 초기화
   */
  init: function() {
    this.uploadModalInstance = new bootstrap.Modal(document.getElementById('uploadModal'));
    this.worklogModalInstance = new bootstrap.Modal(document.getElementById('worklogModal'));
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

    document.getElementById('worklogModalLabel').textContent = '작업 내역 수정';
    document.getElementById('worklogId').value = id;
    document.getElementById('workDatetime').value = cells[1].textContent.trim();
    document.getElementById('carModel').value = cells[2].textContent.trim();
    document.getElementById('productColor').value = cells[3].textContent.trim();
    document.getElementById('productCode').value = cells[4].textContent.trim();
    document.getElementById('productName').value = cells[5].textContent.trim();
    document.getElementById('quantity').value = cells[6].textContent.trim();

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

    const workLogData = {
      id: document.getElementById('worklogId').value,
      workDatetime: document.getElementById('workDatetime').value,
      carModel: document.getElementById('carModel').value,
      productColor: document.getElementById('productColor').value,
      productCode: document.getElementById('productCode').value,
      productName: document.getElementById('productName').value,
      quantity: document.getElementById('quantity').value
    };

    if (editMode) {
      // 수정 API 호출
      API.updateWorkLog(workLogData)
      .then(() => {
        if (this.worklogModalInstance) {
          this.worklogModalInstance.hide();
        }
      });
    } else {
      // 생성 API 호출
      API.createWorkLog(workLogData)
      .then(() => {
        if (this.worklogModalInstance) {
          this.worklogModalInstance.hide();
        }
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