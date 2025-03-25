CREATE TABLE IF NOT EXISTS work_logs (
    wl_id INTEGER PRIMARY KEY AUTOINCREMENT,                        --  '작업 로그 고유 ID',
    wl_work_datetime DATETIME NOT NULL,                             --  '작업시간 - YY.MM.DD HH:MM 형식',
    wl_car_model TEXT NOT NULL,                                     --  '차종',
    wl_product_color TEXT(7) NOT NULL,                              --  제품 색상
    wl_product_code  TEXT(20) NOT NULL,                             --  제품 코드
    wl_product_name  TEXT(50) NOT NULL,                             --  제품 이름
    wl_quantity INTEGER DEFAULT 0 NOT NULL,                         --  '수량',
    wl_completed_by TEXT(20) NULL,                                  --  '완료 주체' - 완료되지 않은 경우 NULL,
    wl_completed_at DATETIME NULL,                                  --  '완료 시간' - 완료되지 않은 경우 NULL,
    wl_created_at DATETIME DEFAULT (datetime('now', 'localtime'))   --  '생성일' - 시스템 자동 기록,
    );

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_wl_created_at ON work_logs (wl_created_at);
CREATE INDEX IF NOT EXISTS idx_wl_car_model ON work_logs (wl_car_model);
CREATE INDEX IF NOT EXISTS idx_wl_completed_at ON work_logs (wl_completed_at);
CREATE INDEX IF NOT EXISTS idx_wl_work_datetime ON work_logs (wl_work_datetime);
