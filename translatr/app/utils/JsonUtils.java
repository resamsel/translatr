package utils;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.routes;
import models.LogEntry;
import play.libs.Json;
import play.mvc.Call;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 2 Oct 2016
 */
public class JsonUtils
{
	public static String nameOf(LogEntry activity)
	{
		JsonNode node = parse(activity);

		switch(activity.contentType)
		{
			case "dto.User":
			case "dto.Project":
			case "dto.Locale":
			case "dto.Key":
				return node.get("name").asText();
			case "dto.Message":
				return String.format("%s (%s)", node.get("keyName").asText(), node.get("localeName").asText());
			default:
				return "";
		}
	}

	public static Call linkTo(LogEntry activity)
	{
		JsonNode node = parse(activity);
		UUID uuid = getUuid(node);

		switch(activity.contentType)
		{
			case "dto.User":
				if(uuid != null)
					return routes.Users.user(uuid);
			break;
			case "dto.Project":
				if(uuid != null)
					return routes.Projects.project(getUuid(node));
			break;
			case "dto.Locale":
				if(uuid != null)
					return routes.Locales.locale(getUuid(node));
			break;
			case "dto.Key":
				if(uuid != null)
					return routes.Application.key(getUuid(node));
			break;
			case "dto.Message":
				UUID keyId = getUuid(node, "keyId");
				if(keyId != null)
					return routes.Application.key(keyId);
			break;
			default:
			break;
		}

		return null;
	}

	public static String iconOf(LogEntry activity)
	{
		switch(activity.contentType)
		{
			case "dto.Project":
				return "view_quilt";
			case "dto.Locale":
				return "book";
			case "dto.Key":
				return "vpn_key";
			case "dto.Message":
				return "message";
			case "dto.User":
				return "account_circle";
			default:
				return "";
		}
	}

	public static String colorOf(LogEntry activity)
	{
		switch(activity.contentType)
		{
			case "dto.Project":
				return "orange";
			case "dto.Locale":
				return "blue";
			case "dto.Key":
				return "light-green";
			case "dto.Message":
				return "red";
			case "dto.User":
				return "purple";
			default:
				return "";
		}
	}

	public static UUID getUuid(JsonNode node)
	{
		return getUuid(node, "id");
	}

	public static UUID getUuid(JsonNode node, String key)
	{
		if(!node.hasNonNull(key))
			return null;

		return UUID.fromString(node.get(key).asText());
	}

	public static JsonNode parse(LogEntry activity)
	{
		switch(activity.type)
		{
			case Create:
			case Update:
				return Json.parse(activity.after);
			case Delete:
				return Json.parse(activity.before);
			default:
				return Json.parse("{}");
		}
	}
}
