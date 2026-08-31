package com.fiveelements.calendar.user.domain;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class UserDtos {
  private UserDtos() {}

  public record ProfileView(
      long id,
      String maskedPhone,
      String nickname,
      String avatarUrl,
      String gender,
      LocalDate birthday,
      String region,
      String signature,
      LocalDateTime createTime) {}

  public record UserRow(
      long id,
      String phone,
      String nickname,
      String avatarUrl,
      String gender,
      LocalDate birthday,
      String region,
      String signature,
      LocalDateTime createTime) {}

  public record DeviceView(
      long id,
      String deviceId,
      String platform,
      String deviceModel,
      String systemVersion,
      String appVersion,
      LocalDateTime lastActiveTime,
      String status) {}

  public record UpdateProfileRequest(
      @Size(min = 1, max = 64) String nickname,
      @Size(max = 512) String avatarUrl,
      @Pattern(regexp = "^[012]?$", message = "性别编码不正确") String gender,
      LocalDate birthday,
      @Size(max = 128) String region,
      @Size(max = 255) String signature) {}
}
