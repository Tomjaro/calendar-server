package com.fiveelements.calendar.diary.service;

import com.fiveelements.calendar.diary.domain.DiaryConflictException;
import com.fiveelements.calendar.diary.domain.DiaryDtos.*;
import com.fiveelements.calendar.diary.mapper.DiaryMapper;
import java.time.LocalDate;
import java.util.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiaryService {
  private final DiaryMapper mapper;

  public DiaryService(DiaryMapper mapper) {
    this.mapper = mapper;
  }

  public List<DiaryView> list(long uid, LocalDate start, LocalDate end) {
    return mapper.selectList(uid, start, end);
  }

  public DiaryView findByDate(long uid, LocalDate date) {
    DiaryView value = mapper.selectByDate(uid, date);
    if (value == null) throw new IllegalArgumentException("该日期没有云端记录");
    return value;
  }

  @Transactional
  public DiaryView create(long uid, SaveDiaryRequest r) {
    try {
      mapper.insertDiary(
          uid,
          r,
          safe(r.activityContent()),
          safe(r.feelingContent()),
          r.draft() ? "0" : "1",
          r.favorite() ? "1" : "0",
          privacy(r.privacyType()));
      long id = mapper.lastInsertId();
      syncTags(uid, id, r.tags());
      syncImages(uid, id, r.fileIds());
      log(uid, "DIARY_CREATE", id);
      return findById(uid, id);
    } catch (DuplicateKeyException e) {
      throw new IllegalArgumentException("该日期已有云端记录，请执行更新");
    }
  }

  @Transactional
  public DiaryView update(long uid, long id, SaveDiaryRequest r) {
    int n =
        mapper.updateDiary(
            uid,
            id,
            r,
            safe(r.activityContent()),
            safe(r.feelingContent()),
            r.draft() ? "0" : "1",
            r.favorite() ? "1" : "0",
            privacy(r.privacyType()));
    if (n == 0) throw new DiaryConflictException("云端记录已变化，请先刷新后再选择保留本机或云端内容");
    syncTags(uid, id, r.tags());
    syncImages(uid, id, r.fileIds());
    log(uid, "DIARY_UPDATE", id);
    return findById(uid, id);
  }

  @Transactional
  public void delete(long uid, long id) {
    if (mapper.softDelete(uid, id) == 0) throw new IllegalArgumentException("记录不存在");
    log(uid, "DIARY_DELETE", id);
  }

  public List<DiaryView> trash(long uid) {
    return mapper.selectTrash(uid);
  }

  @Transactional
  public DiaryView restore(long uid, long id) {
    if (mapper.restore(uid, id) == 0) throw new IllegalArgumentException("回收站记录不存在");
    log(uid, "DIARY_RESTORE", id);
    return findById(uid, id);
  }

  @Transactional
  public void permanentDelete(long uid, long id) {
    if (mapper.permanentDelete(uid, id) == 0) throw new IllegalArgumentException("仅能永久删除回收站记录");
    mapper.deleteTagRelations(id);
    log(uid, "DIARY_PERMANENT_DELETE", id);
  }

  public List<DiaryView> search(
      long uid, String keyword, LocalDate start, LocalDate end, String mood, String tag) {
    String key = clean(keyword);
    return mapper.search(uid, key, "%" + key + "%", start, end, clean(mood), clean(tag));
  }

  public List<FeedView> discover(long uid, int page, int size) {
    int safeSize = Math.max(1, Math.min(size, 50));
    return mapper.discover(uid, safeSize, Math.max(0, page) * safeSize);
  }

  @Transactional
  public MigrationResult migrate(long uid, MigrationRequest r) {
    String batch = UUID.randomUUID().toString();
    List<LocalDate> conflicts = new ArrayList<>();
    int success = 0;
    mapper.insertMigration(uid, r.guestIdHash(), batch, r.diaries().size());
    for (GuestDiaryItem item : r.diaries()) {
      if (mapper.countDiaryOnDate(uid, item.recordDate()) > 0) {
        conflicts.add(item.recordDate());
        continue;
      }
      mapper.insertGuestDiary(
          uid,
          item,
          safe(item.activityContent()),
          safe(item.feelingContent()),
          item.favorite() ? "1" : "0");
      success++;
    }
    String status = conflicts.isEmpty() ? "SUCCESS" : "PARTIAL_FAILED";
    mapper.finishMigration(batch, success, conflicts.size(), status);
    return new MigrationResult(batch, r.diaries().size(), success, conflicts.size(), conflicts);
  }

  private DiaryView findById(long uid, long id) {
    return mapper.selectById(uid, id);
  }

  private String safe(String v) {
    return v == null ? "" : v;
  }

  private String clean(String v) {
    return v == null ? "" : v.trim();
  }

  private String privacy(String v) {
    return "PUBLIC".equals(v) ? "1" : "0";
  }

  private void syncTags(long uid, long id, List<String> tags) {
    mapper.deleteTagRelations(id);
    if (tags == null) return;
    tags.stream()
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .distinct()
        .limit(20)
        .forEach(
            name -> {
              mapper.upsertTag(uid, name);
              mapper.insertTagRelation(id, mapper.lastInsertId());
            });
  }

  private void syncImages(long uid, long id, List<Long> ids) {
    mapper.deleteImages(uid, id);
    if (ids == null) return;
    int sort = 0;
    for (Long fileId : ids.stream().filter(Objects::nonNull).distinct().limit(9).toList()) {
      if (mapper.countOwnedFile(uid, fileId) == 0)
        throw new IllegalArgumentException("图片不存在或不属于当前用户");
      mapper.insertImage(uid, id, fileId, sort++);
    }
  }

  private void log(long uid, String operation, long id) {
    mapper.insertOperationLog(uid, operation, String.valueOf(id));
  }
}
