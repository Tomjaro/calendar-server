package com.fiveelements.calendar.common;

import com.fiveelements.calendar.diary.domain.DiaryConflictException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<Void> badRequest(Exception exception) {
    return new ApiResponse<>(400, exception.getMessage(), null);
  }

  @ExceptionHandler({BadCredentialsException.class, AccessDeniedException.class})
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiResponse<Void> unauthorized(Exception exception) {
    return new ApiResponse<>(401, exception.getMessage(), null);
  }

  @ExceptionHandler(DiaryConflictException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiResponse<Void> conflict(DiaryConflictException exception) {
    return new ApiResponse<>(409, exception.getMessage(), null);
  }
}
