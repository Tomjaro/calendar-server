package com.fiveelements.calendar.calendar;

import com.fiveelements.calendar.calendar.domain.CalendarDayView;
import com.fiveelements.calendar.calendar.domain.MonthView;
import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CalendarService {
  static final String ALGORITHM_VERSION = "lunar-java-1.7.4-rule-v1";
  private static final Map<Character, String> ELEMENTS =
      Map.ofEntries(
          Map.entry('甲', "木"), Map.entry('乙', "木"),
          Map.entry('丙', "火"), Map.entry('丁', "火"),
          Map.entry('戊', "土"), Map.entry('己', "土"),
          Map.entry('庚', "金"), Map.entry('辛', "金"),
          Map.entry('壬', "水"), Map.entry('癸', "水"),
          Map.entry('子', "水"), Map.entry('亥', "水"),
          Map.entry('寅', "木"), Map.entry('卯', "木"),
          Map.entry('巳', "火"), Map.entry('午', "火"),
          Map.entry('申', "金"), Map.entry('酉', "金"),
          Map.entry('辰', "土"), Map.entry('戌', "土"),
          Map.entry('丑', "土"), Map.entry('未', "土"));

  public MonthView month(int year, int month) {
    validateYear(year);
    final YearMonth yearMonth;
    try {
      yearMonth = YearMonth.of(year, month);
    } catch (DateTimeException exception) {
      throw new IllegalArgumentException("月份必须在 1 到 12 之间");
    }
    List<CalendarDayView> days = new ArrayList<>(yearMonth.lengthOfMonth());
    for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
      days.add(day(yearMonth.atDay(day)));
    }
    return new MonthView(year, month, days);
  }

  public CalendarDayView day(LocalDate date) {
    validateYear(date.getYear());
    Solar solar = Solar.fromYmd(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    Lunar lunar = solar.getLunar();
    String dayGanZhi = lunar.getDayInGanZhi();
    List<String> festivals = new ArrayList<>();
    festivals.addAll(solar.getFestivals());
    festivals.addAll(lunar.getFestivals());
    return new CalendarDayView(
        date,
        date.getDayOfMonth(),
        solar.getWeekInChinese(),
        lunar.getMonthInChinese() + "月" + lunar.getDayInChinese(),
        lunar.getMonth() < 0,
        lunar.getYearShengXiao(),
        solar.getXingZuo(),
        lunar.getJieQi(),
        festivals.stream().distinct().toList(),
        lunar.getYearInGanZhiByLiChun(),
        lunar.getMonthInGanZhiExact(),
        dayGanZhi,
        elementOf(dayGanZhi.charAt(0)),
        elementOf(dayGanZhi.charAt(1)),
        lunar.getDayNaYin(),
        ALGORITHM_VERSION);
  }

  private String elementOf(char value) {
    return ELEMENTS.getOrDefault(value, "未知");
  }

  private void validateYear(int year) {
    if (year < 1901 || year > 2100) {
      throw new IllegalArgumentException("支持的年份范围为 1901 至 2100");
    }
  }
}
