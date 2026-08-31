package com.fiveelements.calendar.security;

public final class SecurityContext {
  private SecurityContext() {}

  public static UserPrincipal currentUser() {
    var authentication =
        org.springframework.security.core.context.SecurityContextHolder.getContext()
            .getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
      throw new org.springframework.security.access.AccessDeniedException("未登录或登录已过期");
    }
    return principal;
  }
}
