package com.fiveelements.calendar.admin.service;

import com.fiveelements.calendar.admin.mapper.SupportMapper;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AppSupportService {
  private final SupportMapper mapper;

  public AppSupportService(SupportMapper mapper) {
    this.mapper = mapper;
  }

  public List<Map<String, Object>> versions(String platform, String channel) {
    return mapper.selectVersions(platform, channel);
  }

  public long createFeedback(Long userId, String contact, String content) {
    mapper.insertFeedback(userId, contact, content);
    return mapper.lastInsertId();
  }

  public List<Map<String, Object>> feedback(long userId, int limit, int offset) {
    return mapper.selectFeedback(userId, limit, offset);
  }
}
