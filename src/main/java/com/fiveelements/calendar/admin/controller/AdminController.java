package com.fiveelements.calendar.admin.controller;

import com.fiveelements.calendar.admin.domain.AdminPrincipal;
import com.fiveelements.calendar.admin.service.AdminService;
import com.fiveelements.calendar.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin-api")
public class AdminController {
  private final AdminService service;

  public AdminController(AdminService service) {
    this.service = service;
  }

  @PostMapping("/auth/login")
  public ApiResponse<AdminService.LoginResponse> login(
      @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
    return ApiResponse.ok(
        service.login(request.username(), request.password(), httpRequest.getRemoteAddr()));
  }

  @PutMapping("/auth/password")
  public ApiResponse<Void> changePassword(
      @Valid @RequestBody ChangePasswordRequest request,
      @AuthenticationPrincipal AdminPrincipal admin,
      HttpServletRequest httpRequest) {
    service.changePassword(
        admin.id(), request.currentPassword(), request.newPassword(), httpRequest.getRemoteAddr());
    return ApiResponse.ok(null);
  }

  @PutMapping("/admins/{id}/password")
  public ApiResponse<Void> resetPassword(
      @PathVariable long id,
      @Valid @RequestBody ResetPasswordRequest request,
      @AuthenticationPrincipal AdminPrincipal admin,
      HttpServletRequest httpRequest) {
    service.resetPassword(admin.id(), id, request.newPassword(), httpRequest.getRemoteAddr());
    return ApiResponse.ok(null);
  }

  @GetMapping("/admins")
  public ApiResponse<List<Map<String, Object>>> admins() {
    return ApiResponse.ok(service.admins());
  }

  @PostMapping("/admins")
  public ApiResponse<Long> createAdmin(
      @RequestBody AdminService.AdminAccountRequest body,
      @AuthenticationPrincipal AdminPrincipal admin,
      HttpServletRequest request) {
    return ApiResponse.ok(service.createAdmin(admin.id(), body, request.getRemoteAddr()));
  }

  @PutMapping("/admins/{id}")
  public ApiResponse<Void> updateAdmin(
      @PathVariable long id,
      @RequestBody AdminService.AdminAccountRequest body,
      @AuthenticationPrincipal AdminPrincipal admin,
      HttpServletRequest request) {
    service.updateAdmin(admin.id(), id, body, request.getRemoteAddr());
    return ApiResponse.ok(null);
  }

  @GetMapping("/dashboard/overview")
  public ApiResponse<Map<String, Object>> overview() {
    return ApiResponse.ok(service.overview());
  }

  @GetMapping("/dashboard/trends")
  public ApiResponse<List<Map<String, Object>>> trends(
      @RequestParam(defaultValue = "30") int days) {
    return ApiResponse.ok(service.trends(days));
  }

