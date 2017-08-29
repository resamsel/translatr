package i18n;

import com.google.inject.AbstractModule;
import play.api.i18n.DefaultLangs;
import play.api.i18n.Langs;
import play.api.i18n.MessagesApi;

/**
 *
 * @author resamsel
 * @version 31 Aug 2016
 */
public class I18nModule extends AbstractModule
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure()
	{
		bind(Langs.class).to(DefaultLangs.class);
		bind(MessagesApi.class).to(TranslatrMessagesApi.class);
	}
}
