package com.fiveelements.calendar.reminder.mapper;

import com.fiveelements.calendar.reminder.domain.ReminderModels.*;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ReminderMapper {
  List<ReminderView> selectSettings(@Param("userId") long userId);

  int upsert(@Param("userId") long userId, @Param("request") SaveReminderRequest request);
}
