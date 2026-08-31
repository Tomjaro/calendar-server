package com.fiveelements.calendar.auth.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class RedisSmsCodeService implements SmsCodeService {
  private static final DefaultRedisScript<Long> RATE =
      new DefaultRedisScript<>(
          "local n=redis.call('INCR',KEYS[1]);if n==1 then"
              + " redis.call('EXPIRE',KEYS[1],ARGV[1]);end;return n",
          Long.class);
  private static final DefaultRedisScript<Long> VERIFY =
      new DefaultRedisScript<>(
          "local v=redis.call('GET',KEYS[1]);if not v then return -1 end;if v==ARGV[1] then"
              + " redis.call('DEL',KEYS[1]);redis.call('DEL',KEYS[2]);return 1 end;local"
              + " n=redis.call('INCR',KEYS[2]);if n==1 then"
              + " redis.call('EXPIRE',KEYS[2],ARGV[2]);end;if n>=tonumber(ARGV[3]) then"
              + " redis.call('DEL',KEYS[1]);return -2 end;return 0",
          Long.class);
  private final StringRedisTemplate redis;
  private final SmsGateway gateway;
  private final SecureRandom random = new SecureRandom();
  private final int ttlSeconds, cooldownSeconds, phoneDailyLimit, ipHourlyLimit, maxFailures;

  public RedisSmsCodeService(
      StringRedisTemplate redis,
      SmsGateway gateway,
      @Value("${app.auth.sms.code-ttl-seconds:300}") int ttl,
      @Value("${app.auth.sms.cooldown-seconds:60}") int cooldown,
      @Value("${app.auth.sms.phone-daily-limit:10}") int phoneLimit,
      @Value("${app.auth.sms.ip-hourly-limit:30}") int ipLimit,
      @Value("${app.auth.sms.max-verification-failures:5}") int failures) {
    this.redis = redis;
    this.gateway = gateway;
    this.ttlSeconds = ttl;
    this.cooldownSeconds = cooldown;
    this.phoneDailyLimit = phoneLimit;
    this.ipHourlyLimit = ipLimit;
    this.maxFailures = failures;
  }

  public SendResult sendRegisterCode(String phone, String ip) {
    String cooldownKey = "sms:register:cooldown:" + phone;
    Boolean acquired =
        redis.opsForValue().setIfAbsent(cooldownKey, "1", Duration.ofSeconds(cooldownSeconds));
    if (!Boolean.TRUE.equals(acquired))
      return new SendResult(false, ttlSeconds, Math.max(redis.getExpire(cooldownKey), 1));
    checkRate("sms:register:phone-day:" + phone, 86400, phoneDailyLimit, "该手机号今日验证码发送次数已达上限");
    checkRate("sms:register:ip-hour:" + ip, 3600, ipHourlyLimit, "当前网络请求验证码过于频繁");
    String code = String.format("%06d", random.nextInt(1_000_000)),
        codeKey = "sms:register:code:" + phone;
    redis.opsForValue().set(codeKey, code, Duration.ofSeconds(ttlSeconds));
    try {
      gateway.sendRegisterCode(phone, code);
    } catch (RuntimeException e) {
      redis.delete(List.of(codeKey, cooldownKey));
      throw new IllegalStateException("验证码发送失败，请稍后重试", e);
    }
    return new SendResult(true, ttlSeconds, cooldownSeconds);
  }

  public void verifyAndConsumeRegisterCode(String phone, String input) {
    Long result =
        redis.execute(
            VERIFY,
            List.of("sms:register:code:" + phone, "sms:register:fail:" + phone),
            input,
            String.valueOf(ttlSeconds),
            String.valueOf(maxFailures));
    if (result == null || result == -1) throw new IllegalArgumentException("验证码不存在或已过期");
    if (result == -2) throw new IllegalArgumentException("验证码失败次数过多，请重新获取");
    if (result != 1) throw new IllegalArgumentException("验证码错误或已过期");
  }

  private void checkRate(String key, int seconds, int limit, String message) {
    Long count = redis.execute(RATE, List.of(key), String.valueOf(seconds));
    if (count != null && count > limit) throw new IllegalArgumentException(message);
  }
}
