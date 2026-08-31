INSERT IGNORE INTO sys_permission(permission_code, permission_name) VALUES
('admin:manage','Manage administrator accounts'),
('role:manage','Manage roles and permissions');

INSERT IGNORE INTO sys_role_permission(role_id, permission_id)
SELECT r.id,p.id FROM sys_role r CROSS JOIN sys_permission p WHERE r.role_code='SUPER_ADMIN';
