package com.fiveelements.calendar.file.infrastructure;

import com.fiveelements.calendar.file.domain.ObjectStoragePort;
import java.nio.file.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.file.storage-type", havingValue = "local", matchIfMissing = true)
public class LocalPrivateObjectStorage implements ObjectStoragePort {
  private final Path root;

  public LocalPrivateObjectStorage(@Value("${app.file.storage-path}") String path)
      throws Exception {
    root = Paths.get(path).toAbsolutePath().normalize();
    Files.createDirectories(root);
  }

  public void put(String key, byte[] content, String contentType) {
    try {
      Path path = resolve(key);
      Files.createDirectories(path.getParent());
      Files.write(path, content, StandardOpenOption.CREATE_NEW);
    } catch (Exception e) {
      throw new IllegalStateException("Private file write failed", e);
    }
  }

  public byte[] get(String key) {
    try {
      Path path = resolve(key);
      if (!Files.exists(path)) throw new IllegalArgumentException("Image file does not exist");
      return Files.readAllBytes(path);
    } catch (java.io.IOException e) {
      throw new IllegalStateException("Private file read failed", e);
    }
  }

  public boolean exists(String key) {
    return Files.exists(resolve(key));
  }

  public void delete(String key) {
    try {
      Files.deleteIfExists(resolve(key));
    } catch (Exception e) {
      throw new IllegalStateException("Private file delete failed", e);
    }
  }

  private Path resolve(String key) {
    Path path = root.resolve(key).normalize();
    if (!path.startsWith(root)) throw new IllegalArgumentException("Invalid storage key");
    return path;
  }
}
