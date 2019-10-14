package dto;

import org.joda.time.DateTime;

import java.util.UUID;

public class Activity extends Dto {
  public UUID id;
  public ActionType type;
  public String contentType;
  public DateTime whenCreated;
  public UUID userId;
  public String userName;
  public String userUsername;
  public String userEmailHash;
  public UUID projectId;
  public String projectName;
  public String before;
  public String after;
}
