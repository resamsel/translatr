package models;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 5 Oct 2016
 */
public enum ProjectRole {
  Owner, Developer, Translator;

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRole.class);

  /**
   * @param role
   * @return
   */
  public boolean hasPermission(ProjectRole role) {
    LOGGER.debug("{}.hasPermission({})", this, role);

    switch (this) {
      case Owner:
        return Arrays.asList(Translator, Developer, Owner).contains(role);
      case Developer:
        return Arrays.asList(Translator, Developer).contains(role);
      case Translator:
        return Arrays.asList(Translator).contains(role);
      default:
        return false;
    }
  }
}
