package com.fiveelements.calendar.region.service;

import com.fiveelements.calendar.region.domain.RegionDtos.RegionView;
import com.fiveelements.calendar.region.mapper.RegionMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RegionService {
  private final RegionMapper mapper;

  public RegionService(RegionMapper mapper) {
    this.mapper = mapper;
  }

  public List<RegionView> list(Integer parentId) {
    if (parentId == null) return mapper.selectProvinces();
    return mapper.selectChildren(parentId);
  }
}
