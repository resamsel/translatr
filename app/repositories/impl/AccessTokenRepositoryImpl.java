package repositories.impl;

import static models.AccessToken.FETCH_USER;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import criterias.AccessTokenCriteria;
import criterias.HasNextPagedList;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import models.AccessToken;
import models.ActionType;
import models.LogEntry;
import org.apache.commons.lang3.StringUtils;
import play.cache.CacheApi;
import repositories.AccessTokenRepository;
import repositories.LogEntryRepository;
import utils.QueryUtils;

@Singleton
public class AccessTokenRepositoryImpl extends
    AbstractModelRepository<AccessToken, Long, AccessTokenCriteria> implements
    AccessTokenRepository {

  private static final String[] PROPERTIES_TO_FETCH = new String[]{FETCH_USER};


  public final Find<Long, AccessToken> find = new Find<Long, AccessToken>() {
  };

  @Inject
  public AccessTokenRepositoryImpl(Validator validator, CacheApi cache,
      LogEntryRepository logEntryRepository) {
    super(validator, cache, logEntryRepository);
  }

  @Override
  public PagedList<AccessToken> findBy(AccessTokenCriteria criteria) {
    ExpressionList<AccessToken> query = fetch().where();

    if (criteria.getUserId() != null) {
      query.eq("user.id", criteria.getUserId());
    }

    if (StringUtils.isNoneEmpty(criteria.getSearch())) {
      query.ilike("name", "%" + criteria.getSearch() + "%");
    }

    criteria.paged(query);

    return HasNextPagedList.create(query);
  }

  @Override
  public AccessToken byId(Long id, String... fetches) {
    return fetch().setId(id).findUnique();
  }

  @Override
  public AccessToken byKey(String key) {
    return fetch().where().eq("key", key).findUnique();
  }

  @Override
  public AccessToken byUserAndName(UUID userId, String name) {
    return find.where().eq("user.id", userId).eq("name", name).findUnique();
  }

  private Query<AccessToken> fetch() {
    return QueryUtils.fetch(find.query().setDisableLazyLoading(true), PROPERTIES_TO_FETCH);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void preSave(AccessToken t, boolean update) {
    if (t.key == null) {
      t.key = generateKey(AccessToken.KEY_LENGTH);
    }
  }

  @Override
  protected void prePersist(AccessToken t, boolean update) {
    if (update) {
      logEntryRepository.save(LogEntry.from(ActionType.Update, null, dto.AccessToken.class,
          dto.AccessToken.from(byId(t.id)), dto.AccessToken.from(t)));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void postSave(AccessToken t, boolean update) {
    if (!update) {
      logEntryRepository.save(LogEntry.from(ActionType.Create, null, dto.AccessToken.class, null,
          dto.AccessToken.from(t)));
    }

    cache.remove(AccessToken.getCacheKey(t.key));
  }

  private String generateKey(int length) {
    String raw = Base64.getEncoder().encodeToString(String
        .format("%s%s", UUID.randomUUID(), UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));

    if (raw.length() > length) {
      raw = raw.substring(0, length);
    }

    return raw.replace("+", "/");
  }
}
