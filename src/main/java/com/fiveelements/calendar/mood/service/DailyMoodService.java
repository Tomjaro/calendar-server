package com.fiveelements.calendar.mood.service;

import com.fiveelements.calendar.mood.domain.DailyMoodModels.*;
import com.fiveelements.calendar.mood.mapper.DailyMoodMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DailyMoodService {
  private record Definition(String name,String category,String score) {}
  private static final Map<String,Definition> MOODS=Map.ofEntries(
    mood("bliss","幸福","positive","5"),mood("happy","开心","positive","5"),mood("content","满足","positive","4"),mood("relaxed","放松","positive","4"),mood("grateful","感恩","positive","4"),mood("calm","平静","calm","3"),mood("bored","无聊","calm","2"),mood("conflicted","纠结","calm","2"),mood("helpless","无奈","calm","2"),mood("tired","疲惫","calm","2"),mood("irritated","烦躁","low","1"),mood("anxious","焦虑","low","1"),mood("sad","难过","low","1"),mood("angry","愤怒","low","1"),mood("pain","痛苦","low","0"));
  private static final Map<String,String> LEGACY=Map.ofEntries(Map.entry("pleasant","happy"),Map.entry("moved","bliss"),Map.entry("sweet","bliss"),Map.entry("proud","content"),Map.entry("fulfilled","content"),Map.entry("expectant","content"),Map.entry("curious","calm"),Map.entry("surprised","calm"),Map.entry("sleepy","tired"),Map.entry("confused","conflicted"),Map.entry("stressed","anxious"),Map.entry("lost","sad"),Map.entry("heartbroken","pain"),Map.entry("other","calm"),Map.entry("healed","relaxed"),Map.entry("very_happy","bliss"));
  private final DailyMoodMapper mapper;
  public DailyMoodService(DailyMoodMapper mapper){this.mapper=mapper;}
  private static Map.Entry<String,Definition> mood(String code,String name,String category,String score){return Map.entry(code,new Definition(name,category,score));}
  public List<DailyMoodView> list(long userId,LocalDate start,LocalDate end){if(end.isBefore(start)||start.plusYears(1).isBefore(end))throw new IllegalArgumentException("心情查询日期范围无效");return mapper.selectRange(userId,start,end).stream().map(this::view).toList();}
  public DailyMoodView save(long userId,SaveDailyMoodRequest request){if(!request.date().equals(LocalDate.now()))throw new IllegalArgumentException("只能记录或修改当天心情");Definition definition=MOODS.get(request.moodCode());if(definition==null)throw new IllegalArgumentException("心情类型无效");BigDecimal score=new BigDecimal(definition.score());mapper.upsert(userId,request.date(),request.moodCode(),definition.category(),score,score.multiply(BigDecimal.valueOf(2)).intValue(),cleanNote(request.note()));return view(mapper.selectOne(userId,request.date()));}
  private String cleanNote(String note){if(note==null)return null;String clean=note.trim();return clean.isEmpty()?null:clean;}
  private DailyMoodView view(DailyMoodRow row){String code=LEGACY.getOrDefault(row.moodCode(),row.moodCode());if(!MOODS.containsKey(code))code="calm";Definition d=MOODS.get(code);return new DailyMoodView(row.date(),code,d.name(),d.category(),row.score(),"/static/icons/mood/"+code+".svg",row.note(),row.updateTime());}
}
