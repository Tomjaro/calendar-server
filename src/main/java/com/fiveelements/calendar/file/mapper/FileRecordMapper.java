package com.fiveelements.calendar.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fiveelements.calendar.file.domain.FileRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileRecordMapper extends BaseMapper<FileRecord> {
  int countUsage(@Param("id") long id, @Param("userId") long userId);

  List<FileRecord> selectActiveFiles();
}
