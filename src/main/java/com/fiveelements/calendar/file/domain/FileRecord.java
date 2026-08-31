package com.fiveelements.calendar.file.domain;

import com.baomidou.mybatisplus.annotation.*;

@TableName("file_record")
public class FileRecord {
  @TableId(type = IdType.AUTO)
  private Long id;

  private Long userId;
  private String storageKey;
  private String originalName;
  private String contentType;
  private Long fileSize;
  private String sha256;

  @TableLogic(value = "0", delval = "1")
  private String status;

  public Long getId() {
    return id;
  }

  public void setId(Long v) {
    id = v;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long v) {
    userId = v;
  }

  public String getStorageKey() {
    return storageKey;
  }

  public void setStorageKey(String v) {
    storageKey = v;
  }

  public String getOriginalName() {
    return originalName;
  }

  public void setOriginalName(String v) {
    originalName = v;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String v) {
    contentType = v;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public void setFileSize(Long v) {
    fileSize = v;
  }

  public String getSha256() {
    return sha256;
  }

  public void setSha256(String v) {
    sha256 = v;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String v) {
    status = v;
  }
}
