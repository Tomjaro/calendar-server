package com.fiveelements.calendar.diary.domain;

import java.time.LocalDateTime;

public record CommentView(
    long id,
    long userId,
    String authorName,
    String avatarUrl,
    String content,
    Long parentId,
    Long replyToUserId,
    String replyToName,
    boolean mine,
    LocalDateTime createTime) {}
