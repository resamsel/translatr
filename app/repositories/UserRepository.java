package repositories;

import com.feth.play.module.pa.user.AuthUserIdentity;
import com.google.inject.ImplementedBy;
import criterias.ProjectCriteria;
import criterias.UserCriteria;
import java.util.UUID;
import models.Project;
import models.User;
import repositories.impl.ProjectRepositoryImpl;
import repositories.impl.UserRepositoryImpl;

@ImplementedBy(UserRepositoryImpl.class)
public interface UserRepository extends ModelRepository<User, UUID, UserCriteria> {

  User byUsername(String username, String... fetches);

  User findByAuthUserIdentity(AuthUserIdentity identity);

  String nameToUsername(String name);

  String emailToUsername(String email);
}
