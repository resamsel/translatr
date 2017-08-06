package tests.models;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.UUID;
import models.Project;
import org.junit.Test;

/**
 * @author resamsel
 * @version 18 May 2017
 */
public class ProjectTest {

  @Test
  public void getCacheKey() {
    UUID projectId = UUID.randomUUID();

    assertThat(Project.getCacheKey(null)).isNull();
    assertThat(Project.getCacheKey(projectId)).isEqualTo("project:id:" + projectId);
    assertThat(Project.getCacheKey(projectId, "keys"))
        .isEqualTo("project:id:" + projectId + ":keys");
    assertThat(Project.getCacheKey(projectId, "keys", "locales"))
        .isEqualTo("project:id:" + projectId + ":keys:locales");
  }
}
