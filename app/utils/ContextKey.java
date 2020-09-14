package utils;

import play.libs.typedmap.TypedKey;
import play.mvc.Http;

public enum ContextKey {
  AccessToken("accessToken"),

  ProjectId("projectId"),

  BrandProjectId("BrandProjectId"),

  UndoMessage("undoMessage"),

  UndoCommand("undoCommand"),

  UndefinedMessages("undefined");

  private final String key;

  ContextKey(String key) {
    this.key = key;
  }

  public <T> T get(Http.Request request) {
    return get(request, key);
  }

  public <T> T put(Http.Request request, T value) {
    return put(request, key, value);
  }

  public static <T> T get(Http.Request request, String key) {
    return (T) request.attrs().getOptional(TypedKey.create(key)).orElse(null);
  }

  public static <T> T put(Http.Request request, String key, T value) {
    request.addAttr(TypedKey.create(key), value);
    return value;
  }
}
