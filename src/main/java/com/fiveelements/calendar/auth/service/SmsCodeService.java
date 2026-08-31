package com.fiveelements.calendar.auth.service;

public interface SmsCodeService {
  SendResult sendRegisterCode(String phone, String ipAddress);

  void verifyAndConsumeRegisterCode(String phone, String code);

  record SendResult(boolean sent, long expiresInSeconds, long retryAfterSeconds) {}
}
