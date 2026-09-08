package com.fiveelements.calendar.region.mapper;

import com.fiveelements.calendar.region.domain.RegionDtos.RegionView;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RegionMapper {
  List<RegionView> selectProvinces();

  List<RegionView> selectChildren(@Param("parentId") int parentId);
}
