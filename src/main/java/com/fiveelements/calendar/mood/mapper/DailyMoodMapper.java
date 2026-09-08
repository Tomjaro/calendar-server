package com.fiveelements.calendar.mood.mapper;

import com.fiveelements.calendar.mood.domain.DailyMoodModels.DailyMoodRow;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DailyMoodMapper {
  List<DailyMoodRow> selectRange(@Param("userId") long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);
  int upsert(@Param("userId") long userId,@Param("date") LocalDate date,@Param("code") String code,@Param("category") String category,@Param("score") BigDecimal score,@Param("level") int level,@Param("note") String note);
  DailyMoodRow selectOne(@Param("userId") long userId, @Param("date") LocalDate date);
}
