package com.fiveelements.calendar.user.service;

import com.fiveelements.calendar.user.domain.UserDtos.*;
import com.fiveelements.calendar.user.mapper.UserMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserMapper mapper;

  public UserService(UserMapper mapper) {
    this.mapper = mapper;
  }

  public ProfileView profile(long userId) {
    UserRow row = mapper.selectProfile(userId);
    if (row == null) throw new IllegalArgumentException("用户不存在");
    return new ProfileView(
        row.id(),
        mask(row.phone()),
        row.nickname(),
        row.avatarUrl(),
        row.gender(),
        row.birthday(),
        row.region(),
        row.signature(),
        row.createTime());
  }

  public ProfileView update(long userId, UpdateProfileRequest request) {
    mapper.updateProfile(userId, request, emptyToNull(request.gender()));
    mapper.insertProfileLog(userId);
    return profile(userId);
  }

  public List<DeviceView> devices(long userId) {
    return mapper.selectDevices(userId);
  }

  public void removeDevice(long userId, String deviceId) {
    if (mapper.disableDevice(userId, deviceId) == 0) throw new IllegalArgumentException("设备不存在");
  }

  private String mask(String phone) {
    return phone.substring(0, 3) + "****" + phone.substring(7);
  }

  private String emptyToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
