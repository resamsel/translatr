package mappers;

import dto.Message;
import models.Key;
import models.Locale;
import play.mvc.Http;
import utils.FormatUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MessageMapper {
  private final FormatUtils formatUtils;

  @Inject
  public MessageMapper(FormatUtils formatUtils) {
    this.formatUtils = formatUtils;
  }

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

  public Message toDto(models.Message in, Http.Request request) {
    Message out = new Message();

    out.id = in.id;
    out.whenCreated = in.whenCreated;
    out.whenUpdated = in.whenUpdated;
    out.value = in.value;
    out.wordCount = in.wordCount;

    if (in.locale != null) {
      out.localeId = in.locale.id;
      out.localeName = in.locale.name;
      out.localeDisplayName = formatUtils.formatDisplayName(in.locale, request);
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
