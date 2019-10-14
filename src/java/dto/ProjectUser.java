package dto;

import org.joda.time.DateTime;

import java.util.UUID;

public class ProjectUser extends Dto {

  private static final long serialVersionUID = 523759011096520257L;

  public Long id;
  public DateTime whenCreated;
  public DateTime whenUpdated;

  public ProjectRole role;

  public UUID projectId;
  public String projectName;
  public String projectOwnerUsername;

  public UUID userId;
  public String userName;
  public String userUsername;
  public String userEmailHash;
}
