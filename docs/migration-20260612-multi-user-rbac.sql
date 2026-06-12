USE qsl_tracker;

DELIMITER //
CREATE PROCEDURE migrate_multi_user_rbac()
BEGIN
  DECLARE admin_count INT DEFAULT 0;
  DECLARE admin_id BIGINT UNSIGNED;

  SELECT COUNT(*), MAX(id) INTO admin_count, admin_id
  FROM admin_user
  WHERE username = 'admin';

  IF admin_count <> 1 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Migration requires exactly one admin_user with username=admin';
  END IF;

  RENAME TABLE admin_user TO sys_user;
  RENAME TABLE admin_login_log TO user_login_log;

  ALTER TABLE user_login_log
    DROP FOREIGN KEY fk_admin_login_log_user,
    CHANGE COLUMN admin_user_id user_id BIGINT UNSIGNED NULL COMMENT '用户ID';

  ALTER TABLE qso_log ADD COLUMN user_id BIGINT UNSIGNED NULL AFTER id;
  ALTER TABLE qsl_card ADD COLUMN user_id BIGINT UNSIGNED NULL AFTER id;
  ALTER TABLE storage_file
    ADD COLUMN user_id BIGINT UNSIGNED NULL AFTER id,
    ADD COLUMN file_key CHAR(32) NULL AFTER user_id;
  ALTER TABLE print_template
    DROP FOREIGN KEY fk_print_template_admin_user,
    CHANGE COLUMN admin_user_id user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID';
  ALTER TABLE qsl_confirm_log ADD COLUMN user_id BIGINT UNSIGNED NULL AFTER id;

  UPDATE qso_log SET user_id = admin_id WHERE user_id IS NULL;
  UPDATE qsl_card SET user_id = admin_id WHERE user_id IS NULL;
  UPDATE storage_file
    SET user_id = admin_id,
        file_key = LOWER(REPLACE(UUID(), '-', ''))
    WHERE user_id IS NULL OR file_key IS NULL;
  UPDATE qsl_confirm_log qcl
    JOIN qsl_card qc ON qc.id = qcl.qsl_card_id
    SET qcl.user_id = qc.user_id
    WHERE qcl.user_id IS NULL;

  ALTER TABLE qso_log
    MODIFY user_id BIGINT UNSIGNED NOT NULL,
    ADD KEY idx_qso_log_user_time (user_id, qso_time);
  ALTER TABLE qsl_card
    DROP FOREIGN KEY fk_qsl_card_qso_log,
    MODIFY user_id BIGINT UNSIGNED NOT NULL,
    ADD KEY idx_qsl_card_user_created (user_id, created_at);
  ALTER TABLE storage_file
    MODIFY user_id BIGINT UNSIGNED NOT NULL,
    MODIFY file_key CHAR(32) NOT NULL,
    ADD UNIQUE KEY uk_storage_file_key (file_key),
    ADD KEY idx_storage_file_user_status (user_id, status);
  ALTER TABLE print_template
    DROP FOREIGN KEY fk_print_template_background_file;
  ALTER TABLE qsl_confirm_log
    DROP FOREIGN KEY fk_qsl_confirm_log_card,
    MODIFY user_id BIGINT UNSIGNED NOT NULL,
    ADD KEY idx_qsl_confirm_log_user_time (user_id, created_at);

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

  INSERT INTO sys_role_permission(role_id, permission_id)
    SELECT r.id, p.id FROM sys_role r CROSS JOIN sys_permission p
    WHERE r.code = 'SUPER_ADMIN';
  INSERT INTO sys_role_permission(role_id, permission_id)
    SELECT r.id, p.id FROM sys_role r CROSS JOIN sys_permission p
    WHERE r.code = 'USER'
      AND p.code IN ('qso:read','qso:write','qsl:read','qsl:write',
                     'file:read','file:write','template:read','template:write');
  INSERT INTO sys_user_role(user_id, role_id)
    SELECT admin_id, id FROM sys_role WHERE code = 'SUPER_ADMIN';
END//
DELIMITER ;

CALL migrate_multi_user_rbac();
DROP PROCEDURE migrate_multi_user_rbac;
