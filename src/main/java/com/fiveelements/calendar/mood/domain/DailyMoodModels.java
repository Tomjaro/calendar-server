package com.fiveelements.calendar.mood.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DailyMoodModels {
  private DailyMoodModels() {}
  public record SaveDailyMoodRequest(@NotNull LocalDate date,@NotBlank @Size(max=32) String moodCode,@Size(max=200) String note) {}
  public record DailyMoodRow(LocalDate date,String moodCode,String moodCategory,BigDecimal score,String note,LocalDateTime updateTime) {}
  public record DailyMoodView(LocalDate date,String moodCode,String moodName,String category,BigDecimal score,String icon,String note,LocalDateTime updateTime) {}
}
