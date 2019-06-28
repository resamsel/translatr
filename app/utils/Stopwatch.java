package utils;

import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 *
 * @author resamsel
 * @version 9 Aug 2016
 */
public class Stopwatch
{
	public static Stopwatch createStarted()
	{
		return new Stopwatch();
	}

	private final com.google.common.base.Stopwatch stopwatch;

	/**
	 * 
	 */
	private Stopwatch()
	{
		this.stopwatch = com.google.common.base.Stopwatch.createStarted();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return String.format("%d ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
	}

	/**
	 * @param supplier
	 * @param logger
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static <T> T log(Supplier<T> supplier, Logger logger, String format, Object... args)
	{
		Stopwatch stopwatch = createStarted();
		try
		{
			return supplier.get();
		}
		finally
		{
			logger.debug("{} took {}", String.format(format, args), stopwatch);
		}
	}
}
