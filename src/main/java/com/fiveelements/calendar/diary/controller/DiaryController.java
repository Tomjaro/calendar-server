package com.fiveelements.calendar.diary.controller;

import com.fiveelements.calendar.common.ApiResponse;
import com.fiveelements.calendar.diary.domain.DiaryDtos.*;
import com.fiveelements.calendar.diary.service.DiaryService;
import com.fiveelements.calendar.security.SecurityContext;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app-api/diaries")
public class DiaryController {
  private final DiaryService service;

  public DiaryController(DiaryService service) {
    this.service = service;
  }

  @GetMapping
  public ApiResponse<List<DiaryView>> list(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return ApiResponse.ok(service.list(userId(), startDate, endDate));
  }

  @GetMapping("/{date}")
  public ApiResponse<DiaryView> day(
      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ApiResponse.ok(service.findByDate(userId(), date));
  }

  @PostMapping
  public ApiResponse<DiaryView> create(@Valid @RequestBody SaveDiaryRequest request) {
    return ApiResponse.ok(service.create(userId(), request));
  }

  @PutMapping("/{id}")
  public ApiResponse<DiaryView> update(
      @PathVariable long id, @Valid @RequestBody SaveDiaryRequest request) {
    return ApiResponse.ok(service.update(userId(), id, request));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable long id) {
    service.delete(userId(), id);
    return ApiResponse.ok(null);
  }

  @GetMapping("/trash")
  public ApiResponse<List<DiaryView>> trash() {
    return ApiResponse.ok(service.trash(userId()));
  }

  @PostMapping("/{id}/restore")
  public ApiResponse<DiaryView> restore(@PathVariable long id) {
    return ApiResponse.ok(service.restore(userId(), id));
  }

  @DeleteMapping("/{id}/permanent")
  public ApiResponse<Void> permanent(@PathVariable long id) {
    service.permanentDelete(userId(), id);
    return ApiResponse.ok(null);
  }

  @GetMapping("/search")
  public ApiResponse<List<DiaryView>> search(
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(defaultValue = "") String mood,
      @RequestParam(defaultValue = "") String tag) {
    return ApiResponse.ok(service.search(userId(), keyword, startDate, endDate, mood, tag));
  }

  @GetMapping("/discover")
  public ApiResponse<List<FeedView>> discover(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.discover(userId(), page, size));
  }

  private long userId() {
    return SecurityContext.currentUser().userId();
  }
}
