package com.fiveelements.calendar.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fiveelements.calendar.auth.service.RedisSmsCodeService;
import com.fiveelements.calendar.auth.service.SmsGateway;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

class RedisSmsCodeServiceTest {
  @Test
  void generatesSixDigitCodeAndSendsThroughGateway() {
    StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> values = mock(ValueOperations.class);
    SmsGateway gateway = mock(SmsGateway.class);
    when(redis.opsForValue()).thenReturn(values);
    when(values.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(true);
    when(redis.execute(any(RedisScript.class), anyList(), anyString())).thenReturn(1L);
    var service = new RedisSmsCodeService(redis, gateway, 300, 60, 10, 30, 5);
    assertThat(service.sendRegisterCode("19900000001", "127.0.0.1").sent()).isTrue();
    ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
    verify(gateway).sendRegisterCode(eq("19900000001"), code.capture());
    assertThat(code.getValue()).matches("[0-9]{6}");
    verify(values)
        .set(startsWith("sms:register:code:"), eq(code.getValue()), eq(Duration.ofSeconds(300)));
  }

  @Test
  void returnsRetryWhenCooldownIsActive() {
    StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> values = mock(ValueOperations.class);
    when(redis.opsForValue()).thenReturn(values);
    when(values.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(false);
    when(redis.getExpire(anyString())).thenReturn(42L);
    SmsGateway gateway = mock(SmsGateway.class);
    var result =
        new RedisSmsCodeService(redis, gateway, 300, 60, 10, 30, 5)
            .sendRegisterCode("19900000001", "ip");
    assertThat(result.sent()).isFalse();
    assertThat(result.retryAfterSeconds()).isEqualTo(42);
    verifyNoInteractions(gateway);
  }

  @Test
  void rejectsExpiredAndOverAttemptCodes() {
    StringRedisTemplate redis = mock(StringRedisTemplate.class);
    SmsGateway gateway = mock(SmsGateway.class);
    var service = new RedisSmsCodeService(redis, gateway, 300, 60, 10, 30, 5);
    when(redis.execute(any(RedisScript.class), anyList(), anyString(), anyString(), anyString()))
        .thenReturn(-1L, -2L);
    assertThatThrownBy(() -> service.verifyAndConsumeRegisterCode("19900000001", "000000"))
        .hasMessageContaining("过期");
    assertThatThrownBy(() -> service.verifyAndConsumeRegisterCode("19900000001", "000000"))
        .hasMessageContaining("失败次数过多");
  }
}
