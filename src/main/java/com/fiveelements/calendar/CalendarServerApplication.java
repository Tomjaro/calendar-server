package com.fiveelements.calendar;

import java.util.TimeZone;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({
  "com.fiveelements.calendar.diary.mapper",
  "com.fiveelements.calendar.user.mapper",
  "com.fiveelements.calendar.reminder.mapper",
  "com.fiveelements.calendar.file.mapper",
  "com.fiveelements.calendar.auth.mapper",
  "com.fiveelements.calendar.admin.mapper",
  "com.fiveelements.calendar.region.mapper",
  "com.fiveelements.calendar.mood.mapper",
  "com.fiveelements.calendar.record.mapper"
})
public class CalendarServerApplication {
  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    SpringApplication.run(CalendarServerApplication.class, args);
  }
}
