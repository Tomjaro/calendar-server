package com.fiveelements.calendar.diary.controller;

import com.fiveelements.calendar.common.ApiResponse;
import com.fiveelements.calendar.diary.domain.DiaryQueryModels.MonthStatistics;
import com.fiveelements.calendar.diary.service.DiaryQueryService;
import com.fiveelements.calendar.security.SecurityContext;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/app-api/diaries/statistics")
public class DiaryStatisticsController {
  private final DiaryQueryService service;

  public DiaryStatisticsController(DiaryQueryService service) {
    this.service = service;
  }

  @GetMapping("/month")
  public ApiResponse<MonthStatistics> month(
      @RequestParam @Min(1901) @Max(2100) int year, @RequestParam @Min(1) @Max(12) int month) {
    return ApiResponse.ok(service.month(SecurityContext.currentUser().userId(), year, month));
  }
}
