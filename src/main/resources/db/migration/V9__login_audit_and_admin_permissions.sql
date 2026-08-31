CREATE TABLE IF NOT EXISTS app_login_log (
  id BIGINT NOT NULL AUTO_INCREMENT, user_id BIGINT DEFAULT NULL, phone VARCHAR(32) NOT NULL,
  login_type VARCHAR(16) NOT NULL DEFAULT 'PASSWORD', ip_address VARCHAR(64) DEFAULT NULL,
  device_id VARCHAR(128) DEFAULT NULL, platform VARCHAR(32) DEFAULT NULL,
  success_flag CHAR(1) NOT NULL DEFAULT '1', failure_reason VARCHAR(128) DEFAULT NULL,
  login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), KEY idx_login_time (login_time), KEY idx_login_user_time (user_id, login_time)
) COMMENT='App login audit records';

INSERT IGNORE INTO sys_permission(permission_code, permission_name) VALUES
('dashboard:view','View dashboard'),('user:view','View user metadata'),('user:manage','Freeze and logout users'),
('version:manage','Manage App releases'),('feedback:manage','Process feedback'),('audit:view','View audit records'),('role:view','View roles and permissions');
INSERT IGNORE INTO sys_role_permission(role_id, permission_id)
SELECT r.id,p.id FROM sys_role r CROSS JOIN sys_permission p WHERE r.role_code='SUPER_ADMIN';
