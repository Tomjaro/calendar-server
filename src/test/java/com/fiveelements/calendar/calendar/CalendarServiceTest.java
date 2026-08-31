package com.fiveelements.calendar.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fiveelements.calendar.calendar.domain.CalendarDayView;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CalendarServiceTest {
  private final CalendarService service = new CalendarService();

  @Test
  void returnsStableDayInformation() {
    CalendarDayView day = service.day(LocalDate.of(2026, 8, 2));
    assertThat(day.date()).isEqualTo(LocalDate.of(2026, 8, 2));
    assertThat(day.dayGanZhi()).hasSize(2);
    assertThat(day.dayStemElement()).isIn("木", "火", "土", "金", "水");
    assertThat(day.algorithmVersion()).isEqualTo(CalendarService.ALGORITHM_VERSION);
  }

  @Test
  void rejectsUnsupportedYears() {
    assertThatThrownBy(() -> service.month(2101, 1)).isInstanceOf(IllegalArgumentException.class);
  }
}
