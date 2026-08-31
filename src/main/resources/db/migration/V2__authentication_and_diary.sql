CREATE TABLE IF NOT EXISTS auth_refresh_token (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token_hash CHAR(64) NOT NULL,
  expires_at DATETIME NOT NULL,
  revoked_at DATETIME DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_token_hash (token_hash),
  KEY idx_user_active (user_id, revoked_at, expires_at)
) COMMENT='刷新令牌';

CREATE TABLE IF NOT EXISTS app_operation_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT DEFAULT NULL,
  identity_type VARCHAR(16) NOT NULL DEFAULT 'USER' COMMENT 'GUEST/USER',
  guest_id_hash VARCHAR(128) DEFAULT NULL,
  module_code VARCHAR(32) DEFAULT NULL,
  operation_code VARCHAR(64) DEFAULT NULL,
  object_type VARCHAR(32) DEFAULT NULL,
  object_id VARCHAR(64) DEFAULT NULL,
  ip_address VARCHAR(64) DEFAULT NULL,
  operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  success_flag CHAR(1) DEFAULT '1',
  error_code VARCHAR(64) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_user_operation_time (user_id, operation_time)
) COMMENT='用户操作日志';
