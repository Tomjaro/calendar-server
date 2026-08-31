package com.fiveelements.calendar.admin;

import static org.assertj.core.api.Assertions.*;

import com.fiveelements.calendar.admin.service.AdminService;
import com.fiveelements.calendar.security.JwtService;
import java.time.LocalDate;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@JdbcTest(properties = "spring.flyway.enabled=false")
class DashboardTrendTest {
  @Autowired DataSource dataSource;
  private JdbcTemplate template;
  private AdminService service;

  @BeforeEach
  void setUp() {
    template = new JdbcTemplate(dataSource);
    template.execute(
        "CREATE TABLE IF NOT EXISTS app_user(id BIGINT,del_flag CHAR(1),create_time TIMESTAMP)");
    template.execute(
        "CREATE TABLE IF NOT EXISTS app_login_log(user_id BIGINT,success_flag CHAR(1),login_time"
            + " TIMESTAMP)");
    template.execute(
        "CREATE TABLE IF NOT EXISTS diary_record(id BIGINT,status CHAR(1),create_time TIMESTAMP)");
    template.update("DELETE FROM app_user");
    template.update("DELETE FROM app_login_log");
    template.update("DELETE FROM diary_record");
    service =
        new AdminService(
            JdbcClient.create(dataSource),
            new BCryptPasswordEncoder(),
            new JwtService("test-secret-that-is-at-least-32-bytes-long", 120),
            "admin",
            "",
            5,
            30);
  }

  @Test
  void returnsContinuousAggregateSeriesWithoutIdentityData() {
    LocalDate today = LocalDate.now();
    template.update("INSERT INTO app_user VALUES (1,'0',?)", today.atTime(8, 0));
    template.update(
        "INSERT INTO app_login_log VALUES (1,'1',?),(1,'1',?)",
        today.atTime(9, 0),
        today.atTime(10, 0));
    template.update(
        "INSERT INTO diary_record VALUES (1,'1',?),(2,'2',?)",
        today.atTime(11, 0),
        today.atTime(12, 0));
    var result = service.trends(7);
    assertThat(result).hasSize(7);
    assertThat(result.get(6))
        .containsEntry("date", today)
        .containsEntry("registrations", 1L)
        .containsEntry("activeUsers", 1L)
        .containsEntry("diaries", 1L);
    assertThat(result.get(0)).containsEntry("registrations", 0L);
    assertThat(result.get(6).keySet())
        .containsExactlyInAnyOrder("date", "registrations", "activeUsers", "diaries");
  }

  @Test
  void clampsRangeToSevenAndNinetyDays() {
    assertThat(service.trends(1)).hasSize(7);
    assertThat(service.trends(365)).hasSize(90);
  }
}
