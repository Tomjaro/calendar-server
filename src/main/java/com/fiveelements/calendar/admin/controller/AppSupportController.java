package com.fiveelements.calendar.admin.controller;

import com.fiveelements.calendar.admin.service.AppSupportService;
import com.fiveelements.calendar.common.ApiResponse;
import com.fiveelements.calendar.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class AppSupportController {
  private final AppSupportService service;

  public AppSupportController(AppSupportService service) {
    this.service = service;
  }

  @GetMapping("/app-api/public/version/latest")
  public ApiResponse<Map<String, Object>> latest(
      @RequestParam String platform,
      @RequestParam(defaultValue = "OFFICIAL") String channel,
      @RequestParam(defaultValue = "") String deviceId,
      @RequestParam(defaultValue = "0") int currentVersionCode) {
    List<Map<String, Object>> candidates =
        service.versions(platform.toUpperCase(), channel.toUpperCase());
    for (Map<String, Object> candidate : candidates) {
      int percent = ((Number) candidate.get("rolloutPercent")).intValue();
      if (percent >= 100
          || bucket(deviceId, ((Number) candidate.get("id")).longValue()) < percent) {
        var result = new java.util.LinkedHashMap<String, Object>();
        result.put("platform", candidate.get("platform"));
        result.put("channel", candidate.get("channel"));
        result.put("versionName", candidate.get("versionName"));
        result.put("versionCode", candidate.get("versionCode"));
        result.put("downloadUrl", candidate.get("downloadUrl"));
        result.put("releaseNotes", candidate.get("releaseNotes"));
        result.put("forceUpdate", candidate.get("forceUpdate"));
        result.put("rolloutPercent", candidate.get("rolloutPercent"));
        result.put("packageSha256", candidate.get("packageSha256"));
        result.put("minSupportedVersionCode", candidate.get("minSupportedVersionCode"));
        result.put("publishTime", candidate.get("publishTime"));
        if (currentVersionCode < ((Number) candidate.get("minSupportedVersionCode")).intValue())
          result.put("forceUpdate", "1");
        return ApiResponse.ok(result);
      }
    }
    return ApiResponse.ok(Map.of());
  }

  @PostMapping("/app-api/public/feedback")
  public ApiResponse<Long> feedback(
      @Valid @RequestBody FeedbackRequest request,
      @AuthenticationPrincipal UserPrincipal principal) {
    Long userId = principal == null ? null : principal.userId();
    return ApiResponse.ok(service.createFeedback(userId, request.contact(), request.content()));
  }

  @GetMapping("/app-api/feedback")
  public ApiResponse<List<Map<String, Object>>> myFeedback(
      @AuthenticationPrincipal UserPrincipal principal,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size) {
    int limit = Math.min(Math.max(size, 1), 50), offset = Math.max(page - 1, 0) * limit;
    return ApiResponse.ok(service.feedback(principal.userId(), limit, offset));
  }

  public record FeedbackRequest(String contact, @NotBlank String content) {}

  private int bucket(String deviceId, long versionId) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256")
              .digest((deviceId + ":" + versionId).getBytes(StandardCharsets.UTF_8));
      return Byte.toUnsignedInt(digest[0]) % 100;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
