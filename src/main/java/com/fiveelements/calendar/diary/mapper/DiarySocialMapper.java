package com.fiveelements.calendar.diary.mapper;

import com.fiveelements.calendar.diary.domain.PostDetail;
import org.apache.ibatis.annotations.Param;

public interface DiarySocialMapper {
  PostDetail selectPublicPost(@Param("viewerId") long viewerId, @Param("diaryId") long diaryId);
}
