CREATE TABLE IF NOT EXISTS work_logs (
       wl_id INTEGER PRIMARY KEY AUTOINCREMENT,                        --  '작업 로그 고유 ID',
       wl_work_datetime TEXT(20) NOT NULL,                             --  '작업시간 - YY.MM.DD HH:MM 형식',
       wl_car_model TEXT NOT NULL,                                     --  '차종',
       wl_product_color TEXT(7) NOT NULL,
       wl_product_code  TEXT(20) NOT NULL,
       wl_product_name  TEXT(50) NOT NULL,
       wl_quantity INTEGER DEFAULT 0 NOT NULL,                         --  '수량',
       wl_created_at DATETIME DEFAULT (datetime('now', 'localtime'))  --  '생성일 - 시스템 자동 기록',
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_wl_created_at ON work_logs (wl_created_at);
CREATE INDEX IF NOT EXISTS idx_wl_car_model ON work_logs (wl_car_model);
