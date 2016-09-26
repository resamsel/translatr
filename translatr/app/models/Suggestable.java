package models;

import java.util.UUID;

/**
 * (c) 2016 Skiline Media GmbH
 * <p>
 *
 * @author resamsel
 * @version 16 Sep 2016
 */
public interface Suggestable
{
	String value();

	Data data();

	public static class DefaultSuggestable implements Suggestable
	{
		private String value;

		private Data data;

		/**
		 * @param value
		 * @param data
		 */
		public DefaultSuggestable(String value, Data data)
		{
			this.value = value;
			this.data = data;
		}

		@Override
		public String value()
		{
			return value;
		}

		@Override
		public Data data()
		{
			return data;
		}

		public static Suggestable from(String value, Data data)
		{
			return new DefaultSuggestable(value, data);
		}
	}

	public static class Data
	{
		public String type;

		public String id;

		public String name;

		public String url;

		public static Data from(Class<?> clazz, UUID id, String name, String url)
		{
			Data out = new Data();

			out.type = clazz.getSimpleName().toLowerCase();
			if(id != null)
				out.id = id.toString();
			out.name = name;
			out.url = url;

			return out;
		}
	}
}
