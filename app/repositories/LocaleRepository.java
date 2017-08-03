package repositories;

import com.google.inject.ImplementedBy;
import criterias.LocaleCriteria;
import java.util.List;
import java.util.UUID;
import models.Locale;
import models.Project;
import repositories.impl.LocaleRepositoryImpl;

@ImplementedBy(LocaleRepositoryImpl.class)
public interface LocaleRepository extends ModelRepository<Locale, UUID, LocaleCriteria> {

  List<Locale> latest(Project project, int limit);

  Locale byProjectAndName(Project project, String name);
}
