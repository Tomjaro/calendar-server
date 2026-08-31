package com.fiveelements.calendar.auth.service;

import com.fiveelements.calendar.auth.domain.AuthDtos.PasswordLoginRequest;
import com.fiveelements.calendar.auth.domain.AuthDtos.RefreshRow;
import com.fiveelements.calendar.auth.domain.AuthDtos.RegisterRequest;
import com.fiveelements.calendar.auth.domain.AuthDtos.TokenResponse;
import com.fiveelements.calendar.auth.domain.AuthDtos.UserRow;
import com.fiveelements.calendar.auth.mapper.AuthMapper;
import com.fiveelements.calendar.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final AuthMapper mapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final SmsCodeService smsCodeService;
  private final long refreshDays;
  private final SecureRandom random = new SecureRandom();

  public AuthService(
      AuthMapper mapper,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      SmsCodeService smsCodeService,
      @Value("${app.auth.refresh-token-days}") long refreshDays) {
    this.mapper = mapper;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.smsCodeService = smsCodeService;
    this.refreshDays = refreshDays;
  }

  @Transactional
  public TokenResponse register(RegisterRequest request) {
    smsCodeService.verifyAndConsumeRegisterCode(request.phone(), request.code());
    try {
      mapper.insertUser(
          request.phone(),
          passwordEncoder.encode(request.password()),
          "五行用户" + request.phone().substring(7));
      Long id = mapper.lastInsertId();
      registerDevice(
          id,
          request.deviceId(),
          request.platform(),
          request.deviceModel(),
          request.systemVersion(),
          request.appVersion());
      return issueTokens(id, request.phone());
    } catch (DuplicateKeyException exception) {
      throw new IllegalArgumentException("该手机号已经注册");
    }
  }

  public TokenResponse login(PasswordLoginRequest request) {
    return login(request, null);
  }

  public TokenResponse login(PasswordLoginRequest request, String ip) {
    UserRow user = mapper.selectUserByPhone(request.phone());
    if (user == null) throw new BadCredentialsException("手机号或密码错误");
    if (!passwordEncoder.matches(request.password(), user.password()))
      throw new BadCredentialsException("手机号或密码错误");
    if (!"0".equals(user.status()) || !"0".equals(user.cancelStatus()))
      throw new IllegalArgumentException("账号当前不可登录");
    mapper.updateLogin(user.id(), ip);
    mapper.insertLoginSuccess(user.id(), request, ip);
    registerDevice(
        user.id(),
        request.deviceId(),
        request.platform(),
        request.deviceModel(),
        request.systemVersion(),
        request.appVersion());
    return issueTokens(user.id(), user.phone());
  }

  @Transactional
  public TokenResponse refresh(String token) {
    String hash = sha256(token);
    RefreshRow row = mapper.selectRefresh(hash);
    if (row == null) throw new BadCredentialsException("Refresh Token 无效或已过期");
    mapper.revokeById(row.id());
    return issueTokens(row.userId(), row.phone());
  }

  public void logout(String token) {
    mapper.revokeByHash(sha256(token));
  }

  public void logFailedLogin(PasswordLoginRequest request, String ip, String reason) {
    mapper.insertLoginFailure(request, ip, reason);
  }

  private TokenResponse issueTokens(long userId, String phone) {
    byte[] bytes = new byte[48];
    random.nextBytes(bytes);
    String refresh = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    mapper.insertRefresh(userId, sha256(refresh), LocalDateTime.now().plusDays(refreshDays));
    return new TokenResponse(userId, jwtService.createAccessToken(userId, phone), refresh, 7200);
  }

  private void registerDevice(
      long userId,
      String deviceId,
      String platform,
      String model,
      String systemVersion,
      String appVersion) {
    if (deviceId == null || deviceId.isBlank()) return;
    mapper.upsertDevice(
        userId,
        deviceId,
        platform == null ? "UNKNOWN" : platform,
        model,
        systemVersion,
        appVersion);
  }

  private String sha256(String value) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception exception) {
      throw new IllegalStateException(exception);
    }
  }
}
