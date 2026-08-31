package com.fiveelements.calendar.diary.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
    @NotBlank @Size(max = 1000) String content, Long parentId, Long replyToUserId) {}
