package com.fiveelements.calendar.diary.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PostDetail(
    long id,
    long authorId,
    String authorName,
    String avatarUrl,
    LocalDate recordDate,
    String moodCode,
    Integer moodScore,
    String activityContent,
    String feelingContent,
    boolean mine,
    long commentCount,
    LocalDateTime createTime) {}
