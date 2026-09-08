package com.fiveelements.calendar.region.domain;

public final class RegionDtos {
  private RegionDtos() {}

  public record RegionView(int id, String name, Integer parentId, Integer levelType) {}
}
