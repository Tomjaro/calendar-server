package com.fiveelements.calendar.config;

import com.fiveelements.calendar.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter filter)
      throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .cors(cors -> {})
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/actuator/health",
                        "/actuator/health/**",
                        "/actuator/info",
                        "/actuator/prometheus",
                        "/app-api/public/**",
                        "/app-api/auth/**")
                    .permitAll()
                    .requestMatchers("/admin-api/auth/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/admin-api/dashboard/**")
                    .hasAuthority("dashboard:view")
                    .requestMatchers(HttpMethod.GET, "/admin-api/users", "/admin-api/page-summary")
                    .hasAnyAuthority("user:view", "audit:view", "feedback:manage")
                    .requestMatchers("/admin-api/users/**")
                    .hasAuthority("user:manage")
                    .requestMatchers("/admin-api/versions/**")
                    .hasAuthority("version:manage")
                    .requestMatchers("/admin-api/feedback/**")
                    .hasAuthority("feedback:manage")
                    .requestMatchers("/admin-api/login-logs", "/admin-api/operation-logs")
                    .hasAuthority("audit:view")
                    .requestMatchers(HttpMethod.GET, "/admin-api/roles", "/admin-api/permissions")
                    .hasAuthority("role:view")
                    .requestMatchers("/admin-api/roles/**")
                    .hasAuthority("role:manage")
                    .requestMatchers("/admin-api/admins/**")
                    .hasAuthority("admin:manage")
                    .requestMatchers("/admin-api/auth/password")
                    .authenticated()
                    .requestMatchers("/admin-api/**")
                    .denyAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                    (request, response, authException) -> {
                      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                      response.setContentType("application/json;charset=UTF-8");
                      response
                          .getWriter()
                          .write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
                    }))
        .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
