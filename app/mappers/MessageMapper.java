package mappers;

import dto.Message;
import models.Key;
import models.Locale;
import utils.FormatUtils;

public class MessageMapper {
  public static models.Message toModel(Message in) {
    return toModel(in, LocaleMapper.toModel(in), KeyMapper.toModel(in));
  }

  public static models.Message toModel(Message in, Locale locale, Key key) {
    models.Message out = new models.Message();

    out.id = in.id;
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;
    out.locale = locale;
    out.key = key;
    out.value = in.value;

    return out;
  }

  public static Message toDto(models.Message in) {
    Message out = new Message();

    out.id = in.id;
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;
    out.value = in.value;

    if (in.locale != null) {
      out.localeId = in.locale.id;
      out.localeName = in.locale.name;
      out.localeDisplayName = FormatUtils.formatDisplayName(in.locale);
      out.localePathName = in.locale.getPathName();
    }

    if (in.key != null) {
      out.keyId = in.key.id;
      out.keyName = in.key.name;
      out.keyPathName = in.key.getPathName();
      if (in.key.project != null) {
        out.projectId = in.key.project.id;
        out.projectName = in.key.project.name;
        out.projectOwnerUsername = in.key.project.owner.username;
      }
    }

    return out;
  }
}
