package com.fiveelements.calendar.diary.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class DiaryDtos {
  private DiaryDtos() {}

  public record SaveDiaryRequest(
      @NotNull LocalDate recordDate,
      @Size(max = 32) String moodCode,
      @Min(1) @Max(5) Integer moodScore,
      @Size(max = 5000) String activityContent,
      @Size(max = 10000) String feelingContent,
      @Size(max = 500) String keywordText,
      @Size(max = 20) List<@Size(min = 1, max = 32) String> tags,
      @Size(max = 9) List<Long> fileIds,
      @NotNull Integer serverVersion,
      boolean favorite,
      boolean draft) {}

  public record DiaryView(
      long id,
      LocalDate recordDate,
      String moodCode,
      Integer moodScore,
      String activityContent,
      String feelingContent,
      String keywordText,
      String status,
      boolean favorite,
      int serverVersion,
      LocalDateTime createTime,
      LocalDateTime updateTime) {}

  public record GuestDiaryItem(
      String localId,
      @NotNull LocalDate recordDate,
      String moodCode,
      Integer moodScore,
      @Size(max = 5000) String activityContent,
      @Size(max = 10000) String feelingContent,
      @Size(max = 500) String keywordText,
      boolean favorite) {}

  public record MigrationRequest(
      @Size(min = 16, max = 128) String guestIdHash,
      @Size(min = 1, max = 200) List<GuestDiaryItem> diaries) {}

  public record MigrationResult(
      String batchNo,
      int totalCount,
      int successCount,
      int conflictCount,
      List<LocalDate> conflictDates) {}
}
