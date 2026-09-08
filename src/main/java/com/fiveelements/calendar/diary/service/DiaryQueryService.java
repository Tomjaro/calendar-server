package com.fiveelements.calendar.diary.service;

import com.fiveelements.calendar.diary.domain.DiaryQueryModels.*;
import com.fiveelements.calendar.diary.mapper.DiaryQueryMapper;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DiaryQueryService {
  private final DiaryQueryMapper mapper;

  public DiaryQueryService(DiaryQueryMapper mapper) {
    this.mapper = mapper;
  }

  public MonthStatistics month(long userId, int year, int month) {
    YearMonth yearMonth = YearMonth.of(year, month);
    LocalDate start = yearMonth.atDay(1);
    LocalDate end = yearMonth.atEndOfMonth();
    Summary summary = mapper.selectSummary(userId, start, end);
    return new MonthStatistics(
        year,
        month,
        summary.recordedDays(),
        summary.averageMoodScore(),
        summary.favoriteCount(),
        mapper.selectMoodCounts(userId, start, end),
        mapper.selectDailyScores(userId, start, end));
  }

  public List<TagView> tags(long userId) {
    return mapper.selectTags(userId);
  }
}
