package repositories;

import com.google.common.collect.ImmutableMap;
import com.google.inject.ImplementedBy;
import criterias.LocaleCriteria;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import models.Locale;
import models.Project;
import repositories.impl.LocaleRepositoryImpl;

@ImplementedBy(LocaleRepositoryImpl.class)
public interface LocaleRepository extends ModelRepository<Locale, UUID, LocaleCriteria> {

  String FETCH_PROJECT = "project";

  String FETCH_MESSAGES = "messages";

  String[] PROPERTIES_TO_FETCH = {FETCH_PROJECT};

  Map<String, List<String>> FETCH_MAP =
      ImmutableMap.of(
          FETCH_PROJECT, Arrays.asList(FETCH_PROJECT, FETCH_PROJECT + ".owner"),
          FETCH_MESSAGES, Arrays.asList(FETCH_MESSAGES, FETCH_MESSAGES + ".key"));

  List<Locale> latest(Project project, int limit);

  Locale byProjectAndName(Project project, String name);
}
