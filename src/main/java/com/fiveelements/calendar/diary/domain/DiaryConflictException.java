package com.fiveelements.calendar.diary.domain;

public class DiaryConflictException extends RuntimeException {
  public DiaryConflictException(String message) {
    super(message);
  }
}
