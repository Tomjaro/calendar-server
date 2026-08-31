package com.fiveelements.calendar.diary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fiveelements.calendar.diary.domain.CommentView;
import com.fiveelements.calendar.diary.domain.DiaryComment;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DiaryCommentMapper extends BaseMapper<DiaryComment> {
  List<CommentView> selectViews(@Param("viewerId") long viewerId, @Param("diaryId") long diaryId);

  CommentView selectViewById(@Param("id") long id);

  Long selectRootId(@Param("id") long id, @Param("diaryId") long diaryId);
}
