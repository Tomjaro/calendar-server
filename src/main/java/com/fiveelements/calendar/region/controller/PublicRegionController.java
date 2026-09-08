package com.fiveelements.calendar.region.controller;

import com.fiveelements.calendar.common.ApiResponse;
import com.fiveelements.calendar.region.domain.RegionDtos.RegionView;
import com.fiveelements.calendar.region.service.RegionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app-api/public/regions")
public class PublicRegionController {
  private final RegionService service;

  public PublicRegionController(RegionService service) {
    this.service = service;
  }

  @GetMapping
  public ApiResponse<List<RegionView>> list(@RequestParam(required = false) Integer parentId) {
    return ApiResponse.ok(service.list(parentId));
  }
}
