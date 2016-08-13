package controllers;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;

import exporters.Exporter;
import exporters.PlayExporter;
import models.Key;
import models.Locale;
import models.Message;
import models.Project;
import play.api.routing.JavaScriptReverseRoute;
import play.mvc.Controller;
import play.mvc.Result;
import play.routing.JavaScriptReverseRouter;
import scala.collection.JavaConversions;

/**
 * This controller contains an action to handle HTTP requests to the
 * application's home page.
 */
public class ApplicationController extends Controller {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);

	public Result index() {
		return ok(views.html.index.render());
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

	public Result locale(UUID id) {
		Locale locale = Locale.find.byId(id);

		if (locale == null)
			return redirect(controllers.routes.ApplicationController.index());

		Project project = Project.find.byId(locale.project.id);
		
		Collections.sort(project.keys, (a, b) -> a.name.compareTo(b.name));
		
		return ok(views.html.locale.render(project, locale, Locale.find.all()));
	}
	
	public Result localeExport(UUID id) {
		Locale locale = Locale.find.byId(id);

		if (locale == null)
			return redirect(controllers.routes.ApplicationController.index());

		Exporter exporter = new PlayExporter();
		
		exporter.addHeaders(response(), locale);
		
		return ok(new ByteArrayInputStream(exporter.export(locale)));
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

		return redirect(controllers.routes.ApplicationController.index());
	}

	public Result javascriptRoutes() {
		return ok(JavaScriptReverseRouter.create("jsRoutes", routes.javascript.ApplicationController.locale(),
				routes.javascript.ApiController.getMessage(), routes.javascript.ApiController.putMessage()));
	}
}
