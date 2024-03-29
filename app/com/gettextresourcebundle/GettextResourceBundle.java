/**
 * Copyright (c) 2012 Tom Schaible See the file license.txt for copying permission.
 */
package com.gettextresourcebundle;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tom Schaible
 *
 *         A resource bundle that is created from a gettext PO file
 */
public class GettextResourceBundle extends ResourceBundle {
  private static final Logger LOG = LoggerFactory.getLogger(GettextResourceBundle.class);

  private static final Pattern LINE_PATTERN = Pattern.compile("^([\\w_\\[\\]]*)\\s*\\\"(.*)\\\"$");

  private Map<String, Object> resources = new HashMap<String, Object>();;

  public GettextResourceBundle(Reader reader) {
    init(new LineNumberReader(reader));
  }

  /**
   * initialize the ResourceBundle from a PO file
   *
   * if
   *
   * @param reader the reader to read the contents of the PO file from
   */
  private void init(LineNumberReader reader) {
    if (reader != null) {
      String line = null;
      String key = null;
      String value = null;
      try {
        while ((line = reader.readLine()) != null) {
          if (line.startsWith("#")) {
            LOG.trace(reader.getLineNumber() + ": Parsing PO file, comment skipped [" + line + "]");
          } else if (line.trim().length() == 0) {
            LOG.trace(reader.getLineNumber() + ": Parsing PO file, whitespace line skipped");
          } else {
            Matcher matcher = LINE_PATTERN.matcher(line);
            if (matcher.matches()) {
              String type = matcher.group(1);
              String str = matcher.group(2);
              if ("msgid".equals(type)) {
                if (key != null && value != null) {
                  LOG.debug("Parsing PO file, key,value pair found [" + key + " => " + value + "]");
                  resources.put(StringEscapeUtils.unescapeJava(key),
                      StringEscapeUtils.unescapeJava(value));
                  key = null;
                  value = null;
                }
                key = str;
                LOG.trace(reader.getLineNumber() + ": Parsing PO file, msgid found [" + key + "]");
              } else if ("msgstr".equals(type)) {
                value = str;
                LOG.trace(
                    reader.getLineNumber() + ": Parsing PO file, msgstr found [" + value + "]");
              } else if (type == null || type.length() == 0) {
                if (value == null) {
                  LOG.trace(reader.getLineNumber() + ": Parsing PO file, addition to msgid found ["
                      + str + "]");
                  key += str;
                } else {
                  LOG.trace(reader.getLineNumber() + ": Parsing PO file, addition to msgstr found ["
                      + str + "]");
                  value += str;
                }

              }
            } else {
              LOG.error(
                  reader.getLineNumber() + ": Parsing PO file, invalid syntax [" + line + "]");
            }
          }

        }

        if (key != null && value != null) {
          LOG.debug("Parsing PO file, key,value pair found [" + key + " => " + value + "]");
          resources.put(StringEscapeUtils.unescapeJava(key), StringEscapeUtils.unescapeJava(value));
          key = null;
          value = null;
        }
      } catch (IOException e) {
        LOG.error("GettextResourceBundle could not be initialized", e);
      }

    } else {
      LOG.warn("GettextResourceBundle could not be initialized, input was null");
    }
    LOG.debug(
        "GettextResourceBundle initialization complete, " + resources.size() + " resources loaded");
  }

  /*
   * (non-Javadoc)
   *
   * @see java.util.ResourceBundle#getKeys()
   */
  @Override
  public Enumeration<String> getKeys() {
    return Collections.enumeration(resources.keySet());
  }

  /*
   * (non-Javadoc)
   *
   * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
   */
  @Override
  protected Object handleGetObject(String key) {
    return resources.get(key);
  }
}
