package com.fiveelements.calendar.auth.service;

import java.time.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!prod")
public class DevelopmentSmsCodeService implements SmsCodeService {
  private final String code;
  private final ConcurrentHashMap<String, Entry> codes = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Window> phoneDays = new ConcurrentHashMap<>(),
      ipHours = new ConcurrentHashMap<>();

  public DevelopmentSmsCodeService(@Value("${app.auth.dev-sms-code}") String code) {
    this.code = code;
  }

  @Override
  public synchronized SendResult sendRegisterCode(String phone, String ip) {
    Instant now = Instant.now();
    Entry existing = codes.get(phone);
    if (existing != null && Duration.between(existing.sentAt(), now).getSeconds() < 60) {
      long retry = 60 - Duration.between(existing.sentAt(), now).getSeconds();
      return new SendResult(false, 300, retry);
    }
    increment(
        phoneDays,
        "phone:" + phone,
        LocalDate.now().atStartOfDay().plusDays(1).atZone(ZoneId.systemDefault()).toInstant(),
        10,
        "该手机号今日验证码发送次数已达上限");
    increment(
        ipHours, "ip:" + ip, now.plus(1, java.time.temporal.ChronoUnit.HOURS), 30, "当前网络请求验证码过于频繁");
    codes.put(phone, new Entry(code, now, now.plusSeconds(300), 0));
    return new SendResult(true, 300, 60);
  }

  @Override
  public synchronized void verifyAndConsumeRegisterCode(String phone, String input) {
    Entry entry = codes.get(phone);
    if (entry == null || Instant.now().isAfter(entry.expiresAt())) {
      codes.remove(phone);
      throw new IllegalArgumentException("验证码不存在或已过期");
    }
    if (entry.failures() >= 5) {
      codes.remove(phone);
      throw new IllegalArgumentException("验证码失败次数过多，请重新获取");
    }
    if (!entry.code().equals(input)) {
      codes.put(
          phone, new Entry(entry.code(), entry.sentAt(), entry.expiresAt(), entry.failures() + 1));
      throw new IllegalArgumentException("验证码错误或已过期");
    }
    codes.remove(phone);
  }

  private void increment(
      ConcurrentHashMap<String, Window> map,
      String key,
      Instant resetAt,
      int limit,
      String message) {
    Window old = map.get(key);
    if (old == null || Instant.now().isAfter(old.resetAt())) old = new Window(0, resetAt);
    if (old.count() >= limit) throw new IllegalArgumentException(message);
    map.put(key, new Window(old.count() + 1, old.resetAt()));
  }

  private record Entry(String code, Instant sentAt, Instant expiresAt, int failures) {}

  private record Window(int count, Instant resetAt) {}
}
