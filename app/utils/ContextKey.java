package utils;

import play.mvc.Http.Context;

public enum ContextKey {
  AccessToken("accessToken"),

  ProjectId("projectId"),

  BrandProjectId("BrandProjectId"),

  UndoMessage("undoMessage"),

  UndoCommand("undoCommand"),

  UndefinedMessages("undefined");

  private String key;

  ContextKey(String key) {
    this.key = key;
  }

  public <T> T get() {
    try {
      return get(Context.current());
    } catch (RuntimeException e) {
      // There is no HTTP Context available from here
      return null;
    }
  }

  public <T> T get(Context ctx) {
    if (ctx == null) {
      return null;
    }

    return get(ctx, key);
  }

  public <T> T put(T value) {
    try {
      return put(Context.current(), value);
    } catch (RuntimeException e) {
      // There is no HTTP Context available from here
      return null;
    }
  }

  public <T> T put(Context ctx, T value) {
    return put(ctx, key, value);
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(Context ctx, String key) {
    return (T) ctx.args.get(key);
  }

  public static <T> T put(Context ctx, String key, T value) {
    ctx.args.put(key, value);
    return value;
  }

  public static Context context() {
    try {
      return Context.current();
    } catch (RuntimeException e) {
      // There is no HTTP Context available from here.
      return null;
    }
  }
}
