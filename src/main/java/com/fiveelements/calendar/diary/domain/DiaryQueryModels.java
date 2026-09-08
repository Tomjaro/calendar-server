package com.fiveelements.calendar.diary.domain;

import java.time.LocalDate;
import java.util.List;

public final class DiaryQueryModels {
  private DiaryQueryModels() {}

  public record MoodCount(String moodCode, long count) {}

  public record DailyMood(LocalDate date, Double score) {}

  public record MonthStatistics(
      int year,
      int month,
      long recordedDays,
      Double averageMoodScore,
      long favoriteCount,
      List<MoodCount> moodDistribution,
      List<DailyMood> dailyTrend) {}

  public record Summary(long recordedDays, Double averageMoodScore, long favoriteCount) {}

  public record TagView(long id, String tagName, int useCount) {}
}
