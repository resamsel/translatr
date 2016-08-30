package models;

import java.util.HashMap;
import java.util.Map;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 30 Aug 2016
 */
public enum FileType
{
	JavaProperties("java_properties"),

	PlayMessages("play_messages");

	private static final Map<String, FileType> KEYMAP = new HashMap<>();

	static
	{
		for(FileType type : FileType.values())
			KEYMAP.put(type.key, type);
	}

	private String key;

	FileType(String key)
	{
		this.key = key;
	}

	public static FileType fromKey(String key)
	{
		if(!KEYMAP.containsKey(key))
			throw new IllegalArgumentException(key);

		return KEYMAP.get(key);
	}
}
