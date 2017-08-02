package forms;

import java.util.UUID;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;

/**
 * @author resamsel
 * @version 20 Feb 2017
 */
public class ProjectOwnerForm {
  @Constraints.Required
  private UUID ownerId;

  public UUID getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(UUID ownerId) {
    this.ownerId = ownerId;
  }

  public static Form<ProjectOwnerForm> form(FormFactory formFactory) {
    return formFactory.form(ProjectOwnerForm.class);
  }
}
