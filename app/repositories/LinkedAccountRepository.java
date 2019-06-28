package repositories;

import com.google.inject.ImplementedBy;
import criterias.LinkedAccountCriteria;
import models.LinkedAccount;
import repositories.impl.LinkedAccountRepositoryImpl;

@ImplementedBy(LinkedAccountRepositoryImpl.class)
public interface LinkedAccountRepository extends
    ModelRepository<LinkedAccount, Long, LinkedAccountCriteria> {
  String FETCH_USER = "user";

  String[] PROPERTIES_TO_FETCH = {FETCH_USER};
}
