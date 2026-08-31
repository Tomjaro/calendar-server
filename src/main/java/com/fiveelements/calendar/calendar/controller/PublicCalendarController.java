package com.fiveelements.calendar.calendar.controller;

import com.fiveelements.calendar.calendar.domain.CalendarDayView;
import com.fiveelements.calendar.common.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/app-api/public/calendar")
public class PublicCalendarController {
  private final com.fiveelements.calendar.calendar.CalendarService calendarService;

  public PublicCalendarController(
      com.fiveelements.calendar.calendar.CalendarService calendarService) {
    this.calendarService = calendarService;
  }

  @GetMapping("/month")
  public ApiResponse<com.fiveelements.calendar.calendar.domain.MonthView> month(
      @RequestParam @Min(1901) @Max(2100) int year, @RequestParam @Min(1) @Max(12) int month) {
    return ApiResponse.ok(calendarService.month(year, month));
  }

  @GetMapping("/day")
  public ApiResponse<CalendarDayView> day(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ApiResponse.ok(calendarService.day(date));
  }
}
