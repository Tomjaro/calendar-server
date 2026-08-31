package com.fiveelements.calendar.auth.domain;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
  private AuthDtos() {}

  public record PhoneRequest(
      @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone) {}

  public record RegisterRequest(
      @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone,
      @NotBlank String code,
      @NotBlank
          @Size(min = 8, max = 30)
          @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密码至少包含字母和数字")
          String password,
      @AssertTrue(message = "必须同意用户协议和隐私政策") boolean agreedToTerms,
      @Size(max = 128) String deviceId,
      @Size(max = 16) String platform,
      @Size(max = 128) String deviceModel,
      @Size(max = 64) String systemVersion,
      @Size(max = 32) String appVersion) {}

  public record PasswordLoginRequest(
      @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone,
      @NotBlank String password,
      @Size(max = 128) String deviceId,
      @Size(max = 16) String platform,
      @Size(max = 128) String deviceModel,
      @Size(max = 64) String systemVersion,
      @Size(max = 32) String appVersion) {}

  public record RefreshRequest(@NotBlank String refreshToken) {}

  public record TokenResponse(
      long userId, String accessToken, String refreshToken, long expiresInSeconds) {}

  public record UserRow(
      long id, String phone, String password, String status, String cancelStatus) {}

  public record RefreshRow(long id, long userId, String phone) {}
}
