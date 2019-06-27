package mappers;

import dto.SuggestionData;
import models.Suggestable;

public class SuggestionDataMapper {
  public static SuggestionData toDto(Suggestable.Data in) {
    SuggestionData out = new SuggestionData();

    out.id = in.id;
    out.name = in.name;
    out.type = in.type;
    out.url = in.url;

    return out;
  }
}
