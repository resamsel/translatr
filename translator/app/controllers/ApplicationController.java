package controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;

import exporters.Exporter;
import exporters.PlayExporter;
import importers.Importer;
import importers.PlayImporter;
import models.Key;
import models.Locale;
import models.Message;
import models.Project;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.routing.JavaScriptReverseRouter;
import scala.collection.JavaConversions;

/**
 * This controller contains an action to handle HTTP requests to the
 * application's home page.
 */
public class ApplicationController extends Controller {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);
	private final FormFactory formFactory;

	@Inject
	public ApplicationController(FormFactory formFactory) {
		this.formFactory = formFactory;
	}

	public Result index() {
		// return ok(views.html.index.render());
		return redirect(controllers.routes.ApplicationController.dashboard());
	}

	public Result dashboard() {
		return ok(views.html.dashboard.render(Project.find.all()));
	}

	public Result project(UUID id) {
		Project project = Project.find.byId(id);

		if (project == null)
			return redirect(controllers.routes.ApplicationController.index());

		return ok(views.html.project.render(project, Locale.find.where().eq("project", project).findList()));
	}

	public Result projectCreate() {
		Project project = formFactory.form(Project.class).bindFromRequest().get();

		LOGGER.debug("Project: {}", Json.toJson(project));

		Ebean.save(project);

		return redirect(routes.ApplicationController.project(project.id));
	}

	public Result locale(UUID id) {
		Locale locale = Locale.find.byId(id);

		if (locale == null)
			return redirect(controllers.routes.ApplicationController.index());

		Project project = Project.find.byId(locale.project.id);

		Collections.sort(project.keys, (a, b) -> a.name.compareTo(b.name));

		return ok(views.html.locale.render(project, locale, Locale.find.all()));
	}

	public Result localeImport(UUID id) {
		Locale locale = Locale.find.byId(id);

		if (locale == null)
			return redirect(routes.ApplicationController.index());

		if ("POST".equals(request().method())) {
			MultipartFormData<File> body = request().body().asMultipartFormData();
			FilePart<File> messages = body.getFile("messages");

			if (messages == null)
				return redirect(routes.ApplicationController.locale(id));

			Importer importer = new PlayImporter();

			try {
				importer.apply(messages.getFile(), locale);
			} catch (Exception e) {
				LOGGER.error("Error while importing messages", e);
			}

			return redirect(routes.ApplicationController.locale(id));
		} else {
			return ok(views.html.localeImport.render(locale));
		}
	}

	public Result localeExport(UUID id) {
		Locale locale = Locale.find.byId(id);

		if (locale == null)
			return redirect(routes.ApplicationController.index());

		Exporter exporter = new PlayExporter();

		exporter.addHeaders(response(), locale);

		return ok(new ByteArrayInputStream(exporter.apply(locale)));
	}

	public Result localeCreate(UUID projectId) {
		Project project = Project.find.byId(projectId);

		if (project == null)
			return redirect(routes.ApplicationController.index());

		Locale locale = formFactory.form(Locale.class).bindFromRequest().get();

		locale.project = project;

		LOGGER.debug("Locale: {}", Json.toJson(locale));

		Ebean.save(locale);

		return redirect(routes.ApplicationController.locale(locale.id));
	}

	public Result localeRemove(UUID localeId) {
		Locale locale = Locale.find.byId(localeId);

		if (locale == null)
			return redirect(routes.ApplicationController.index());

		Ebean.delete(locale);

		return redirect(routes.ApplicationController.project(locale.project.id));
	}

	public Result keyCreate(UUID localeId) {
		Key key = formFactory.form(Key.class).bindFromRequest().get();

		Locale locale = Locale.find.byId(localeId);
		key.project = locale.project;

		LOGGER.debug("Key: {}", Json.toJson(key));

		Ebean.save(key);

		return redirect(routes.ApplicationController.locale(locale.id).withFragment("#key=" + key.name));
	}

	public Result keyRemove(UUID keyId, UUID localeId) {
		Key key = Key.find.byId(keyId);

		LOGGER.debug("Key: {}", Json.toJson(key));

		if (key == null)
			return redirect(routes.ApplicationController.index());

		Ebean.delete(key);

		LOGGER.debug("Deleted key: {}", Json.toJson(key));

		Locale locale = Locale.find.byId(localeId);
		if (locale != null)
			return redirect(routes.ApplicationController.locale(locale.id));

		LOGGER.debug("Go to project: {}", Json.toJson(key));

		return redirect(routes.ApplicationController.project(key.project.id));
	}

	public Result load() {
		Project project = Project.find.where().eq("name", "Internal").findUnique();
		if (project == null) {
			project = new Project("Internal");
			Ebean.save(project);
		}

		for (Entry<String, scala.collection.immutable.Map<String, String>> bundle : JavaConversions
				.mapAsJavaMap(ctx().messages().messagesApi().scalaApi().messages()).entrySet()) {
			LOGGER.debug("Key: {}", bundle.getKey());
			if ("default.play".equals(bundle.getKey()))
				break;
			Locale locale = Locale.find.where().eq("project", project).eq("name", bundle.getKey()).findUnique();
			if (locale == null) {
				locale = new Locale(project, bundle.getKey());
				Ebean.save(locale);
			}
			for (Entry<String, String> msg : JavaConversions.mapAsJavaMap(bundle.getValue()).entrySet()) {
				Key key = Key.find.where().eq("project", project).eq("name", msg.getKey()).findUnique();
				if (key == null) {
					key = new Key(project, msg.getKey());
					Ebean.save(key);
				}
				Message message = Message.find.where().eq("locale", locale).eq("key", key).findUnique();
				if (message == null) {
					message = new Message(locale, key, msg.getValue());
					Ebean.save(message);
				}
			}
		}

		return redirect(controllers.routes.ApplicationController.project(project.id));
	}

	public Result javascriptRoutes() {
		return ok(JavaScriptReverseRouter.create("jsRoutes", routes.javascript.ApplicationController.locale(),
				routes.javascript.ApplicationController.keyRemove(), routes.javascript.ApiController.getMessage(),
				routes.javascript.ApiController.putMessage()));
	}
}
