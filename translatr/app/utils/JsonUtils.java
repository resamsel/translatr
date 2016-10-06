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
				return getAsText(node, "name");
			case "dto.Message":
				return String.format("%s (%s)", getAsText(node, "keyName"), getAsText(node, "localeName"));
			case "dto.ProjectUser":
				return String.format("%s (%s)", getAsText(node, "projectName"), getAsText(node, "userName"));
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
					return routes.Projects.project(uuid);
			break;
			case "dto.Locale":
				if(uuid != null)
					return routes.Locales.locale(uuid);
			break;
			case "dto.Key":
				if(uuid != null)
					return routes.Keys.key(uuid);
			break;
			case "dto.Message":
				UUID keyId = getUuid(node, "keyId");
				if(keyId != null)
					return routes.Keys.key(keyId);
			break;
			case "dto.ProjectUser":
				if(uuid != null)
					return routes.Projects.members(uuid);
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
			case "dto.ProjectUser":
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
				return "teal";
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

	/**
	 * @param node
	 * @param string
	 * @return
	 */
	private static String getAsText(JsonNode node, String fieldName)
	{
		if(!node.hasNonNull(fieldName))
			return null;

		return node.get(fieldName).asText();
	}
}
