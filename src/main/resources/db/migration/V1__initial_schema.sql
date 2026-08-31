CREATE TABLE IF NOT EXISTS app_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  phone VARCHAR(32) NOT NULL COMMENT '手机号',
  password VARCHAR(255) DEFAULT NULL COMMENT '密码密文',
  nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  avatar_url VARCHAR(512) DEFAULT NULL COMMENT '头像地址',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '0正常 1冻结',
  cancel_status CHAR(1) NOT NULL DEFAULT '0' COMMENT '0正常 1注销申请 2已注销',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (id),
  UNIQUE KEY uk_phone (phone)
) COMMENT='App用户';

CREATE TABLE IF NOT EXISTS diary_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  record_date DATE NOT NULL,
  mood_code VARCHAR(32) DEFAULT NULL,
  mood_score TINYINT DEFAULT NULL,
  activity_content TEXT,
  feeling_content LONGTEXT,
  keyword_text VARCHAR(500) DEFAULT NULL,
  status CHAR(1) NOT NULL DEFAULT '1' COMMENT '0草稿 1完成 2删除',
  is_favorite CHAR(1) NOT NULL DEFAULT '0',
  privacy_type CHAR(1) NOT NULL DEFAULT '0',
  client_update_time DATETIME DEFAULT NULL,
  server_version INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  delete_time DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_date (user_id, record_date),
  KEY idx_user_update_time (user_id, update_time)
) COMMENT='每日记录';

CREATE TABLE IF NOT EXISTS guest_data_migration (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  guest_id_hash VARCHAR(128) NOT NULL,
  migration_batch_no VARCHAR(64) NOT NULL,
  total_count INT NOT NULL DEFAULT 0,
  success_count INT NOT NULL DEFAULT 0,
  failure_count INT NOT NULL DEFAULT 0,
  conflict_count INT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  failure_reason VARCHAR(500) DEFAULT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  finish_time DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_batch_no (migration_batch_no),
  KEY idx_user_create_time (user_id, create_time)
) COMMENT='游客数据迁移任务';

CREATE TABLE IF NOT EXISTS calendar_day_extension (
  id BIGINT NOT NULL AUTO_INCREMENT,
  calendar_date DATE NOT NULL,
  holiday_name VARCHAR(64) DEFAULT NULL,
  holiday_type VARCHAR(16) DEFAULT NULL,
  work_status CHAR(1) DEFAULT '0' COMMENT '0正常 1休息 2补班',
  suitable_items JSON DEFAULT NULL,
  avoid_items JSON DEFAULT NULL,
  description VARCHAR(1000) DEFAULT NULL,
  data_source VARCHAR(128) DEFAULT NULL,
  algorithm_version VARCHAR(32) DEFAULT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_calendar_date (calendar_date)
) COMMENT='日历扩展信息';
