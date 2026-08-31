package com.fiveelements.calendar.calendar.domain;

import java.util.List;

public record MonthView(int year, int month, List<CalendarDayView> days) {}
