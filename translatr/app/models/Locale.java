package models;

import static utils.FormatUtils.formatLocale;
import static utils.Stopwatch.log;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Find;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import controllers.routes;
import criterias.LocaleCriteria;
import play.mvc.Http.Context;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"project_id", "name"})})
public class Locale implements Suggestable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Locale.class);

  public static final int NAME_LENGTH = 15;

  @Id
  public UUID id;

  @Version
  public Long version;

  @CreatedTimestamp
  public DateTime whenCreated;

  @UpdatedTimestamp
  public DateTime whenUpdated;

  @ManyToOne(optional = false)
  public Project project;

  @Column(nullable = false, length = NAME_LENGTH)
  public String name;

  @JsonIgnore
  @OneToMany
  public List<Message> messages;

  public Locale() {}

  public Locale(Project project, String name) {
    this.project = project;
    this.name = name;
  }

  @Override
  public String value() {
    Context ctx = Context.current();
    return ctx.messages().at("locale.autocomplete", formatLocale(ctx.lang().locale(), this));
  }

  @Override
  public Data data() {
    return Data.from(Locale.class, id, formatLocale(Context.current().lang().locale(), this),
        routes.Locales.locale(id).absoluteURL(Context.current().request()));
  }

  private static final Find<UUID, Locale> find = new Find<UUID, Locale>() {};

  /**
   * @param fromString
   * @return
   */
  public static Locale byId(UUID id) {
    return find.setId(id).fetch("project").findUnique();
  }

  /**
   * @param project
   * @return
   */
  public static List<Locale> byProject(Project project) {
    return find.fetch("project").where().eq("project", project).findList();
  }

  /**
   * @param project
   * @param name
   * @return
   */
  public static Locale byProjectAndName(Project project, String name) {
    return find.fetch("project").where().eq("project", project).eq("name", name).findUnique();
  }

  /**
   * @param criteria
   * @return
   */
  public static List<Locale> findBy(LocaleCriteria criteria) {
    ExpressionList<Locale> query = find.fetch("project").where();

    if (criteria.getProjectId() != null)
      query.eq("project.id", criteria.getProjectId());

    if (criteria.getLocaleName() != null)
      query.eq("name", criteria.getLocaleName());

    if (criteria.getSearch() != null)
      query.ilike("name", "%" + criteria.getSearch() + "%");

    if (criteria.getLimit() != null)
      query.setMaxRows(criteria.getLimit() + 1);

    if (criteria.getOffset() != null)
      query.setFirstRow(criteria.getOffset());

    if (criteria.getOrder() != null)
      query.setOrderBy(criteria.getOrder());

    return log(() -> query.findList(), LOGGER, "findBy");
  }

  public static List<Locale> last(Project project, int limit) {
    return log(() -> find.fetch("project").where().eq("project", project).order("whenUpdated desc")
        .setMaxRows(limit).findList(), LOGGER, "last(%d)", limit);
  }

  /**
   * @param project
   * @return
   */
  public static long countBy(Project project) {
    return find.where().eq("project", project).findRowCount();
  }
}
