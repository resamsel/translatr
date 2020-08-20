package validators;

import io.ebean.PagedList;
import criterias.ProjectUserCriteria;
import models.ProjectRole;
import models.ProjectUser;
import models.User;
import play.i18n.Lang;
import play.i18n.MessagesApi;
import play.inject.Injector;
import play.mvc.Http;
import repositories.ProjectUserRepository;
import services.AuthProvider;
import services.ContextProvider;

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
public class ProjectUserModifyAllowedValidator implements ConstraintValidator<ProjectUserModifyAllowed, ProjectUser> {

  public static final String MESSAGE = "error.notamember";
  private static final String OWNER_ROLE_PERMISSION_MESSAGE = "error.memberrolepermission.owner";
  private static final String ANY_ROLE_PERMISSION_MESSAGE = "error.memberrolepermission.any";
  private final ProjectUserRepository projectUserRepository;
  private final Injector injector;
  private final AuthProvider authProvider;
  private final ContextProvider contextProvider;

  private MessagesApi messagesApi;
  private Lang lang;

  @Inject
  public ProjectUserModifyAllowedValidator(
      ProjectUserRepository projectUserRepository, Injector injector, AuthProvider authProvider,
      ContextProvider contextProvider) {
    this.projectUserRepository = projectUserRepository;
    this.injector = injector;
    this.authProvider = authProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public void initialize(ProjectUserModifyAllowed constraintAnnotation) {
    this.messagesApi = injector.instanceOf(MessagesApi.class);
    Http.Context ctx = contextProvider.getOrNull();
    if (ctx != null) {
      this.lang = ctx.lang();
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

    User loggedInUser = authProvider.loggedInUser();
    if (loggedInUser == null) {
      // Anonymous users are allowed to do nothing
      return false;
    }

    if (loggedInUser.isAdmin()) {
      // Admins are free to do anything
      return true;
    }

    if (value.project.owner != null && Objects.equals(loggedInUser.id, value.project.owner.id)) {
      // loggedInUser is owner
      return true;
    }

    PagedList<ProjectUser> found = projectUserRepository.findBy(
        new ProjectUserCriteria().withProjectId(value.project.id).withUserId(loggedInUser.id));
    if (found == null || found.getList() == null || found.getList().isEmpty()) {
      // Logged-in-user is not member of project
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(messagesApi.get(lang, MESSAGE))
          .addPropertyNode("user")
          .addConstraintViolation();
      return false;
    }

    ProjectUser member = found.getList().get(0);
    // Is the logged-in-user allowed to set the Owner role?
    if (member.role == ProjectRole.Owner) {
      // Owners are allowed to do anything
      return true;
    }

    if (member.role == ProjectRole.Manager && value.role == ProjectRole.Owner) {
      // Owner role cannot be set by a Manager
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(messagesApi.get(lang, OWNER_ROLE_PERMISSION_MESSAGE))
          .addPropertyNode("role")
          .addConstraintViolation();
      return false;
    }

    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(messagesApi.get(lang, ANY_ROLE_PERMISSION_MESSAGE))
        .addPropertyNode("role")
        .addConstraintViolation();
    return false;
  }
}
