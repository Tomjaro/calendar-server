package com.fiveelements.calendar.admin;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fiveelements.calendar.admin.controller.AdminController;
import com.fiveelements.calendar.admin.service.AdminService;
import com.fiveelements.calendar.common.GlobalExceptionHandler;
import com.fiveelements.calendar.config.RequestCorrelationFilter;
import com.fiveelements.calendar.config.SecurityConfig;
import com.fiveelements.calendar.security.JwtAuthenticationFilter;
import com.fiveelements.calendar.security.JwtService;
import com.fivelements.calendar.admin.domain.AdminPrincipal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  RequestCorrelationFilter.class,
  GlobalExceptionHandler.class
})
class AdminSecurityIntegrationTest {
  @Autowired MockMvc mvc;
  @MockBean AdminService service;
  @MockBean JwtService jwtService;

  @Test
  void rejectsAnonymousRequestsToAdminApi() throws Exception {
    mvc.perform(get("/admin-api/dashboard/overview")).andExpect(status().isForbidden());
  }

  @Test
  void rejectsNonAdminTokenOnAdminApi() throws Exception {
    when(jwtService.parseAdminToken(anyString()))
        .thenThrow(new IllegalArgumentException("wrong token type"));
    mvc.perform(get("/admin-api/dashboard/overview").header("Authorization", "Bearer app-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void permitsSuperAdminToken() throws Exception {
    when(jwtService.parseAdminToken("admin-token"))
        .thenReturn(
            new AdminPrincipal(1, "admin", "SUPER_ADMIN", java.util.List.of("dashboard:view")));
    when(service.overview()).thenReturn(Map.of("totalUsers", 0));
    mvc.perform(get("/admin-api/dashboard/overview").header("Authorization", "Bearer admin-token"))
        .andExpect(status().isOk());
  }

  @Test
  void rejectsAdminWithoutRequiredRole() throws Exception {
    when(jwtService.parseAdminToken("viewer-token"))
        .thenReturn(new AdminPrincipal(2, "viewer", "VIEWER", java.util.List.of()));
    mvc.perform(get("/admin-api/dashboard/overview").header("Authorization", "Bearer viewer-token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void returnsValidatedRequestCorrelationId() throws Exception {
    mvc.perform(get("/actuator/health").header("X-Request-Id", "request-12345678"))
        .andExpect(header().string("X-Request-Id", "request-12345678"));
    mvc.perform(get("/actuator/health").header("X-Request-Id", "invalid id"))
        .andExpect(header().exists("X-Request-Id"));
  }
}
