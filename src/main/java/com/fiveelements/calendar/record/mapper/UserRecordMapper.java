package com.fiveelements.calendar.record.mapper;
import com.fiveelements.calendar.record.domain.RecordModels.*;import java.util.List;import org.apache.ibatis.annotations.Param;
public interface UserRecordMapper {
 List<RecordRow> selectRecords(@Param("userId") long userId,@Param("keyword") String keyword,@Param("keywordLike") String keywordLike,@Param("tag") String tag);
 RecordRow selectRecord(@Param("userId") long userId,@Param("id") long id);
 List<TagView> selectTags(@Param("userId") long userId);
 int insertRecord(@Param("userId") long userId,@Param("content") String content,@Param("plainContent") String plainContent);long lastInsertId();
 int updateRecord(@Param("userId") long userId,@Param("id") long id,@Param("content") String content,@Param("plainContent") String plainContent);
 int insertTag(@Param("userId") long userId,@Param("name") String name);Long selectTagId(@Param("userId") long userId,@Param("name") String name);
 int insertRecordTag(@Param("entryId") long entryId,@Param("tagId") long tagId);
 int insertRecordFile(@Param("entryId") long entryId,@Param("userId") long userId,@Param("fileId") long fileId);
 int deleteRecordFiles(@Param("userId") long userId,@Param("id") long id);
 int deleteRecordTags(@Param("userId") long userId,@Param("id") long id);int deleteRecord(@Param("userId") long userId,@Param("id") long id);
}
