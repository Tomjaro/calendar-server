package com.fiveelements.calendar.record.domain;
import jakarta.validation.constraints.NotBlank;import jakarta.validation.constraints.Size;import java.time.LocalDateTime;import java.util.List;
public final class RecordModels {
 private RecordModels() {}
 public record CreateRecordRequest(@NotBlank @Size(max=100000) String content,@Size(max=10) List<@Size(min=1,max=20) String> tags) {}
 public record RecordRow(long id,String content,String plainContent,String contentFormat,String tagsText,LocalDateTime createTime) {}
 public record RecordView(long id,String content,String plainContent,String contentFormat,List<String> tags,LocalDateTime createTime) {}
 public record TagView(String name,long useCount) {}
}
