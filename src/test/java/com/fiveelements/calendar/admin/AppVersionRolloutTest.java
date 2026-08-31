package com.fiveelements.calendar.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fiveelements.calendar.admin.controller.AppSupportController;
import com.fiveelements.calendar.admin.service.AppSupportService;
import java.util.*;
import org.junit.jupiter.api.*;

class AppVersionRolloutTest {
  private AppSupportService service;
  private AppSupportController controller;

  @BeforeEach
  void setUp() {
    service = mock(AppSupportService.class);
    controller = new AppSupportController(service);
  }

  @Test
  void selectsOnlyRequestedChannelAndForcesUnsupportedClients() {
    when(service.versions("ANDROID", "OFFICIAL"))
        .thenReturn(List.of(version(1, 20, "OFFICIAL", 100, 15)));
    var result = controller.latest("ANDROID", "OFFICIAL", "device-a", 10).data();
    assertThat(result.get("versionCode")).isEqualTo(20);
    assertThat(result.get("channel")).isEqualTo("OFFICIAL");
    assertThat(result.get("forceUpdate")).isEqualTo("1");
  }

  @Test
  void rolloutAssignmentIsStableForSameDevice() {
    when(service.versions("ANDROID", "OFFICIAL"))
        .thenReturn(List.of(version(1, 20, "OFFICIAL", 50, 0)));
    var first = controller.latest("ANDROID", "OFFICIAL", "stable-device", 10).data();
    var second = controller.latest("ANDROID", "OFFICIAL", "stable-device", 10).data();
    assertThat(second).isEqualTo(first);
  }

  private Map<String, Object> version(long id, int code, String channel, int rollout, int minimum) {
    Map<String, Object> value = new HashMap<>();
    value.put("id", id);
    value.put("platform", "ANDROID");
    value.put("channel", channel);
    value.put("versionName", "v");
    value.put("versionCode", code);
    value.put("downloadUrl", null);
    value.put("releaseNotes", null);
    value.put("forceUpdate", "0");
    value.put("rolloutPercent", rollout);
    value.put("packageSha256", null);
    value.put("minSupportedVersionCode", minimum);
    value.put("publishTime", null);
    return value;
  }
}
