package com.fiveelements.calendar.admin.mapper;

import java.util.*;
import org.apache.ibatis.annotations.Param;

public interface SupportMapper {
  List<Map<String, Object>> selectVersions(
      @Param("platform") String platform, @Param("channel") String channel);

  int insertFeedback(
      @Param("userId") Long userId,
      @Param("contact") String contact,
      @Param("content") String content);

  Long lastInsertId();

  List<Map<String, Object>> selectFeedback(
      @Param("userId") long userId, @Param("limit") int limit, @Param("offset") int offset);
}
