ALTER TABLE diary_record
  MODIFY COLUMN privacy_type CHAR(1) NOT NULL DEFAULT '0' COMMENT '0仅自己可见 1公开';

CREATE INDEX idx_diary_public_feed
  ON diary_record (privacy_type, status, create_time, id);
