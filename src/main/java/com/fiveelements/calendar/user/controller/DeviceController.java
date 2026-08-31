package com.fiveelements.calendar.user.controller;

import com.fiveelements.calendar.common.ApiResponse;
import com.fiveelements.calendar.security.SecurityContext;
import com.fiveelements.calendar.user.domain.UserDtos.DeviceView;
import com.fiveelements.calendar.user.service.UserService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app-api/user/devices")
public class DeviceController {
  private final UserService service;

  public DeviceController(UserService service) {
    this.service = service;
  }

  @GetMapping
  public ApiResponse<List<DeviceView>> list() {
    long uid = SecurityContext.currentUser().userId();
    return ApiResponse.ok(service.devices(uid));
  }

  @DeleteMapping("/{deviceId}")
  public ApiResponse<Void> remove(@PathVariable String deviceId) {
    long uid = SecurityContext.currentUser().userId();
    service.removeDevice(uid, deviceId);
    return ApiResponse.ok(null);
  }
}
