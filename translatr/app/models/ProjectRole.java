package models;

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
  Owner(3), Developer(2), Translator(1);

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRole.class);

  private int order;

  private ProjectRole(int order) {
    this.order = order;
  }

  /**
   * @param role
   * @return
   */
  public boolean hasPermission(ProjectRole role) {
    LOGGER.debug("{}.hasPermission({}): {}", this, role, order >= role.order);

    return order >= role.order;
  }
}
