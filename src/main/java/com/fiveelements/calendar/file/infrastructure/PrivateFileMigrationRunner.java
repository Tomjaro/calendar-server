package com.fiveelements.calendar.file.infrastructure;

import com.fiveelements.calendar.file.domain.FileRecord;
import com.fiveelements.calendar.file.domain.ObjectStoragePort;
import com.fiveelements.calendar.file.mapper.FileRecordMapper;
import java.nio.file.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.file.migration.enabled", havingValue = "true")
public class PrivateFileMigrationRunner implements ApplicationRunner {
  private final FileRecordMapper mapper;
  private final ObjectStoragePort storage;
  private final Path source;

  public PrivateFileMigrationRunner(
      FileRecordMapper mapper,
      ObjectStoragePort storage,
      @Value("${app.file.migration.local-path}") String source) {
    this.mapper = mapper;
    this.storage = storage;
    this.source = Paths.get(source).toAbsolutePath().normalize();
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    for (FileRecord row : mapper.selectActiveFiles()) {
      if (storage.exists(row.getStorageKey())) continue;
      Path file = source.resolve(row.getStorageKey()).normalize();
      if (!file.startsWith(source) || !Files.isRegularFile(file))
        throw new IllegalStateException("Local private file is missing: " + row.getStorageKey());
      storage.put(row.getStorageKey(), Files.readAllBytes(file), row.getContentType());
    }
  }
}
