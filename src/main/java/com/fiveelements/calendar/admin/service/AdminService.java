package com.fiveelements.calendar.admin.service;

import com.fiveelements.calendar.security.JwtService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService implements ApplicationRunner {
  private final JdbcClient jdbc;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  private final String initialUsername;
  private final String initialPassword;
  private final int maxFailedAttempts;
  private final long lockMinutes;
  private static final Pattern STRONG_PASSWORD =
      Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,72}$");

  public AdminService(
      JdbcClient jdbc,
      PasswordEncoder encoder,
      JwtService jwt,
      @Value("${app.admin.initial-username:admin}") String initialUsername,
      @Value("${app.admin.initial-password:}") String initialPassword,
      @Value("${app.admin.login.max-failed-attempts:5}") int maxFailedAttempts,
      @Value("${app.admin.login.lock-minutes:30}") long lockMinutes) {
    this.jdbc = jdbc;
    this.encoder = encoder;
    this.jwt = jwt;
    this.initialUsername = initialUsername;
    this.initialPassword = initialPassword;
    this.maxFailedAttempts = Math.max(maxFailedAttempts, 1);
    this.lockMinutes = Math.max(lockMinutes, 1);
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (initialPassword == null || initialPassword.isBlank()) return;
    Integer count = jdbc.sql("SELECT COUNT(*) FROM sys_admin_user").query(Integer.class).single();
    if (count > 0) return;
    jdbc.sql(
            "INSERT INTO sys_admin_user(username,password,display_name) VALUES"
                + " (:u,:p,'Administrator')")
        .param("u", initialUsername)
        .param("p", encoder.encode(initialPassword))
        .update();
    Long id = jdbc.sql("SELECT LAST_INSERT_ID()").query(Long.class).single();
    jdbc.sql(
            "INSERT INTO sys_admin_user_role(admin_user_id,role_id) SELECT :id,id FROM sys_role"
                + " WHERE role_code='SUPER_ADMIN'")
        .param("id", id)
        .update();
  }

  @Transactional
  public LoginResponse login(String username, String password, String ip) {
    AdminRow row =
        jdbc.sql(
                """
SELECT a.id,a.username,a.password,a.display_name,a.status,a.failed_login_attempts,a.locked_until,r.role_code role,
       GROUP_CONCAT(DISTINCT p.permission_code ORDER BY p.permission_code) permissions
FROM sys_admin_user a JOIN sys_admin_user_role ar ON ar.admin_user_id=a.id
JOIN sys_role r ON r.id=ar.role_id LEFT JOIN sys_role_permission rp ON rp.role_id=r.id
LEFT JOIN sys_permission p ON p.id=rp.permission_id WHERE a.username=:u
GROUP BY a.id,a.username,a.password,a.display_name,a.status,a.failed_login_attempts,a.locked_until,r.role_code""")
            .param("u", username)
            .query(AdminRow.class)
            .optional()
            .orElse(null);
    if (row == null) {
      recordLogin(null, username, ip, false, "BAD_CREDENTIALS");
      throw new BadCredentialsException("Username or password is incorrect");
    }
    if (!"0".equals(row.status())) {
      recordLogin(row.id(), username, ip, false, "DISABLED");
      throw new BadCredentialsException("Username or password is incorrect");
    }
    if (row.lockedUntil() != null && row.lockedUntil().isAfter(LocalDateTime.now())) {
      recordLogin(row.id(), username, ip, false, "LOCKED");
      throw new BadCredentialsException("Account is temporarily locked");
    }
    if (!encoder.matches(password, row.password())) {
      int attempts = row.failedLoginAttempts() + 1;
      LocalDateTime lockedUntil =
          attempts >= maxFailedAttempts ? LocalDateTime.now().plusMinutes(lockMinutes) : null;
      jdbc.sql(
              "UPDATE sys_admin_user SET failed_login_attempts=:attempts,locked_until=:locked WHERE"
                  + " id=:id")
          .param("attempts", attempts)
          .param("locked", lockedUntil)
          .param("id", row.id())
          .update();
      recordLogin(
          row.id(), username, ip, false, lockedUntil == null ? "BAD_CREDENTIALS" : "LOCKED");
      throw new BadCredentialsException("Username or password is incorrect");
    }
    jdbc.sql(
            "UPDATE sys_admin_user SET"
                + " last_login_time=NOW(),failed_login_attempts=0,locked_until=NULL WHERE id=:id")
        .param("id", row.id())
        .update();
    recordLogin(row.id(), username, ip, true, null);
    List<String> permissions =
        row.permissions() == null ? List.of() : List.of(row.permissions().split(","));
    return new LoginResponse(
        jwt.createAdminToken(row.id(), row.username(), row.role(), permissions),
        row.displayName(),
        row.role(),
        permissions,
        7200);
  }

  @Transactional
  public void changePassword(long adminId, String currentPassword, String newPassword, String ip) {
    String encoded =
        jdbc.sql("SELECT password FROM sys_admin_user WHERE id=:id AND status='0'")
            .param("id", adminId)
            .query(String.class)
            .optional()
            .orElseThrow(() -> new IllegalArgumentException("Admin account does not exist"));
    if (!encoder.matches(currentPassword, encoded))
      throw new BadCredentialsException("Current password is incorrect");
    validatePassword(newPassword);
    if (encoder.matches(newPassword, encoded))
      throw new IllegalArgumentException("New password must differ from current password");
    jdbc.sql(
            "UPDATE sys_admin_user SET"
                + " password=:password,password_changed_at=NOW(),failed_login_attempts=0,locked_until=NULL"
                + " WHERE id=:id")
        .param("password", encoder.encode(newPassword))
        .param("id", adminId)
        .update();
    audit(adminId, "CHANGE_OWN_PASSWORD", "ADMIN", String.valueOf(adminId), ip);
  }

  @Transactional
  public void resetPassword(long operatorId, long targetId, String newPassword, String ip) {
    validatePassword(newPassword);
    int changed =
        jdbc.sql(
                "UPDATE sys_admin_user SET"
                    + " password=:password,password_changed_at=NOW(),failed_login_attempts=0,locked_until=NULL"
                    + " WHERE id=:id")
            .param("password", encoder.encode(newPassword))
            .param("id", targetId)
            .update();
    if (changed == 0) throw new IllegalArgumentException("Admin account does not exist");
    audit(operatorId, "RESET_ADMIN_PASSWORD", "ADMIN", String.valueOf(targetId), ip);
  }

  public Map<String, Object> overview() {
    return Map.of(
        "totalUsers",
        scalar("SELECT COUNT(*) FROM app_user WHERE del_flag='0'"),
        "newUsersToday",
        scalar("SELECT COUNT(*) FROM app_user WHERE del_flag='0' AND create_time>=CURRENT_DATE"),
        "activeUsersToday",
        scalar("SELECT COUNT(*) FROM app_user WHERE last_login_time>=CURRENT_DATE"),
        "diariesToday",
        scalar(
            "SELECT COUNT(*) FROM diary_record WHERE create_time>=CURRENT_DATE AND"
                + " status<>'2'"),
        "pendingFeedback",
        scalar("SELECT COUNT(*) FROM user_feedback WHERE status='PENDING'"));
  }

  public List<Map<String, Object>> trends(int requestedDays) {
    int days = Math.min(Math.max(requestedDays, 7), 90);
    LocalDate start = LocalDate.now().minusDays(days - 1L);
    Map<LocalDate, Long> registrations =
        dailyCounts(
            "SELECT CAST(create_time AS DATE) stat_date,COUNT(*) total FROM app_user WHERE"
                + " del_flag='0' AND create_time>=:start GROUP BY CAST(create_time AS DATE)",
            start);
    Map<LocalDate, Long> activeUsers =
        dailyCounts(
            "SELECT CAST(login_time AS DATE) stat_date,COUNT(DISTINCT user_id) total FROM"
                + " app_login_log WHERE success_flag='1' AND user_id IS NOT NULL AND"
                + " login_time>=:start GROUP BY CAST(login_time AS DATE)",
            start);
    Map<LocalDate, Long> diaries =
        dailyCounts(
            "SELECT CAST(create_time AS DATE) stat_date,COUNT(*) total FROM diary_record WHERE"
                + " status<>'2' AND create_time>=:start GROUP BY CAST(create_time AS DATE)",
            start);
    List<Map<String, Object>> result = new ArrayList<>();
    for (int i = 0; i < days; i++) {
      LocalDate day = start.plusDays(i);
      result.add(
          Map.of(
              "date",
              day,
              "registrations",
              registrations.getOrDefault(day, 0L),
              "activeUsers",
              activeUsers.getOrDefault(day, 0L),
              "diaries",
              diaries.getOrDefault(day, 0L)));
    }
    return result;
  }

  private Map<LocalDate, Long> dailyCounts(String sql, LocalDate start) {
    Map<LocalDate, Long> result = new HashMap<>();
    for (var row :
        jdbc.sql(sql).param("start", start.atStartOfDay()).query(DailyCount.class).list())
      result.put(row.statDate(), row.total());
    return result;
  }

  public List<Map<String, Object>> users(String keyword, int page, int size) {
    String value = keyword == null ? "" : keyword.trim();
    return jdbc.sql(
            """
SELECT u.id,u.phone,u.nickname,u.status,u.cancel_status cancelStatus,u.create_time createTime,
       u.last_login_time lastLoginTime,COUNT(d.id) diaryCount
FROM app_user u LEFT JOIN diary_record d ON d.user_id=u.id AND d.status<>'2'
WHERE u.del_flag='0' AND (:keyword='' OR u.phone LIKE CONCAT('%',:keyword,'%') OR u.nickname LIKE CONCAT('%',:keyword,'%'))
GROUP BY u.id,u.phone,u.nickname,u.status,u.cancel_status,u.create_time,u.last_login_time
ORDER BY u.id DESC LIMIT :limit OFFSET :offset""")
        .param("keyword", value)
        .param("limit", Math.min(Math.max(size, 1), 100))
        .param("offset", Math.max(page - 1, 0) * Math.min(Math.max(size, 1), 100))
        .query()
        .listOfRows();
  }

  @Transactional
  public void setFrozen(long adminId, long userId, boolean frozen, String ip) {
    int changed =
        jdbc.sql("UPDATE app_user SET status=:status WHERE id=:id AND del_flag='0'")
            .param("status", frozen ? "1" : "0")
            .param("id", userId)
            .update();
    if (changed == 0) throw new IllegalArgumentException("User does not exist");
    if (frozen)
      jdbc.sql(
              "UPDATE auth_refresh_token SET revoked_at=NOW() WHERE user_id=:id AND revoked_at IS"
                  + " NULL")
          .param("id", userId)
          .update();
    audit(adminId, frozen ? "FREEZE_USER" : "UNFREEZE_USER", "USER", String.valueOf(userId), ip);
  }

  public void forceLogout(long adminId, long userId, String ip) {
    jdbc.sql(
            "UPDATE auth_refresh_token SET revoked_at=NOW() WHERE user_id=:id AND revoked_at IS"
                + " NULL")
        .param("id", userId)
        .update();
    audit(adminId, "FORCE_LOGOUT", "USER", String.valueOf(userId), ip);
  }

  public List<Map<String, Object>> operationLogs(String keyword, int page, int size) {
    return jdbc.sql(
            """
SELECT l.id,a.username,l.operation_code operationCode,l.object_type objectType,l.object_id objectId,
       l.ip_address ipAddress,l.success_flag successFlag,l.operation_time operationTime
FROM admin_operation_log l JOIN sys_admin_user a ON a.id=l.admin_user_id
WHERE (:keyword='' OR a.username LIKE CONCAT('%',:keyword,'%') OR l.operation_code LIKE CONCAT('%',:keyword,'%') OR l.ip_address LIKE CONCAT('%',:keyword,'%'))
ORDER BY l.id DESC LIMIT :limit OFFSET :offset""")
        .param("keyword", clean(keyword))
        .param("limit", limit(size))
        .param("offset", offset(page, size))
        .query()
        .listOfRows();
  }

  public List<Map<String, Object>> loginLogs(String keyword, String result, int page, int size) {
    return jdbc.sql(
            "SELECT id,user_id userId,phone,login_type loginType,ip_address ipAddress,device_id"
                + " deviceId,platform,success_flag successFlag,failure_reason"
                + " failureReason,login_time loginTime FROM app_login_log WHERE (:keyword='' OR"
                + " phone LIKE CONCAT('%',:keyword,'%') OR ip_address LIKE CONCAT('%',:keyword,'%')"
                + " OR platform LIKE CONCAT('%',:keyword,'%')) AND (:result='' OR"
                + " success_flag=:result) ORDER BY id DESC LIMIT :limit OFFSET :offset")
        .param("keyword", clean(keyword))
        .param("result", clean(result))
        .param("limit", limit(size))
        .param("offset", offset(page, size))
        .query()
        .listOfRows();
  }

  public List<Map<String, Object>> versions() {
    return jdbc.sql(
            "SELECT id,platform,channel,version_name versionName,version_code"
                + " versionCode,download_url downloadUrl,release_notes releaseNotes,force_update"
                + " forceUpdate,rollout_percent rolloutPercent,package_sha256"
                + " packageSha256,min_supported_version_code"
                + " minSupportedVersionCode,status,publish_time publishTime,create_time createTime"
                + " FROM app_version ORDER BY version_code DESC")
        .query()
        .listOfRows();
  }

  @Transactional
  public long saveVersion(long adminId, VersionRequest r, String ip) {
    validateVersion(r);
    jdbc.sql(
            "INSERT INTO"
                + " app_version(platform,channel,version_name,version_code,download_url,release_notes,force_update,rollout_percent,package_sha256,min_supported_version_code,status,publish_time)"
                + " VALUES"
                + " (:platform,:channel,:name,:code,:url,:notes,:force,:rollout,:sha,:minimum,:status,:publish)")
        .param("platform", r.platform().toUpperCase())
        .param("channel", r.channel().toUpperCase())
        .param("name", r.versionName())
        .param("code", r.versionCode())
        .param("url", r.downloadUrl())
        .param("notes", r.releaseNotes())
        .param("force", r.forceUpdate() ? "1" : "0")
        .param("rollout", r.rolloutPercent())
        .param("sha", clean(r.packageSha256()).toLowerCase())
        .param("minimum", r.minSupportedVersionCode())
        .param("status", r.published() ? "1" : "0")
        .param("publish", r.published() ? LocalDateTime.now() : null)
        .update();
    long id = jdbc.sql("SELECT LAST_INSERT_ID()").query(Long.class).single();
    audit(adminId, "CREATE_VERSION", "VERSION", String.valueOf(id), ip);
    return id;
  }

  @Transactional
  public void updateVersion(long adminId, long id, VersionRequest r, String ip) {
    validateVersion(r);
    int changed =
        jdbc.sql(
                "UPDATE app_version SET"
                    + " platform=:platform,channel=:channel,version_name=:name,version_code=:code,download_url=:url,release_notes=:notes,force_update=:force,rollout_percent=:rollout,package_sha256=:sha,min_supported_version_code=:minimum,status=:status,publish_time=CASE"
                    + " WHEN :status='1' THEN COALESCE(publish_time,NOW()) ELSE NULL END WHERE"
                    + " id=:id")
            .param("platform", r.platform().toUpperCase())
            .param("channel", r.channel().toUpperCase())
            .param("name", r.versionName())
            .param("code", r.versionCode())
            .param("url", r.downloadUrl())
            .param("notes", r.releaseNotes())
            .param("force", r.forceUpdate() ? "1" : "0")
            .param("rollout", r.rolloutPercent())
            .param("sha", clean(r.packageSha256()).toLowerCase())
            .param("minimum", r.minSupportedVersionCode())
            .param("status", r.published() ? "1" : "0")
            .param("id", id)
            .update();
    if (changed == 0) throw new IllegalArgumentException("Version does not exist");
    audit(adminId, "UPDATE_VERSION", "VERSION", String.valueOf(id), ip);
  }

  @Transactional
  public void unpublishVersion(long adminId, long id, String ip) {
    if (jdbc.sql("UPDATE app_version SET status='0',publish_time=NULL WHERE id=:id")
            .param("id", id)
            .update()
        == 0) throw new IllegalArgumentException("Version does not exist");
    audit(adminId, "UNPUBLISH_VERSION", "VERSION", String.valueOf(id), ip);
  }

  public Map<String, Object> pageSummary(
      String entity, String keyword, String status, String result) {
    String key = clean(keyword), state = clean(status), outcome = clean(result);
    long total =
        switch (entity) {
          case "users" ->
              jdbc.sql(
                      "SELECT COUNT(*) FROM app_user WHERE del_flag='0' AND (:keyword='' OR phone"
                          + " LIKE CONCAT('%',:keyword,'%') OR nickname LIKE"
                          + " CONCAT('%',:keyword,'%'))")
                  .param("keyword", key)
                  .query(Long.class)
                  .single();
          case "login-logs" ->
              jdbc.sql(
                      "SELECT COUNT(*) FROM app_login_log WHERE (:keyword='' OR phone LIKE"
                          + " CONCAT('%',:keyword,'%') OR ip_address LIKE CONCAT('%',:keyword,'%')"
                          + " OR platform LIKE CONCAT('%',:keyword,'%')) AND (:result='' OR"
                          + " success_flag=:result)")
                  .param("keyword", key)
                  .param("result", outcome)
                  .query(Long.class)
                  .single();
          case "operation-logs" ->
              jdbc.sql(
                      "SELECT COUNT(*) FROM admin_operation_log l JOIN sys_admin_user a ON"
                          + " a.id=l.admin_user_id WHERE (:keyword='' OR a.username LIKE"
                          + " CONCAT('%',:keyword,'%') OR l.operation_code LIKE"
                          + " CONCAT('%',:keyword,'%') OR l.ip_address LIKE"
                          + " CONCAT('%',:keyword,'%'))")
                  .param("keyword", key)
                  .query(Long.class)
                  .single();
          case "feedback" ->
              jdbc.sql(
                      "SELECT COUNT(*) FROM user_feedback f LEFT JOIN app_user u ON u.id=f.user_id"
                          + " WHERE (:status='' OR f.status=:status) AND (:keyword='' OR u.phone"
                          + " LIKE CONCAT('%',:keyword,'%') OR f.contact LIKE"
                          + " CONCAT('%',:keyword,'%') OR f.content LIKE CONCAT('%',:keyword,'%'))")
                  .param("status", state)
                  .param("keyword", key)
                  .query(Long.class)
                  .single();
          default -> throw new IllegalArgumentException("Unsupported entity");
        };
    return Map.of("total", total);
  }

  public List<Map<String, Object>> feedback(String status, String keyword, int page, int size) {
    String value = status == null ? "" : status.trim();
    return jdbc.sql(
            "SELECT f.id,f.user_id userId,u.phone,f.contact,f.content,f.status,f.admin_reply"
                + " adminReply,f.create_time createTime,f.update_time updateTime FROM user_feedback"
                + " f LEFT JOIN app_user u ON u.id=f.user_id WHERE (:status='' OR f.status=:status)"
                + " AND (:keyword='' OR u.phone LIKE CONCAT('%',:keyword,'%') OR f.contact LIKE"
                + " CONCAT('%',:keyword,'%') OR f.content LIKE CONCAT('%',:keyword,'%')) ORDER BY"
                + " f.id DESC LIMIT :limit OFFSET :offset")
        .param("status", value)
        .param("keyword", clean(keyword))
        .param("limit", limit(size))
        .param("offset", offset(page, size))
        .query()
        .listOfRows();
  }

  @Transactional
  public void replyFeedback(long adminId, long id, String status, String reply, String ip) {
    if (jdbc.sql("UPDATE user_feedback SET status=:status,admin_reply=:reply WHERE id=:id")
            .param("status", status)
            .param("reply", reply)
            .param("id", id)
            .update()
        == 0) throw new IllegalArgumentException("Feedback does not exist");
    audit(adminId, "REPLY_FEEDBACK", "FEEDBACK", String.valueOf(id), ip);
  }

  public List<Map<String, Object>> roles() {
    return jdbc.sql(
            "SELECT r.id,r.role_code roleCode,r.role_name"
                + " roleName,r.status,GROUP_CONCAT(p.permission_code ORDER BY p.permission_code)"
                + " permissions FROM sys_role r LEFT JOIN sys_role_permission rp ON rp.role_id=r.id"
                + " LEFT JOIN sys_permission p ON p.id=rp.permission_id GROUP BY"
                + " r.id,r.role_code,r.role_name,r.status ORDER BY r.id")
        .query()
        .listOfRows();
  }

  public List<Map<String, Object>> permissions() {
    return jdbc.sql(
            "SELECT id,permission_code permissionCode,permission_name permissionName FROM"
                + " sys_permission ORDER BY permission_code")
        .query()
        .listOfRows();
  }

  public List<Map<String, Object>> admins() {
    return jdbc.sql(
            "SELECT a.id,a.username,a.display_name displayName,a.status,a.failed_login_attempts"
                + " failedLoginAttempts,a.locked_until lockedUntil,a.last_login_time"
                + " lastLoginTime,a.create_time createTime,r.id roleId,r.role_code"
                + " roleCode,r.role_name roleName FROM sys_admin_user a LEFT JOIN"
                + " sys_admin_user_role ar ON ar.admin_user_id=a.id LEFT JOIN sys_role r ON"
                + " r.id=ar.role_id ORDER BY a.id")
        .query()
        .listOfRows();
  }

  @Transactional
  public long createAdmin(long operatorId, AdminAccountRequest r, String ip) {
    validatePassword(r.password());
    if (clean(r.username()).isEmpty() || clean(r.displayName()).isEmpty())
      throw new IllegalArgumentException("Username and display name are required");
    ensureRole(r.roleId());
    jdbc.sql(
            "INSERT INTO sys_admin_user(username,password,display_name,status,password_changed_at)"
                + " VALUES (:username,:password,:name,:status,NOW())")
        .param("username", clean(r.username()))
        .param("password", encoder.encode(r.password()))
        .param("name", clean(r.displayName()))
        .param("status", r.enabled() ? "0" : "1")
        .update();
    long id = jdbc.sql("SELECT LAST_INSERT_ID()").query(Long.class).single();
    jdbc.sql("INSERT INTO sys_admin_user_role(admin_user_id,role_id) VALUES (:admin,:role)")
        .param("admin", id)
        .param("role", r.roleId())
        .update();
    audit(operatorId, "CREATE_ADMIN", "ADMIN", String.valueOf(id), ip);
    return id;
  }

  @Transactional
  public void updateAdmin(long operatorId, long id, AdminAccountRequest r, String ip) {
    if (id == operatorId && !r.enabled())
      throw new IllegalArgumentException("You cannot disable your own account");
    ensureRole(r.roleId());
    protectLastSuperAdmin(id, r.roleId(), r.enabled());
    if (jdbc.sql(
                "UPDATE sys_admin_user SET"
                    + " display_name=:name,status=:status,failed_login_attempts=CASE WHEN"
                    + " :status='0' THEN 0 ELSE failed_login_attempts END,locked_until=CASE WHEN"
                    + " :status='0' THEN NULL ELSE locked_until END WHERE id=:id")
            .param("name", clean(r.displayName()))
            .param("status", r.enabled() ? "0" : "1")
            .param("id", id)
            .update()
        == 0) throw new IllegalArgumentException("Admin account does not exist");
    jdbc.sql("DELETE FROM sys_admin_user_role WHERE admin_user_id=:id").param("id", id).update();
    jdbc.sql("INSERT INTO sys_admin_user_role(admin_user_id,role_id) VALUES (:admin,:role)")
        .param("admin", id)
        .param("role", r.roleId())
        .update();
    audit(operatorId, "UPDATE_ADMIN", "ADMIN", String.valueOf(id), ip);
  }

  @Transactional
  public long createRole(long operatorId, RoleRequest r, String ip) {
    String code = clean(r.roleCode()).toUpperCase();
    if (!code.matches("[A-Z][A-Z0-9_]{2,63}"))
      throw new IllegalArgumentException("Invalid role code");
    jdbc.sql("INSERT INTO sys_role(role_code,role_name,status) VALUES (:code,:name,'0')")
        .param("code", code)
        .param("name", clean(r.roleName()))
        .update();
    long id = jdbc.sql("SELECT LAST_INSERT_ID()").query(Long.class).single();
    replacePermissions(id, r.permissionIds());
    audit(operatorId, "CREATE_ROLE", "ROLE", String.valueOf(id), ip);
    return id;
  }

  @Transactional
  public void updateRole(long operatorId, long id, RoleRequest r, String ip) {
    String code =
        jdbc.sql("SELECT role_code FROM sys_role WHERE id=:id")
            .param("id", id)
            .query(String.class)
            .optional()
            .orElseThrow(() -> new IllegalArgumentException("Role does not exist"));
    if ("SUPER_ADMIN".equals(code))
      throw new IllegalArgumentException("The built-in super administrator role cannot be edited");
    jdbc.sql("UPDATE sys_role SET role_name=:name,status=:status WHERE id=:id")
        .param("name", clean(r.roleName()))
        .param("status", r.enabled() ? "0" : "1")
        .param("id", id)
        .update();
    replacePermissions(id, r.permissionIds());
    audit(operatorId, "UPDATE_ROLE", "ROLE", String.valueOf(id), ip);
  }

  private void replacePermissions(long roleId, List<Long> ids) {
    jdbc.sql("DELETE FROM sys_role_permission WHERE role_id=:id").param("id", roleId).update();
    if (ids == null) return;
    for (Long permissionId : ids) {
      jdbc.sql(
              "INSERT INTO sys_role_permission(role_id,permission_id) SELECT :role,id FROM"
                  + " sys_permission WHERE id=:permission")
          .param("role", roleId)
          .param("permission", permissionId)
          .update();
    }
  }

  private void ensureRole(long roleId) {
    if (jdbc.sql("SELECT COUNT(*) FROM sys_role WHERE id=:id AND status='0'")
            .param("id", roleId)
            .query(Integer.class)
            .single()
        == 0) throw new IllegalArgumentException("Role does not exist or is disabled");
  }

  private void protectLastSuperAdmin(long adminId, long newRoleId, boolean enabled) {
    Integer isSuper =
        jdbc.sql(
                "SELECT COUNT(*) FROM sys_admin_user_role ar JOIN sys_role r ON r.id=ar.role_id"
                    + " WHERE ar.admin_user_id=:id AND r.role_code='SUPER_ADMIN'")
            .param("id", adminId)
            .query(Integer.class)
            .single();
    Integer newSuper =
        jdbc.sql("SELECT COUNT(*) FROM sys_role WHERE id=:id AND role_code='SUPER_ADMIN'")
            .param("id", newRoleId)
            .query(Integer.class)
            .single();
    if (isSuper > 0
        && (!enabled || newSuper == 0)
        && jdbc.sql(
                    "SELECT COUNT(*) FROM sys_admin_user a JOIN sys_admin_user_role ar ON"
                        + " ar.admin_user_id=a.id JOIN sys_role r ON r.id=ar.role_id WHERE"
                        + " a.status='0' AND r.role_code='SUPER_ADMIN'")
                .query(Integer.class)
                .single()
            <= 1)
      throw new IllegalArgumentException("At least one enabled super administrator is required");
  }

  private int limit(int size) {
    return Math.min(Math.max(size, 1), 100);
  }

  private int offset(int page, int size) {
    return Math.max(page - 1, 0) * limit(size);
  }

  private String clean(String value) {
    return value == null ? "" : value.trim();
  }

  private long scalar(String sql) {
    return jdbc.sql(sql).query(Long.class).single();
  }

  private void audit(long adminId, String code, String type, String objectId, String ip) {
    jdbc.sql(
            "INSERT INTO"
                + " admin_operation_log(admin_user_id,operation_code,object_type,object_id,ip_address)"
                + " VALUES (:a,:c,:t,:o,:ip)")
        .param("a", adminId)
        .param("c", code)
        .param("t", type)
        .param("o", objectId)
        .param("ip", ip)
        .update();
  }

  private void recordLogin(
      Long adminId, String username, String ip, boolean success, String reason) {
    jdbc.sql(
            "INSERT INTO"
                + " admin_login_log(admin_user_id,username,ip_address,success_flag,failure_reason)"
                + " VALUES (:id,:username,:ip,:success,:reason)")
        .param("id", adminId)
        .param("username", username)
        .param("ip", ip)
        .param("success", success ? "1" : "0")
        .param("reason", reason)
        .update();
  }

  private void validatePassword(String password) {
    if (password == null || !STRONG_PASSWORD.matcher(password).matches())
      throw new IllegalArgumentException(
          "Password must be 12-72 characters and include uppercase, lowercase, number, and symbol");
  }

  private void validateVersion(VersionRequest r) {
    if (r.rolloutPercent() < 1 || r.rolloutPercent() > 100)
      throw new IllegalArgumentException("Rollout percent must be between 1 and 100");
    if (clean(r.channel()).isEmpty())
      throw new IllegalArgumentException("Release channel is required");
    String sha = clean(r.packageSha256());
    if (!sha.isEmpty() && !sha.matches("(?i)[0-9a-f]{64}"))
      throw new IllegalArgumentException("Package SHA-256 must contain 64 hexadecimal characters");
    if (r.published() && (clean(r.downloadUrl()).isEmpty() || sha.isEmpty()))
      throw new IllegalArgumentException(
          "Published versions require a download URL and package SHA-256");
  }

  private record AdminRow(
      long id,
      String username,
      String password,
      String displayName,
      String status,
      int failedLoginAttempts,
      LocalDateTime lockedUntil,
      String role,
      String permissions) {}

  private record DailyCount(LocalDate statDate, long total) {}

  public record LoginResponse(
      String accessToken,
      String displayName,
      String role,
      List<String> permissions,
      long expiresIn) {}

  public record AdminAccountRequest(
      String username, String displayName, String password, long roleId, boolean enabled) {}

  public record RoleRequest(
      String roleCode, String roleName, boolean enabled, List<Long> permissionIds) {}

  public record VersionRequest(
      String platform,
      String channel,
      String versionName,
      int versionCode,
      String downloadUrl,
      String releaseNotes,
      boolean forceUpdate,
      int rolloutPercent,
      String packageSha256,
      int minSupportedVersionCode,
      boolean published) {}
}
