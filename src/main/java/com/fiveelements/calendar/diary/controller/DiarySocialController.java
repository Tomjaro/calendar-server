package com.fiveelements.calendar.diary.controller;

import com.fiveelements.calendar.common.ApiResponse;
import com.fiveelements.calendar.diary.domain.CommentRequest;
import com.fiveelements.calendar.diary.domain.CommentView;
import com.fiveelements.calendar.diary.domain.PostDetail;
import com.fiveelements.calendar.diary.service.DiarySocialService;
import com.fiveelements.calendar.security.SecurityContext;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app-api/social/diaries")
public class DiarySocialController {
  private final DiarySocialService service;

  public DiarySocialController(DiarySocialService service) {
    this.service = service;
  }

  @GetMapping("/{diaryId}")
  public ApiResponse<PostDetail> detail(@PathVariable long diaryId) {
    return ApiResponse.ok(service.detail(userId(), diaryId));
  }

  @GetMapping("/{diaryId}/comments")
  public ApiResponse<List<CommentView>> comments(@PathVariable long diaryId) {
    return ApiResponse.ok(service.comments(userId(), diaryId));
  }

  @PostMapping("/{diaryId}/comments")
  public ApiResponse<CommentView> comment(
      @PathVariable long diaryId, @Valid @RequestBody CommentRequest request) {
    return ApiResponse.ok(service.comment(userId(), diaryId, request));
  }

  @DeleteMapping("/comments/{commentId}")
  public ApiResponse<Void> delete(@PathVariable long commentId) {
    service.deleteComment(userId(), commentId);
    return ApiResponse.ok(null);
  }

  private long userId() {
    return SecurityContext.currentUser().userId();
  }
}
