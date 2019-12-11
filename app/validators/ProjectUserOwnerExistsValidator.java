package validators;

import com.avaje.ebean.PagedList;
import criterias.ProjectUserCriteria;
import models.ProjectRole;
import models.ProjectUser;
import play.i18n.Lang;
import play.i18n.MessagesApi;
import play.inject.Injector;
import play.mvc.Http;
import repositories.ProjectUserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Locale;
import java.util.Objects;

/**
 * @author resamsel
 * @version 23 Jan 2017
 */
@Singleton
public class ProjectUserOwnerExistsValidator implements ConstraintValidator<ProjectUserOwnerExists, ProjectUser> {

  public static final String MESSAGE = "error.ownermissing";
  private final ProjectUserRepository projectUserRepository;
  private final Injector injector;

  private ProjectUserOwnerExists constraintAnnotation;
  private MessagesApi messagesApi;
  private Lang lang;

  @Inject
  public ProjectUserOwnerExistsValidator(ProjectUserRepository projectUserRepository, Injector injector) {
    this.projectUserRepository = projectUserRepository;
    this.injector = injector;
  }

  @Override
  public void initialize(ProjectUserOwnerExists constraintAnnotation) {
    this.constraintAnnotation = constraintAnnotation;
    this.messagesApi = injector.instanceOf(MessagesApi.class);
    if (Http.Context.current.get() != null) {
      this.lang = Http.Context.current().lang();
    } else {
      this.lang = new Lang(Locale.ENGLISH);
    }
  }

  @Override
  public boolean isValid(ProjectUser value, ConstraintValidatorContext context) {
    if (value == null) {
      context.buildConstraintViolationWithTemplate("Value required")
          .addConstraintViolation();
      return false;
    }

    if (value.role == ProjectRole.Owner) {
      // We don't care about a new owner or a kept owner
      return true;
    }

    PagedList<ProjectUser> existing = projectUserRepository.findBy(
        new ProjectUserCriteria().withProjectId(value.project.id).withRoles(ProjectRole.Owner));

    if (existing.getList()
        .stream()
        .noneMatch(member -> !Objects.equals(member.id, value.id) && member.role == ProjectRole.Owner)) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(messagesApi.get(lang, constraintAnnotation.message()))
          .addPropertyNode("role")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
