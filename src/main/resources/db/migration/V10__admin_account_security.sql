ALTER TABLE sys_admin_user
  ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0 AFTER status,
  ADD COLUMN locked_until DATETIME DEFAULT NULL AFTER failed_login_attempts,
  ADD COLUMN password_changed_at DATETIME DEFAULT NULL AFTER locked_until;

CREATE TABLE IF NOT EXISTS admin_login_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  admin_user_id BIGINT DEFAULT NULL,
  username VARCHAR(64) NOT NULL,
  ip_address VARCHAR(64) DEFAULT NULL,
  success_flag CHAR(1) NOT NULL DEFAULT '1',
  failure_reason VARCHAR(128) DEFAULT NULL,
  login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_admin_login_username_time (username, login_time),
  KEY idx_admin_login_time (login_time)
) COMMENT='Admin login audit records';
