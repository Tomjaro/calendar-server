package com.fiveelements.calendar.mood.controller;

import com.fiveelements.calendar.common.ApiResponse;
import com.fiveelements.calendar.mood.domain.DailyMoodModels.*;
import com.fiveelements.calendar.mood.service.DailyMoodService;
import com.fiveelements.calendar.security.SecurityContext;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app-api/moods")
public class DailyMoodController {
  private final DailyMoodService service;
  public DailyMoodController(DailyMoodService service) { this.service = service; }
  @GetMapping
  public ApiResponse<List<DailyMoodView>> list(
      @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return ApiResponse.ok(service.list(SecurityContext.currentUser().userId(),startDate,endDate));
  }
  @PutMapping
  public ApiResponse<DailyMoodView> save(@Valid @RequestBody SaveDailyMoodRequest request) {
    return ApiResponse.ok(service.save(SecurityContext.currentUser().userId(),request));
  }
}
