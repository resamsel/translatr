package mappers;

import dto.Suggestion;
import models.Suggestable;

import java.util.List;
import java.util.stream.Collectors;

public class SuggestionMapper {
  public static Suggestion toDto(Suggestable suggestable) {
    Suggestion out = new Suggestion();

    out.value = suggestable.value();
    out.data = SuggestionDataMapper.toDto(suggestable.data());

    return out;
  }

  public static List<Suggestion> toDto(List<? extends Suggestable> suggestables) {
    return suggestables.stream()
        .map(SuggestionMapper::toDto)
        .collect(Collectors.toList());
  }
}
