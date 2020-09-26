package validators;

import criterias.ProjectUserCriteria;
import io.ebean.PagedList;
import models.ProjectRole;
import models.ProjectUser;
import play.inject.Injector;
import repositories.ProjectUserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
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

  @Inject
  public ProjectUserOwnerExistsValidator(ProjectUserRepository projectUserRepository, Injector injector) {
    this.projectUserRepository = projectUserRepository;
    this.injector = injector;
  }

  @Override
  public void initialize(ProjectUserOwnerExists constraintAnnotation) {
    this.constraintAnnotation = constraintAnnotation;
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
      context.buildConstraintViolationWithTemplate(constraintAnnotation.message())
          .addPropertyNode("role")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
