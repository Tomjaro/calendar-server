package com.fiveelements.calendar.security;

import com.fiveelements.calendar.admin.domain.AdminPrincipal;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey key;
  private final long accessMinutes;

  public JwtService(
      @Value("${app.auth.jwt-secret}") String secret,
      @Value("${app.auth.access-token-minutes}") long accessMinutes) {
    if (secret.getBytes(StandardCharsets.UTF_8).length < 32)
      throw new IllegalStateException("JWT 密钥至少需要 32 字节");
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessMinutes = accessMinutes;
  }

  public String createAccessToken(long userId, String phone) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("phone", phone)
        .claim("type", "access")
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(accessMinutes, ChronoUnit.MINUTES)))
        .signWith(key)
        .compact();
  }

  public UserPrincipal parseAccessToken(String token) {
    var payload = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    if (!"access".equals(payload.get("type", String.class)))
      throw new IllegalArgumentException("Token 类型错误");
    return new UserPrincipal(
        Long.parseLong(payload.getSubject()), payload.get("phone", String.class));
  }

  public String createAdminToken(
      long adminId, String username, String role, List<String> permissions) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(String.valueOf(adminId))
        .claim("username", username)
        .claim("role", role)
        .claim("permissions", permissions)
        .claim("type", "admin")
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(accessMinutes, ChronoUnit.MINUTES)))
        .signWith(key)
        .compact();
  }

  public AdminPrincipal parseAdminToken(String token) {
    var payload = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    if (!"admin".equals(payload.get("type", String.class)))
      throw new IllegalArgumentException("Invalid admin token");
    Object raw = payload.get("permissions");
    List<String> permissions =
        raw instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
    return new AdminPrincipal(
        Long.parseLong(payload.getSubject()),
        payload.get("username", String.class),
        payload.get("role", String.class),
        permissions);
  }
}
