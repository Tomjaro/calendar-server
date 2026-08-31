package com.fiveelements.calendar.auth.service;

public interface SmsGateway {
  void sendRegisterCode(String phone, String code);
}
