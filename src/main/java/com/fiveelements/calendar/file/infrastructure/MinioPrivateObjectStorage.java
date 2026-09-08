package com.fiveelements.calendar.file.infrastructure;

import com.fiveelements.calendar.file.domain.ObjectStoragePort;
import io.minio.*;
import java.io.ByteArrayInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.file.storage-type", havingValue = "minio")
public class MinioPrivateObjectStorage implements ObjectStoragePort {
  private final MinioClient client;
  private final String bucket;

  public MinioPrivateObjectStorage(
      @Value("${app.file.minio.endpoint}") String endpoint,
      @Value("${app.file.minio.access-key}") String accessKey,
      @Value("${app.file.minio.secret-key}") String secretKey,
      @Value("${app.file.minio.bucket}") String bucket) {
    this.client =
        MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
    this.bucket = bucket;
    try {
      if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build()))
        client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
    } catch (Exception e) {
      throw new IllegalStateException("MinIO private bucket initialization failed", e);
    }
  }

  public void put(String key, byte[] content, String contentType) {
    try {
      client.putObject(
          PutObjectArgs.builder().bucket(bucket).object(key).stream(
                  new ByteArrayInputStream(content), content.length, -1)
              .contentType(contentType)
              .build());
    } catch (Exception e) {
      throw new IllegalStateException("MinIO upload failed", e);
    }
  }

  public byte[] get(String key) {
    try (var stream =
        client.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build())) {
      return stream.readAllBytes();
    } catch (Exception e) {
      throw new IllegalArgumentException("Image file does not exist", e);
    }
  }

  public boolean exists(String key) {
    try {
      client.statObject(StatObjectArgs.builder().bucket(bucket).object(key).build());
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public void delete(String key) {
    try {
      client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
    } catch (Exception e) {
      throw new IllegalStateException("MinIO delete failed", e);
    }
  }
}
