package com.fiveelements.calendar.reminder.controller;

import com.fiveelements.calendar.common.ApiResponse;
import com.fiveelements.calendar.reminder.domain.ReminderModels.*;
import com.fiveelements.calendar.reminder.service.ReminderService;
import com.fiveelements.calendar.security.SecurityContext;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app-api/reminders")
public class ReminderController {
  private final ReminderService service;

  public ReminderController(ReminderService service) {
    this.service = service;
  }

  @GetMapping("/settings")
  public ApiResponse<List<ReminderView>> list() {
    return ApiResponse.ok(service.settings(SecurityContext.currentUser().userId()));
  }

  @PutMapping("/settings")
  public ApiResponse<ReminderView> save(@Valid @RequestBody SaveReminderRequest request) {
    return ApiResponse.ok(service.save(SecurityContext.currentUser().userId(), request));
  }
}
