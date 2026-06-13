CREATE TABLE qsl_share (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  share_token_hash CHAR(64) NOT NULL,
  expires_at DATETIME(3) NULL,
  record_limit INT NOT NULL DEFAULT 10,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_qsl_share_user (user_id),
  UNIQUE KEY uk_qsl_share_token_hash (share_token_hash),
  KEY idx_qsl_share_enabled_expire (enabled, expires_at)
) ENGINE=InnoDB COMMENT='通联记录嵌入分享';
