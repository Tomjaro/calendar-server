CREATE TABLE diary_comment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  diary_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  parent_id BIGINT NULL,
  reply_to_user_id BIGINT NULL,
  content VARCHAR(1000) NOT NULL,
  status CHAR(1) NOT NULL DEFAULT '0',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_comment_diary_time (diary_id, status, create_time),
  KEY idx_comment_parent (parent_id),
  CONSTRAINT fk_comment_diary FOREIGN KEY (diary_id) REFERENCES diary_record(id) ON DELETE CASCADE,
  CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES app_user(id)
) COMMENT='公开日记评论';
