CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  role_code VARCHAR(64) NOT NULL,
  role_name VARCHAR(64) NOT NULL,
  status CHAR(1) NOT NULL DEFAULT '0',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), UNIQUE KEY uk_role_code (role_code)
) COMMENT='Admin roles';

CREATE TABLE IF NOT EXISTS sys_admin_user (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(255) NOT NULL,
  display_name VARCHAR(64) NOT NULL,
  status CHAR(1) NOT NULL DEFAULT '0',
  last_login_time DATETIME DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id), UNIQUE KEY uk_admin_username (username)
) COMMENT='Admin users';

CREATE TABLE IF NOT EXISTS sys_admin_user_role (
  admin_user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (admin_user_id, role_id)
) COMMENT='Admin user roles';

CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT NOT NULL AUTO_INCREMENT,
  permission_code VARCHAR(96) NOT NULL,
  permission_name VARCHAR(96) NOT NULL,
  PRIMARY KEY (id), UNIQUE KEY uk_permission_code (permission_code)
) COMMENT='Admin permissions';

CREATE TABLE IF NOT EXISTS sys_role_permission (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id)
) COMMENT='Role permissions';

CREATE TABLE IF NOT EXISTS admin_operation_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  admin_user_id BIGINT NOT NULL,
  operation_code VARCHAR(64) NOT NULL,
  object_type VARCHAR(32) DEFAULT NULL,
  object_id VARCHAR(64) DEFAULT NULL,
  ip_address VARCHAR(64) DEFAULT NULL,
  success_flag CHAR(1) NOT NULL DEFAULT '1',
  operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), KEY idx_admin_operation_time (admin_user_id, operation_time)
) COMMENT='Admin audit log';

CREATE TABLE IF NOT EXISTS app_version (
  id BIGINT NOT NULL AUTO_INCREMENT,
  platform VARCHAR(16) NOT NULL,
  version_name VARCHAR(32) NOT NULL,
  version_code INT NOT NULL,
  download_url VARCHAR(512) DEFAULT NULL,
  release_notes TEXT,
  force_update CHAR(1) NOT NULL DEFAULT '0',
  status CHAR(1) NOT NULL DEFAULT '0',
  publish_time DATETIME DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), UNIQUE KEY uk_platform_version_code (platform, version_code)
) COMMENT='App releases';

CREATE TABLE IF NOT EXISTS user_feedback (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT DEFAULT NULL,
  contact VARCHAR(128) DEFAULT NULL,
  content TEXT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  admin_reply TEXT DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id), KEY idx_feedback_status_time (status, create_time)
) COMMENT='User feedback';

INSERT IGNORE INTO sys_role(role_code, role_name) VALUES ('SUPER_ADMIN', 'Super administrator');
