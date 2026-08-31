package com.fiveelements.calendar.calendar.domain;

import java.time.LocalDate;
import java.util.List;

public record CalendarDayView(
    LocalDate date,
    int day,
    String week,
    String lunarDate,
    boolean leapMonth,
    String zodiac,
    String constellation,
    String solarTerm,
    List<String> festivals,
    String yearGanZhi,
    String monthGanZhi,
    String dayGanZhi,
    String dayStemElement,
    String dayBranchElement,
    String naYin,
    String algorithmVersion) {}
