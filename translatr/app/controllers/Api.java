package controllers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;

import criterias.LocaleCriteria;
import criterias.MessageCriteria;
import models.Locale;
import models.Message;
import models.Project;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class Api extends Controller
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Api.class);

	@BodyParser.Of(BodyParser.Json.class)
	public Result putProject()
	{
		JsonNode json = request().body().asJson();

		Project project = null;
		if(json.has("id"))
		{
			project = Project.byId(UUID.fromString(json.get("id").asText()));
		}
		else
		{
			project = Json.fromJson(json, Project.class);
			LOGGER.debug("Project: {}", Json.toJson(project));
			Ebean.save(project);
		}

		return ok(Json.toJson(project));
	}

	public Result findLocales(UUID projectId)
	{
		List<Locale> locales = Locale
			.findBy(new LocaleCriteria().withProjectId(projectId).withLocaleName(request().getQueryString("localeName")));

		return ok(Json.toJson(locales.stream().map(l -> new dto.Locale(l)).collect(Collectors.toList())));
	}

	@BodyParser.Of(BodyParser.Json.class)
	public Result putLocale()
	{
		JsonNode json = request().body().asJson();

		Locale locale = null;
		if(json.has("id"))
		{
			locale = Locale.byId(UUID.fromString(json.get("id").asText()));
		}
		else
		{
			locale = Json.fromJson(json, Locale.class);
			LOGGER.debug("Locale: {}", Json.toJson(locale));
			Ebean.save(locale);
		}

		return ok(Json.toJson(locale));
	}

	@BodyParser.Of(BodyParser.Json.class)
	public Result putMessage()
	{
		JsonNode json = request().body().asJson();

		Message message = null;
		if(json.hasNonNull("id"))
		{
			message = Message.byId(UUID.fromString(json.get("id").asText()));
			message.updateFrom(Json.fromJson(json, Message.class));
		}
		else
		{
			message = Json.fromJson(json, Message.class);
			LOGGER.debug("Locale: {}", Json.toJson(message));
		}
		Ebean.save(message);

		return ok(Json.toJson(message));
	}

	public Result findMessages(UUID projectId)
	{
		List<Message> messages = Message
			.findBy(new MessageCriteria().withProjectId(projectId).withKeyName(request().getQueryString("keyName")));

		return ok(Json.toJson(messages.stream().map(m -> new dto.Message(m)).collect(Collectors.toList())));
	}

	public Result getMessage(UUID localeId, String key)
	{
		Message message = Message.byLocaleAndKeyName(localeId, key);

		if(message == null)
			return notFound(Json.toJson(new Exception("Message not found")));

		return ok(Json.toJson(message));
	}
}
