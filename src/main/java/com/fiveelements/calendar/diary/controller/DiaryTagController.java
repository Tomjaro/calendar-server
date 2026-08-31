package com.fiveelements.calendar.diary.controller;

import com.fiveelements.calendar.common.ApiResponse;
import com.fiveelements.calendar.diary.domain.DiaryQueryModels.TagView;
import com.fiveelements.calendar.diary.service.DiaryQueryService;
import com.fiveelements.calendar.security.SecurityContext;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app-api/diaries/tags")
public class DiaryTagController {
  private final DiaryQueryService service;

  public DiaryTagController(DiaryQueryService service) {
    this.service = service;
  }

  @GetMapping
  public ApiResponse<List<TagView>> list() {
    return ApiResponse.ok(service.tags(SecurityContext.currentUser().userId()));
  }
}
