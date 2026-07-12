package com.wildyqol.prayer;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

final class PrayerLayoutSnapshot
{
	static final int CURRENT_VERSION = 1;
	private static final String ORDER_PREFIX = "prayer_order_book_";
	private static final String HIDDEN_PREFIX = "prayer_hidden_book_";

	private int version;
	private Map<String, String> values;

	@SuppressWarnings("unused")
	private PrayerLayoutSnapshot()
	{
	}

	PrayerLayoutSnapshot(Map<String, String> values)
	{
		this.version = CURRENT_VERSION;
		this.values = sanitized(values);
	}

	boolean isSupportedVersion()
	{
		return version == CURRENT_VERSION && values != null;
	}

	Map<String, String> getValues()
	{
		return Collections.unmodifiableMap(sanitized(values));
	}

	static boolean isLayoutKey(String key)
	{
		return key != null && (key.startsWith(ORDER_PREFIX) || key.startsWith(HIDDEN_PREFIX));
	}

	private static Map<String, String> sanitized(Map<String, String> source)
	{
		Map<String, String> result = new TreeMap<>();
		if (source == null)
		{
			return result;
		}

		for (Map.Entry<String, String> entry : source.entrySet())
		{
			if (isLayoutKey(entry.getKey()) && entry.getValue() != null)
			{
				result.put(entry.getKey(), entry.getValue());
			}
		}

		return result;
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}

		if (!(other instanceof PrayerLayoutSnapshot))
		{
			return false;
		}

		PrayerLayoutSnapshot that = (PrayerLayoutSnapshot) other;
		return getValues().equals(that.getValues());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(getValues());
	}
}
