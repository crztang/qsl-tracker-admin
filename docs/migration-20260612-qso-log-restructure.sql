USE qsl_tracker;

CREATE TABLE sys_dict (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  remark VARCHAR(500) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_dict_code (code)
) ENGINE=InnoDB COMMENT='字典表';

CREATE TABLE sys_dict_item (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  dict_id BIGINT UNSIGNED NOT NULL,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  remark VARCHAR(500) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_dict_item_dict_code (dict_id, item_code),
  KEY idx_sys_dict_item_dict_sort (dict_id, sort_order),
  CONSTRAINT fk_sys_dict_item_dict
    FOREIGN KEY (dict_id) REFERENCES sys_dict(id)
) ENGINE=InnoDB COMMENT='字典项';

INSERT INTO sys_dict(code, name, enabled, remark) VALUES
  ('QSO_MODE', '通联模式', 1, '通联日志模式字典');

INSERT INTO sys_dict_item(dict_id, item_code, item_name, sort_order, enabled)
SELECT d.id, v.item_code, v.item_name, v.sort_order, 1
FROM sys_dict d
JOIN (
  SELECT 'FM' AS item_code, 'FM' AS item_name, 1 AS sort_order
  UNION ALL SELECT 'SSTV', 'SSTV', 2
  UNION ALL SELECT 'SSB', 'SSB', 3
  UNION ALL SELECT 'RTTY', 'RTTY', 4
  UNION ALL SELECT 'CW', 'CW', 5
  UNION ALL SELECT 'FT8', 'FT8', 6
) v
WHERE d.code = 'QSO_MODE';

ALTER TABLE qso_log
  DROP INDEX idx_qso_log_country,
  DROP COLUMN country,
  DROP COLUMN qth_province,
  DROP COLUMN qth_city,
  DROP COLUMN qth_district,
  DROP COLUMN qth_detail,
  ADD COLUMN qth VARCHAR(255) NULL COMMENT 'QTH' AFTER antenna,
  ADD COLUMN antenna_height VARCHAR(32) NULL COMMENT '天线高度' AFTER qth,
  ADD COLUMN device VARCHAR(128) NULL COMMENT '设备' AFTER antenna_height,
  ADD KEY idx_qso_log_qth (qth);
