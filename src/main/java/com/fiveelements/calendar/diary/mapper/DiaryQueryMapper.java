package com.fiveelements.calendar.diary.mapper;

import com.fiveelements.calendar.diary.domain.DiaryQueryModels.*;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DiaryQueryMapper {
  Summary selectSummary(
      @Param("userId") long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

  List<MoodCount> selectMoodCounts(
      @Param("userId") long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

  List<TagView> selectTags(@Param("userId") long userId);
}
