package com.fiveelements.calendar.user.controller;

import com.fiveelements.calendar.common.ApiResponse;
import com.fiveelements.calendar.security.SecurityContext;
import com.fiveelements.calendar.user.domain.UserDtos.*;
import com.fiveelements.calendar.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app-api/user")
public class UserController {
  private final UserService service;

  public UserController(UserService service) {
    this.service = service;
  }

  @GetMapping("/profile")
  public ApiResponse<ProfileView> profile() {
    return ApiResponse.ok(service.profile(SecurityContext.currentUser().userId()));
  }

  @PutMapping("/profile")
  public ApiResponse<ProfileView> update(@Valid @RequestBody UpdateProfileRequest request) {
    return ApiResponse.ok(service.update(SecurityContext.currentUser().userId(), request));
  }
}
