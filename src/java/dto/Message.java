package dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.joda.time.DateTime;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message extends Dto {
  private static final long serialVersionUID = -8848810325610871318L;

  public UUID id;

  @JsonIgnore
  public DateTime whenCreated;
  @JsonIgnore
  public DateTime whenUpdated;

  public UUID localeId;
  public String localeName;
  public String localeDisplayName;
  public String localePathName;

  public UUID keyId;
  public String keyName;
  public String keyPathName;

  public UUID projectId;
  public String projectName;
  public String projectOwnerUsername;

  public String value;
}
