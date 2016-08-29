package controllers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import services.LocaleService;
import services.MessageService;
import services.ProjectService;

public class Api extends Controller
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Api.class);

	private final ProjectService projectService;

	private final LocaleService localeService;

	private final MessageService messageService;

	/**
	 * 
	 */
	@Inject
	public Api(ProjectService projectService, LocaleService localeService, MessageService messageService)
	{
		this.projectService = projectService;
		this.localeService = localeService;
		this.messageService = messageService;
	}

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
			projectService.save(project);
		}

		return ok(Json.toJson(dto.Project.from(project)));
	}

	public Result findLocales(UUID projectId)
	{
		List<Locale> locales = Locale
			.findBy(new LocaleCriteria().withProjectId(projectId).withLocaleName(request().getQueryString("localeName")));

		return ok(Json.toJson(locales.stream().map(l -> dto.Locale.from(l)).collect(Collectors.toList())));
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
			localeService.save(locale);
		}

		return ok(Json.toJson(dto.Locale.from(locale)));
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
		messageService.save(message);

		return ok(Json.toJson(dto.Message.from(message)));
	}

	public Result findMessages(UUID projectId)
	{
		List<Message> messages = Message
			.findBy(new MessageCriteria().withProjectId(projectId).withKeyName(request().getQueryString("keyName")));

		return ok(Json.toJson(messages.stream().map(m -> dto.Message.from(m)).collect(Collectors.toList())));
	}

	public Result getMessage(UUID localeId, String key)
	{
		Message message = Message.byLocaleAndKeyName(localeId, key);

		if(message == null)
			return notFound(Json.toJson(new Exception("Message not found")));

		return ok(Json.toJson(dto.Message.from(message)));
	}
}
