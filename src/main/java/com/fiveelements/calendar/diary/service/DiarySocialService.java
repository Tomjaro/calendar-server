package com.fiveelements.calendar.diary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fiveelements.calendar.diary.domain.CommentRequest;
import com.fiveelements.calendar.diary.domain.CommentView;
import com.fiveelements.calendar.diary.domain.DiaryComment;
import com.fiveelements.calendar.diary.domain.PostDetail;
import com.fiveelements.calendar.diary.mapper.DiaryCommentMapper;
import com.fiveelements.calendar.diary.mapper.DiarySocialMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiarySocialService {
  private final DiarySocialMapper socialMapper;
  private final DiaryCommentMapper commentMapper;

  public DiarySocialService(DiarySocialMapper socialMapper, DiaryCommentMapper commentMapper) {
    this.socialMapper = socialMapper;
    this.commentMapper = commentMapper;
  }

  public PostDetail detail(long viewerId, long diaryId) {
    PostDetail post = socialMapper.selectPublicPost(viewerId, diaryId);
    if (post == null) throw new IllegalArgumentException("这条动态不存在或已设为私密");
    return post;
  }

  public List<CommentView> comments(long viewerId, long diaryId) {
    detail(viewerId, diaryId);
    return commentMapper.selectViews(viewerId, diaryId);
  }

  @Transactional
  public CommentView comment(long userId, long diaryId, CommentRequest request) {
    detail(userId, diaryId);
    Long root = null;
    if (request.parentId() != null) {
      root = commentMapper.selectRootId(request.parentId(), diaryId);
      if (root == null) throw new IllegalArgumentException("回复的评论不存在");
    }
    DiaryComment entity = new DiaryComment();
    entity.setDiaryId(diaryId);
    entity.setUserId(userId);
    entity.setParentId(root);
    entity.setReplyToUserId(request.replyToUserId());
    entity.setContent(request.content().trim());
    entity.setStatus("0");
    commentMapper.insert(entity);
    return commentMapper.selectViewById(entity.getId());
  }

  @Transactional
  public void deleteComment(long userId, long commentId) {
    DiaryComment owned =
        commentMapper.selectOne(
            new LambdaQueryWrapper<DiaryComment>()
                .eq(DiaryComment::getId, commentId)
                .eq(DiaryComment::getUserId, userId));
    if (owned == null) throw new IllegalArgumentException("评论不存在或无权删除");
    commentMapper.deleteById(commentId);
  }
}
