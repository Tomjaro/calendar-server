package com.fiveelements.calendar.mood;

import com.fiveelements.calendar.mood.service.DailyMoodService;
import com.fiveelements.calendar.mood.mapper.DailyMoodMapper;
import com.fiveelements.calendar.mood.domain.DailyMoodModels.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DailyMoodServiceTest {
  @Test void savesPainAsZero() {
    var mapper=mock(DailyMoodMapper.class);
    var date=LocalDate.now();
    when(mapper.selectOne(1L,date)).thenReturn(new DailyMoodRow(date,"pain","low",BigDecimal.ZERO,null,null));
    var result=new DailyMoodService(mapper).save(1L,new SaveDailyMoodRequest(date,"pain",null));
    verify(mapper).upsert(1L,date,"pain","low",BigDecimal.ZERO,0,null);
    assertEquals("痛苦",result.moodName());
    assertEquals(BigDecimal.ZERO,result.score());
  }
  @Test void legacyRecordsUseExistingIconsWithoutRewritingScores() {
    var mapper=mock(DailyMoodMapper.class);
    var date=LocalDate.now();
    when(mapper.selectRange(1L,date,date)).thenReturn(List.of(new DailyMoodRow(date,"heartbroken","sad",new BigDecimal("1.0"),"旧备注",null)));
    var result=new DailyMoodService(mapper).list(1L,date,date).get(0);
    assertEquals("pain",result.moodCode());
    assertEquals("/static/icons/mood/pain.svg",result.icon());
    assertEquals(new BigDecimal("1.0"),result.score());
    assertEquals("旧备注",result.note());
    verify(mapper,never()).upsert(anyLong(),any(),any(),any(),any(),anyInt(),any());
  }
}
