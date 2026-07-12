package com.wildyqol.prayer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.wildyqol.WildyQoLConfig;
import org.junit.Test;

public class PrayerLayoutConfigTest
{
	private final WildyQoLConfig config = new WildyQoLConfig()
	{
	};

	@Test
	public void persistenceIsOffByDefault()
	{
		assertFalse(config.persistPrayerReordering());
	}

	@Test
	public void restoreMessageIsOnByDefault()
	{
		assertTrue(config.gameMessageOnPrayerReordering());
	}

	@Test
	public void hidingRsnIsOffByDefault()
	{
		assertFalse(config.hideRsnInPrayerLayoutMessage());
	}
}