  @GetMapping("/users")
  public ApiResponse<List<Map<String, Object>>> users(
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.users(keyword, page, size));
  }

  @PutMapping("/users/{id}/freeze")
  public ApiResponse<Void> freeze(
      @PathVariable long id,
      @AuthenticationPrincipal AdminPrincipal admin,
      HttpServletRequest request) {
    service.setFrozen(admin.id(), id, true, request.getRemoteAddr());
    return ApiResponse.ok(null);
  }

  @PutMapping("/users/{id}/unfreeze")
  public ApiResponse<Void> unfreeze(
      @PathVariable long id,
      @AuthenticationPrincipal AdminPrincipal admin,
      HttpServletRequest request) {
    service.setFrozen(admin.id(), id, false, request.getRemoteAddr());
    return ApiResponse.ok(null);
  }

  @PostMapping("/users/{id}/force-logout")
  public ApiResponse<Void> forceLogout(
      @PathVariable long id,
      @AuthenticationPrincipal AdminPrincipal admin,
      HttpServletRequest request) {
    service.forceLogout(admin.id(), id, request.getRemoteAddr());
    return ApiResponse.ok(null);
  }

  @GetMapping("/operation-logs")
  public ApiResponse<List<Map<String, Object>>> logs(
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.operationLogs(keyword, page, size));
  }

  @GetMapping("/login-logs")
  public ApiResponse<List<Map<String, Object>>> loginLogs(
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam(defaultValue = "") String result,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.loginLogs(keyword, result, page, size));
  }

  @GetMapping("/versions")
  public ApiResponse<List<Map<String, Object>>> versions() {
    return ApiResponse.ok(service.versions());
  }

  @PostMapping("/versions")
  public ApiResponse<Long> saveVersion(
      @RequestBody AdminService.VersionRequest body,
      @AuthenticationPrincipal AdminPrincipal admin,
      HttpServletRequest request) {
    return ApiResponse.ok(service.saveVersion(admin.id(), body, request.getRemoteAddr()));
  }

  @PutMapping("/versions/{id}")
  public ApiResponse<Void> updateVersion(
      @PathVariable long id,
      @RequestBody AdminService.VersionRequest body,
      @AuthenticationPrincipal AdminPrincipal admin,
      HttpServletRequest request) {
    service.updateVersion(admin.id(), id, body, request.getRemoteAddr());
    return ApiResponse.ok(null);
  }

  @PutMapping("/versions/{id}/unpublish")
  public ApiResponse<Void> unpublishVersion(
      @PathVariable long id,
      @AuthenticationPrincipal AdminPrincipal admin,
      HttpServletRequest request) {
    service.unpublishVersion(admin.id(), id, request.getRemoteAddr());
    return ApiResponse.ok(null);
  }

  @GetMapping("/feedback")
  public ApiResponse<List<Map<String, Object>>> feedback(
      @RequestParam(defaultValue = "") String status,
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(service.feedback(status, keyword, page, size));
  }

  @PutMapping("/feedback/{id}")
  public ApiResponse<Void> reply(
      @PathVariable long id,
      @RequestBody FeedbackReply body,
      @AuthenticationPrincipal AdminPrincipal admin,
      HttpServletRequest request) {
    service.replyFeedback(admin.id(), id, body.status(), body.reply(), request.getRemoteAddr());
    return ApiResponse.ok(null);
  }

  @GetMapping("/roles")
  public ApiResponse<List<Map<String, Object>>> roles() {
    return ApiResponse.ok(service.roles());
  }

  @GetMapping("/permissions")
  public ApiResponse<List<Map<String, Object>>> permissions() {
    return ApiResponse.ok(service.permissions());
  }

  @PostMapping("/roles")
  public ApiResponse<Long> createRole(
      @RequestBody AdminService.RoleRequest body,
      @AuthenticationPrincipal AdminPrincipal admin,
      HttpServletRequest request) {
    return ApiResponse.ok(service.createRole(admin.id(), body, request.getRemoteAddr()));
  }

  @PutMapping("/roles/{id}")
  public ApiResponse<Void> updateRole(
      @PathVariable long id,
      @RequestBody AdminService.RoleRequest body,
      @AuthenticationPrincipal AdminPrincipal admin,
      HttpServletRequest request) {
    service.updateRole(admin.id(), id, body, request.getRemoteAddr());
    return ApiResponse.ok(null);
  }

  @GetMapping("/page-summary")
  public ApiResponse<Map<String, Object>> pageSummary(
      @RequestParam String entity,
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam(defaultValue = "") String status,
      @RequestParam(defaultValue = "") String result) {
    return ApiResponse.ok(service.pageSummary(entity, keyword, status, result));
  }

  public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

  public record ChangePasswordRequest(
      @NotBlank String currentPassword, @NotBlank String newPassword) {}

  public record ResetPasswordRequest(@NotBlank String newPassword) {}

  public record FeedbackReply(@NotBlank String status, String reply) {}
}
