package dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 *
 * @author resamsel
 * @version 29 Aug 2016
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Dto implements Serializable {
  private static final long serialVersionUID = 6541165206617047703L;
}
