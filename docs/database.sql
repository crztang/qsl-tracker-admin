CREATE DATABASE IF NOT EXISTS qsl_tracker
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE qsl_tracker;

CREATE TABLE sys_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  call_sign VARCHAR(32) NULL,
  email VARCHAR(128) NULL,
  mailing_address VARCHAR(500) NULL,
  phone VARCHAR(32) NULL,
  recipient VARCHAR(128) NULL,
  postal_code VARCHAR(32) NULL,
  password_hash VARCHAR(100) NOT NULL,
  password_salt VARCHAR(64) NULL,
  must_change_password TINYINT(1) NOT NULL DEFAULT 0,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  failed_login_count INT UNSIGNED NOT NULL DEFAULT 0,
  locked_until DATETIME(3) NULL,
  last_login_at DATETIME(3) NULL,
  last_login_ip VARCHAR(45) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB COMMENT='系统用户';

CREATE TABLE sys_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  data_scope VARCHAR(16) NOT NULL DEFAULT 'SELF',
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  built_in TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_code (code)
) ENGINE=InnoDB COMMENT='系统角色';

CREATE TABLE sys_permission (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(128) NOT NULL,
  name VARCHAR(128) NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_permission_code (code)
) ENGINE=InnoDB COMMENT='系统权限';

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

CREATE TABLE sys_user_role (
  user_id BIGINT UNSIGNED NOT NULL,
  role_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (user_id, role_id),
  KEY idx_sys_user_role_role (role_id)
) ENGINE=InnoDB COMMENT='用户角色';

CREATE TABLE sys_role_permission (
  role_id BIGINT UNSIGNED NOT NULL,
  permission_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  KEY idx_sys_role_permission_permission (permission_id)
) ENGINE=InnoDB COMMENT='角色权限';

CREATE TABLE user_login_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NULL,
  username VARCHAR(64) NOT NULL,
  login_ip VARCHAR(45) NULL,
  user_agent VARCHAR(255) NULL,
  success TINYINT(1) NOT NULL,
  fail_reason VARCHAR(128) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_user_login_log_user_time (user_id, created_at),
  KEY idx_user_login_log_ip_time (login_ip, created_at)
) ENGINE=InnoDB COMMENT='用户登录审计日志';

CREATE TABLE qso_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  call_sign VARCHAR(32) NOT NULL,
  qso_time DATETIME(3) NOT NULL,
  timezone_offset VARCHAR(6) NOT NULL DEFAULT '+08:00',
  frequency_mhz DECIMAL(12,6) NULL,
  bd VARCHAR(16) NULL,
  mode VARCHAR(32) NULL,
  power_w DECIMAL(8,2) NULL,
  rst_sent VARCHAR(8) NULL,
  rst_received VARCHAR(8) NULL,
  antenna VARCHAR(128) NULL,
  qth VARCHAR(255) NULL,
  antenna_height VARCHAR(32) NULL,
  device VARCHAR(128) NULL,
  remark VARCHAR(1000) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_qso_log_user_time (user_id, qso_time),
  KEY idx_qso_log_call_sign (call_sign),
  KEY idx_qso_log_mode (mode),
  KEY idx_qso_log_qth (qth)
) ENGINE=InnoDB COMMENT='通联日志';

CREATE TABLE qsl_card (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  card_type CHAR(1) NOT NULL,
  qso_log_id BIGINT UNSIGNED NULL,
  call_sign VARCHAR(32) NOT NULL,
  contact_name VARCHAR(128) NULL,
  contact_address VARCHAR(500) NULL,
  postal_code VARCHAR(32) NULL,
  status CHAR(1) NOT NULL DEFAULT '1',
  tracking_no VARCHAR(64) NULL,
  confirm_token CHAR(64) NULL,
  public_confirm_enabled TINYINT(1) NOT NULL DEFAULT 1,
  sent_at DATETIME(3) NULL,
  received_at DATETIME(3) NULL,
  confirmed_at DATETIME(3) NULL,
  confirmed_ip VARCHAR(45) NULL,
  confirmed_user_agent VARCHAR(255) NULL,
  remark VARCHAR(1000) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_qsl_card_tracking_no (tracking_no),
  UNIQUE KEY uk_qsl_card_confirm_token (confirm_token),
  KEY idx_qsl_card_user_created (user_id, created_at),
  KEY idx_qsl_card_qso_log_id (qso_log_id),
  KEY idx_qsl_card_call_sign (call_sign),
  KEY idx_qsl_card_type_status (card_type, status)
) ENGINE=InnoDB COMMENT='QSL卡片';

CREATE TABLE qsl_confirm_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  qsl_card_id BIGINT UNSIGNED NOT NULL,
  tracking_no VARCHAR(64) NOT NULL,
  confirm_ip VARCHAR(45) NULL,
  user_agent VARCHAR(255) NULL,
  success TINYINT(1) NOT NULL,
  fail_reason VARCHAR(128) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_qsl_confirm_log_user_time (user_id, created_at),
  KEY idx_qsl_confirm_log_card_time (qsl_card_id, created_at),
  KEY idx_qsl_confirm_log_ip_time (confirm_ip, created_at)
) ENGINE=InnoDB COMMENT='QSL公开确认审计日志';

CREATE TABLE storage_file (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  file_key CHAR(32) NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  storage_name VARCHAR(255) NOT NULL,
  relative_path VARCHAR(500) NOT NULL,
  file_extension VARCHAR(32) NULL,
  content_type VARCHAR(128) NULL,
  file_size BIGINT UNSIGNED NOT NULL DEFAULT 0,
  file_hash CHAR(64) NULL,
  status CHAR(1) NOT NULL DEFAULT '1',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_storage_file_key (file_key),
  UNIQUE KEY uk_storage_file_relative_path (relative_path),
  KEY idx_storage_file_user_status (user_id, status),
  KEY idx_storage_file_hash (file_hash)
) ENGINE=InnoDB COMMENT='文件';

CREATE TABLE print_template (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  template_name VARCHAR(128) NOT NULL,
  template_type CHAR(1) NOT NULL,
  background_file_id BIGINT UNSIGNED NULL,
  config_json JSON NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  is_default TINYINT(1) NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  remark VARCHAR(500) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_print_template_user_type (user_id, template_type, enabled, sort_order),
  KEY idx_print_template_background_file (background_file_id)
) ENGINE=InnoDB COMMENT='打印配置';

INSERT INTO sys_role(code, name, data_scope, enabled, built_in) VALUES
  ('USER', '普通用户', 'SELF', 1, 1),
  ('SUPER_ADMIN', '超级管理员', 'ALL', 1, 1);

INSERT INTO sys_permission(code, name) VALUES
  ('qso:read', '查看通联日志'), ('qso:write', '维护通联日志'),
  ('qsl:read', '查看QSL卡片'), ('qsl:write', '维护QSL卡片'),
  ('file:read', '读取文件'), ('file:write', '上传文件'),
  ('template:read', '查看打印模板'), ('template:write', '维护打印模板'),
  ('user:manage', '用户管理'), ('role:manage', '角色管理'),
  ('permission:manage', '权限管理');

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

INSERT INTO sys_role_permission(role_id, permission_id)
  SELECT r.id, p.id FROM sys_role r CROSS JOIN sys_permission p
  WHERE r.code = 'SUPER_ADMIN';

INSERT INTO sys_role_permission(role_id, permission_id)
  SELECT r.id, p.id FROM sys_role r CROSS JOIN sys_permission p
  WHERE r.code = 'USER'
    AND p.code IN ('qso:read','qso:write','qsl:read','qsl:write',
                   'file:read','file:write','template:read','template:write');
