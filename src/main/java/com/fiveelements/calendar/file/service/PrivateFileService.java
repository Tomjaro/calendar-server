package com.fiveelements.calendar.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fiveelements.calendar.file.domain.FileRecord;
import com.fiveelements.calendar.file.mapper.FileRecordMapper;
import java.security.MessageDigest;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PrivateFileService {
  public interface StoragePort {
    void put(String key, byte[] content, String contentType);

    byte[] get(String key);

    boolean exists(String key);

    void delete(String key);
  }

  private final FileRecordMapper mapper;
  private final StoragePort storage;
  private final long maxSize;
  private static final Set<String> TYPES = Set.of("image/jpeg", "image/png");

  public PrivateFileService(
      FileRecordMapper mapper,
      StoragePort storage,
      @Value("${app.file.max-size-bytes}") long maxSize) {
    this.mapper = mapper;
    this.storage = storage;
    this.maxSize = maxSize;
  }

  @Transactional
  public FileView save(long userId, MultipartFile file) throws Exception {
    if (file.isEmpty()) throw new IllegalArgumentException("图片不能为空");
    if (file.getSize() > maxSize) throw new IllegalArgumentException("压缩后图片不能超过 3 MB");
    String type = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase();
    if (!TYPES.contains(type)) throw new IllegalArgumentException("仅支持 JPG、JPEG、PNG；HEIC 请先在客户端转换");
    byte[] bytes = file.getBytes();
    String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    String ext = type.equals("image/png") ? ".png" : ".jpg",
        key = userId + "/" + UUID.randomUUID() + ext;
    storage.put(key, bytes, type);
    try {
      FileRecord row = new FileRecord();
      row.setUserId(userId);
      row.setStorageKey(key);
      row.setOriginalName(file.getOriginalFilename());
      row.setContentType(type);
      row.setFileSize((long) bytes.length);
      row.setSha256(hash);
      row.setStatus("0");
      mapper.insert(row);
      return new FileView(row.getId(), "/app-api/files/" + row.getId(), type, bytes.length);
    } catch (Exception e) {
      storage.delete(key);
      throw e;
    }
  }

  public StoredFile load(long userId, long id) {
    FileRow row = find(userId, id);
    if (!storage.exists(row.storageKey())) throw new IllegalArgumentException("图片文件不存在");
    return new StoredFile(storage.get(row.storageKey()), row.contentType());
  }

  @Transactional
  public void delete(long userId, long id) {
    if (mapper.countDiaryUsage(id, userId) > 0)
      throw new IllegalArgumentException("图片正在被日记使用，请先从日记移除");
    FileRow row = find(userId, id);
    storage.delete(row.storageKey());
    mapper.deleteById(id);
  }

  private FileRow find(long userId, long id) {
    FileRecord row =
        mapper.selectOne(
            new LambdaQueryWrapper<FileRecord>()
                .eq(FileRecord::getId, id)
                .eq(FileRecord::getUserId, userId));
    if (row == null) throw new IllegalArgumentException("图片不存在");
    return new FileRow(row.getStorageKey(), row.getContentType());
  }

  public record FileView(long fileId, String url, String contentType, long size) {}

  public record StoredFile(byte[] content, String contentType) {}

  private record FileRow(String storageKey, String contentType) {}
}
