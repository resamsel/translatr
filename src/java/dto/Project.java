package dto;

import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

public class Project extends Dto {

  private static final long serialVersionUID = 4241999533661305290L;

  public UUID id;

  public DateTime whenCreated;
  public DateTime whenUpdated;

  public String name;
  public String description;

  public UUID ownerId;
  public String ownerName;
  public String ownerUsername;
  public String ownerEmail;

  public List<Key> keys;
  public List<Locale> locales;
  public List<Message> messages;
  public List<ProjectUser> members;
}
