package com.fiveelements.calendar.diary.mapper;

import com.fiveelements.calendar.diary.domain.DiaryDtos.*;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DiaryMapper {
  List<DiaryView> selectList(
      @Param("userId") long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

  DiaryView selectByDate(@Param("userId") long userId, @Param("date") LocalDate date);

  DiaryView selectById(@Param("userId") long userId, @Param("id") long id);

  int insertDiary(
      @Param("userId") long userId,
      @Param("request") SaveDiaryRequest request,
      @Param("activity") String activity,
      @Param("feeling") String feeling,
      @Param("status") String status,
      @Param("favorite") String favorite,
      @Param("privacy") String privacy);

  Long lastInsertId();

  int updateDiary(
      @Param("userId") long userId,
      @Param("id") long id,
      @Param("request") SaveDiaryRequest request,
      @Param("activity") String activity,
      @Param("feeling") String feeling,
      @Param("status") String status,
      @Param("favorite") String favorite,
      @Param("privacy") String privacy);

  int softDelete(@Param("userId") long userId, @Param("id") long id);

  List<DiaryView> selectTrash(@Param("userId") long userId);

  int restore(@Param("userId") long userId, @Param("id") long id);

  int permanentDelete(@Param("userId") long userId, @Param("id") long id);

  List<DiaryView> search(
      @Param("userId") long userId,
      @Param("keyword") String keyword,
      @Param("pattern") String pattern,
      @Param("start") LocalDate start,
      @Param("end") LocalDate end,
      @Param("mood") String mood,
      @Param("tag") String tag);

  List<FeedView> discover(
      @Param("viewerId") long viewerId, @Param("limit") int limit, @Param("offset") int offset);

  void insertMigration(
      @Param("userId") long userId,
      @Param("hash") String hash,
      @Param("batch") String batch,
      @Param("total") int total);

  int countDiaryOnDate(@Param("userId") long userId, @Param("date") LocalDate date);

  void insertGuestDiary(
      @Param("userId") long userId,
      @Param("item") GuestDiaryItem item,
      @Param("activity") String activity,
      @Param("feeling") String feeling,
      @Param("favorite") String favorite);

  void finishMigration(
      @Param("batch") String batch,
      @Param("success") int success,
      @Param("conflicts") int conflicts,
      @Param("status") String status);

  void deleteTagRelations(@Param("diaryId") long diaryId);

  void upsertTag(@Param("userId") long userId, @Param("name") String name);

  void insertTagRelation(@Param("diaryId") long diaryId, @Param("tagId") long tagId);

  void deleteImages(@Param("userId") long userId, @Param("diaryId") long diaryId);

  int countOwnedFile(@Param("userId") long userId, @Param("fileId") long fileId);

  void insertImage(
      @Param("userId") long userId,
      @Param("diaryId") long diaryId,
      @Param("fileId") long fileId,
      @Param("sort") int sort);

  void insertOperationLog(
      @Param("userId") long userId, @Param("operation") String operation, @Param("id") String id);
}
