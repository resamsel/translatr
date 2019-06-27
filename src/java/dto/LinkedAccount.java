package dto;

import org.joda.time.DateTime;

import java.util.UUID;

public class LinkedAccount extends Dto {
  private static final long serialVersionUID = 2867985852818242361L;

  public UUID userId;

  public DateTime whenCreated;

  public DateTime whenUpdated;

  public String providerKey;

  public String providerUserId;
}
