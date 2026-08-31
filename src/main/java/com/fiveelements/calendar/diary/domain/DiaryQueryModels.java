package com.fiveelements.calendar.diary.domain;

import java.util.List;

public final class DiaryQueryModels {
  private DiaryQueryModels() {}

  public record MoodCount(String moodCode, long count) {}

  public record MonthStatistics(
      int year,
      int month,
      long recordedDays,
      Double averageMoodScore,
      long favoriteCount,
      List<MoodCount> moodDistribution) {}

  public record Summary(long recordedDays, Double averageMoodScore, long favoriteCount) {}

  public record TagView(long id, String tagName, int useCount) {}
}
