package com.fiveelements.calendar.file.domain;

public interface ObjectStoragePort {
  void put(String key, byte[] content, String contentType);

  byte[] get(String key);

  boolean exists(String key);

  void delete(String key);
}
