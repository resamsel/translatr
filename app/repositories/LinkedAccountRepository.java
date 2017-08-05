package repositories;

import com.google.inject.ImplementedBy;
import criterias.LinkedAccountCriteria;
import criterias.ProjectUserCriteria;
import models.LinkedAccount;
import models.ProjectUser;
import repositories.impl.LinkedAccountRepositoryImpl;
import repositories.impl.ProjectUserRepositoryImpl;

@ImplementedBy(LinkedAccountRepositoryImpl.class)
public interface LinkedAccountRepository extends
    ModelRepository<LinkedAccount, Long, LinkedAccountCriteria> {
  String FETCH_USER = "user";

  String[] PROPERTIES_TO_FETCH = {FETCH_USER};
}
