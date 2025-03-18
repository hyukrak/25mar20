-- SQLite 테스트 데이터베이스용 스키마
DROP TABLE IF EXISTS work_logs;

CREATE TABLE work_logs (
                           wl_id INTEGER PRIMARY KEY AUTOINCREMENT,         -- 고유 ID
                           wl_work_datetime DATETIME NOT NULL,              -- 작업 일시
                           wl_car_model TEXT NOT NULL,                      -- 차량 모델
                           wl_material_code TEXT(50) NOT NULL,              -- 자재 코드
                           wl_quantity INTEGER NOT NULL DEFAULT 1,          -- 수량
                           wl_created_at DATETIME DEFAULT (datetime('now', 'localtime')) -- 생성 일시
);

-- 인덱스 생성
CREATE INDEX idx_wl_created_at ON work_logs (wl_created_at);
CREATE INDEX idx_wl_car_model ON work_logs (wl_car_model);
CREATE INDEX idx_wl_material_code ON work_logs (wl_material_code);