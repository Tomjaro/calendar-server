package com.fiveelements.calendar.diary.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("diary_comment")
public class DiaryComment {
  @TableId(type = IdType.AUTO)
  private Long id;

  private Long diaryId;
  private Long userId;
  private Long parentId;
  private Long replyToUserId;
  private String content;

  @TableLogic(value = "0", delval = "1")
  private String status;

  private LocalDateTime createTime;
  private LocalDateTime updateTime;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getDiaryId() {
    return diaryId;
  }

  public void setDiaryId(Long value) {
    this.diaryId = value;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long value) {
    this.userId = value;
  }

  public Long getParentId() {
    return parentId;
  }

  public void setParentId(Long value) {
    this.parentId = value;
  }

  public Long getReplyToUserId() {
    return replyToUserId;
  }

  public void setReplyToUserId(Long value) {
    this.replyToUserId = value;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String value) {
    this.content = value;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    this.status = value;
  }

  public LocalDateTime getCreateTime() {
    return createTime;
  }

  public void setCreateTime(LocalDateTime value) {
    this.createTime = value;
  }

  public LocalDateTime getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(LocalDateTime value) {
    this.updateTime = value;
  }
}
