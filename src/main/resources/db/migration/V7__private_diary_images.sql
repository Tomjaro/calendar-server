CREATE TABLE IF NOT EXISTS file_record (
 id BIGINT NOT NULL AUTO_INCREMENT,user_id BIGINT NOT NULL,storage_key VARCHAR(255) NOT NULL,original_name VARCHAR(255) DEFAULT NULL,
 content_type VARCHAR(64) NOT NULL,file_size BIGINT NOT NULL,sha256 CHAR(64) NOT NULL,status CHAR(1) NOT NULL DEFAULT '0',create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(id),UNIQUE KEY uk_storage_key(storage_key),KEY idx_user_time(user_id,create_time)
) COMMENT='私有文件';
CREATE TABLE IF NOT EXISTS diary_image (
 id BIGINT NOT NULL AUTO_INCREMENT,diary_id BIGINT NOT NULL,user_id BIGINT NOT NULL,file_id BIGINT NOT NULL,sort_num INT NOT NULL DEFAULT 0,create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(id),UNIQUE KEY uk_diary_file(diary_id,file_id),KEY idx_diary_sort(diary_id,sort_num)
) COMMENT='日记图片';
