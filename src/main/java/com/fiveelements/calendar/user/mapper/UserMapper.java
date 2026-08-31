package com.fiveelements.calendar.user.mapper;

import com.fiveelements.calendar.user.domain.UserDtos.*;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
  UserRow selectProfile(@Param("id") long id);

  int updateProfile(
      @Param("id") long id,
      @Param("request") UpdateProfileRequest request,
      @Param("gender") String gender);

  void insertProfileLog(@Param("id") long id);

  List<DeviceView> selectDevices(@Param("userId") long userId);

  int disableDevice(@Param("userId") long userId, @Param("deviceId") String deviceId);
}
