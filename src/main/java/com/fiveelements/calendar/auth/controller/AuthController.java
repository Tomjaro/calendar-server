package com.fiveelements.calendar.auth.controller;

import com.fiveelements.calendar.auth.domain.AuthDtos.*;
import com.fiveelements.calendar.auth.service.AuthService;
import com.fiveelements.calendar.auth.service.SmsCodeService;
import com.fiveelements.calendar.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app-api/auth")
public class AuthController {
  private final AuthService authService;
  private final SmsCodeService smsCodeService;

  public AuthController(AuthService authService, SmsCodeService smsCodeService) {
    this.authService = authService;
    this.smsCodeService = smsCodeService;
  }

  @PostMapping("/send-register-code")
  public ApiResponse<Map<String, Object>> sendRegisterCode(
      @Valid @RequestBody PhoneRequest request, HttpServletRequest servletRequest) {
    var result = smsCodeService.sendRegisterCode(request.phone(), clientIp(servletRequest));
    return ApiResponse.ok(
        Map.of(
            "sent",
            result.sent(),
            "expiresInSeconds",
            result.expiresInSeconds(),
            "retryAfterSeconds",
            result.retryAfterSeconds()));
  }

  @PostMapping("/register")
  public ApiResponse<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ApiResponse.ok(authService.register(request));
  }

  @PostMapping("/login/password")
  public ApiResponse<TokenResponse> login(
      @Valid @RequestBody PasswordLoginRequest request, HttpServletRequest servletRequest) {
    try {
      return ApiResponse.ok(authService.login(request, clientIp(servletRequest)));
    } catch (RuntimeException exception) {
      authService.logFailedLogin(
          request, clientIp(servletRequest), exception.getClass().getSimpleName());
      throw exception;
    }
  }

  @PostMapping("/refresh-token")
  public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
    return ApiResponse.ok(authService.refresh(request.refreshToken()));
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout(@Valid @RequestBody RefreshRequest request) {
    authService.logout(request.refreshToken());
    return ApiResponse.ok(null);
  }

  private String clientIp(HttpServletRequest request) {
    return request.getRemoteAddr();
  }
}
