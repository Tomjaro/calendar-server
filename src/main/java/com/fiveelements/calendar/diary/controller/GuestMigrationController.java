package com.fiveelements.calendar.diary.controller;

import com.fiveelements.calendar.common.ApiResponse;
import com.fiveelements.calendar.diary.domain.DiaryDtos.*;
import com.fiveelements.calendar.diary.service.DiaryService;
import com.fiveelements.calendar.security.SecurityContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app-api/sync")
public class GuestMigrationController {
  private final DiaryService service;

  public GuestMigrationController(DiaryService service) {
    this.service = service;
  }

  @PostMapping("/guest-data")
  public ApiResponse<MigrationResult> migrate(@Valid @RequestBody MigrationRequest request) {
    return ApiResponse.ok(service.migrate(SecurityContext.currentUser().userId(), request));
  }
}
