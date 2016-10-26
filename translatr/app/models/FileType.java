package models;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * <p>
 *
 * @author resamsel
 * @version 30 Aug 2016
 */
public enum FileType
{
	JavaProperties("java_properties"),

	PlayMessages("play_messages"),

	Gettext("gettext");

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

	/**
	 * @return the key
	 */
	public String key()
	{
		return key;
	}

	public static FileType fromKey(String key)
	{
		if(!KEYMAP.containsKey(key))
			throw new IllegalArgumentException(key + "(" + KEYMAP + ")");

		return KEYMAP.get(key);
	}
}
