package com.fiveelements.calendar.reminder.service;

import com.fiveelements.calendar.reminder.domain.ReminderModels.*;
import com.fiveelements.calendar.reminder.mapper.ReminderMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReminderService {
  private final ReminderMapper mapper;

  public ReminderService(ReminderMapper mapper) {
    this.mapper = mapper;
  }

  public List<ReminderView> settings(long userId) {
    return mapper.selectSettings(userId);
  }

  public ReminderView save(long userId, SaveReminderRequest request) {
    mapper.upsert(userId, request);
    return new ReminderView(
        request.reminderType(),
        request.enabled(),
        request.remindTime(),
        request.repeatWeek(),
        request.skipIfCompleted(),
        request.reminderText());
  }
}
