package com.fiveelements.calendar.record.service;
import com.fiveelements.calendar.record.domain.RecordModels.*;import com.fiveelements.calendar.record.mapper.UserRecordMapper;import java.util.*;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
@Service public class UserRecordService {
 private final UserRecordMapper mapper;public UserRecordService(UserRecordMapper mapper){this.mapper=mapper;}
 public List<RecordView> records(long uid,String keyword,String tag){String key=clean(keyword,100),selectedTag=clean(tag,20);return mapper.selectRecords(uid,key,"%"+key+"%",selectedTag).stream().map(this::view).toList();}
 public List<TagView> tags(long uid){return mapper.selectTags(uid);}
 public RecordView record(long uid,long id){RecordRow row=mapper.selectRecord(uid,id);if(row==null)throw new IllegalArgumentException("记录不存在");return view(row);}
 @Transactional public RecordView create(long uid,CreateRecordRequest request){RichTextSanitizer.RichTextContent content=RichTextSanitizer.normalize(request.content());mapper.insertRecord(uid,content.html(),content.text());long id=mapper.lastInsertId();syncFiles(uid,id,content.fileIds());for(String tag:cleanTags(request.tags())){mapper.insertTag(uid,tag);Long tagId=mapper.selectTagId(uid,tag);if(tagId!=null)mapper.insertRecordTag(id,tagId);}return view(mapper.selectRecord(uid,id));}
 @Transactional public RecordView update(long uid,long id,CreateRecordRequest request){RichTextSanitizer.RichTextContent content=RichTextSanitizer.normalize(request.content());if(mapper.updateRecord(uid,id,content.html(),content.text())==0)throw new IllegalArgumentException("记录不存在");syncFiles(uid,id,content.fileIds());mapper.deleteRecordTags(uid,id);for(String tag:cleanTags(request.tags())){mapper.insertTag(uid,tag);Long tagId=mapper.selectTagId(uid,tag);if(tagId!=null)mapper.insertRecordTag(id,tagId);}return view(mapper.selectRecord(uid,id));}
 @Transactional public void delete(long uid,long id){mapper.deleteRecordFiles(uid,id);mapper.deleteRecordTags(uid,id);if(mapper.deleteRecord(uid,id)==0)throw new IllegalArgumentException("记录不存在");}
 private RecordView view(RecordRow row){List<String> tags=row.tagsText()==null||row.tagsText().isBlank()?List.of():List.of(row.tagsText().split("\\|\\|"));return new RecordView(row.id(),row.content(),row.plainContent(),row.contentFormat(),tags,row.createTime());}
 private List<String> cleanTags(List<String> tags){if(tags==null)return List.of();return tags.stream().map(x->clean(x,20)).filter(x->!x.isEmpty()).distinct().limit(10).toList();}
 private void syncFiles(long uid,long id,List<Long> fileIds){mapper.deleteRecordFiles(uid,id);for(long fileId:fileIds)if(mapper.insertRecordFile(id,uid,fileId)==0)throw new IllegalArgumentException("图片不存在或无权访问");}
 private String clean(String value,int max){String result=value==null?"":value.trim();return result.length()>max?result.substring(0,max):result;}
}
