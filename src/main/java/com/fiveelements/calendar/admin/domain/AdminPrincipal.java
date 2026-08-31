package com.fiveelements.calendar.admin.domain;

import java.util.List;

public record AdminPrincipal(long id, String username, String role, List<String> permissions) {}
