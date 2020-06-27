package dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Key extends Dto {

  private static final long serialVersionUID = 4515423450076475406L;

  public UUID id;

  public DateTime whenCreated;

  public DateTime whenUpdated;

  public UUID projectId;
  public String projectName;
  public String projectOwnerUsername;

  public String name;

  public String pathName;

  public Double progress;

  public Map<String, Message> messages;
}
