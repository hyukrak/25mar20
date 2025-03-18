CREATE TABLE work_logs (
       wl_id INTEGER PRIMARY KEY AUTOINCREMENT,                        --  '작업 로그 고유 ID',
       wl_work_datetime DATETIME NOT NULL,                             --  '작업시간 - YYYY-MM-DD HH:MM:SS 형식',
       wl_car_model TEXT NOT NULL,                                     --  '차종',
       wl_material_code TEXT(50) NOT NULL,                             --  '자재정보 코드',
       wl_quantity INTEGER DEFAULT 0 NOT NULL,                         --  '수량',
       wl_created_at DATETIME DEFAULT (datetime('now', 'localtime')),  --  '생성일 - 시스템 자동 기록',

    -- 내부적으로 사용되는 컬럼들
    -- wl_updated_at DATETIME DEFAULT (datetime('now', 'localtime')),
    -- wl_user_id INTEGER NOT NULL,
    -- wl_status TEXT DEFAULT 'pending',
    -- wl_notes TEXT,
    -- wl_department_id INTEGER,
    -- wl_line_id INTEGER,
    -- wl_lot_number TEXT,
    -- wl_is_deleted INTEGER DEFAULT 0
);

-- 인덱스 생성
CREATE INDEX idx_wl_created_at ON work_logs (wl_created_at);
CREATE INDEX idx_wl_car_model ON work_logs (wl_car_model);
CREATE INDEX idx_wl_material_code ON work_logs (wl_material_code);