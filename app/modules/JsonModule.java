package modules;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaMapper;
import com.google.inject.AbstractModule;
import play.libs.Json;

/**
 * @author resamsel
 * @version 26 May 2017
 */
public class JsonModule extends AbstractModule {
  /**
   * {@inheritDoc}
   */
  @Override
  protected void configure() {
    JodaMapper objectMapper = new JodaMapper();
    objectMapper.setWriteDatesAsTimestamps(false);
    Json.setObjectMapper(
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));
  }
}
