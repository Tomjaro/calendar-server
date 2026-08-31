package com.fiveelements.calendar.reminder.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public final class ReminderModels {
  private ReminderModels() {}

  public record ReminderView(
      String reminderType,
      boolean enabled,
      LocalTime remindTime,
      String repeatWeek,
      boolean skipIfCompleted,
      String reminderText) {}

  public record SaveReminderRequest(
      @NotBlank @Pattern(regexp = "DAILY_DIARY|SOLAR_TERM|SCHEDULE") String reminderType,
      boolean enabled,
      LocalTime remindTime,
      @Pattern(regexp = "^([1-7](,[1-7])*)?$") String repeatWeek,
      boolean skipIfCompleted,
      @Size(max = 128) String reminderText) {}
}
