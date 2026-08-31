package com.fiveelements.calendar.auth.mapper;

import com.fiveelements.calendar.auth.domain.AuthDtos.*;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Param;

public interface AuthMapper {
  int insertUser(
      @Param("phone") String phone,
      @Param("password") String password,
      @Param("nickname") String nickname);

  Long lastInsertId();

  UserRow selectUserByPhone(@Param("phone") String phone);

  int updateLogin(@Param("id") long id, @Param("ip") String ip);

  int insertLoginSuccess(
      @Param("userId") long userId,
      @Param("request") PasswordLoginRequest request,
      @Param("ip") String ip);

  RefreshRow selectRefresh(@Param("hash") String hash);

  int revokeById(@Param("id") long id);

  int revokeByHash(@Param("hash") String hash);

  int insertLoginFailure(
      @Param("request") PasswordLoginRequest request,
      @Param("ip") String ip,
      @Param("reason") String reason);

  int insertRefresh(
      @Param("userId") long userId,
      @Param("hash") String hash,
      @Param("expires") LocalDateTime expires);

  int upsertDevice(
      @Param("userId") long userId,
      @Param("deviceId") String deviceId,
      @Param("platform") String platform,
      @Param("model") String model,
      @Param("systemVersion") String systemVersion,
      @Param("appVersion") String appVersion);
}
