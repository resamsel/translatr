package mappers;

import models.Feature;

public class FeatureMapper {
  public static Feature toModel(dto.Feature in) {
    return Feature.of(in.getName());
  }

  public static dto.Feature toDto(Feature in) {
    return dto.Feature.of(in.getName());
  }
}
