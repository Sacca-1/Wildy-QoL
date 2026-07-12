package com.wildyqol.prayer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PrayerLayoutContextTest
{
	@Test
	public void accountMessageIncludesRsnByDefault()
	{
		PrayerLayoutContext context = PrayerLayoutContext.account("profile", "Player Name");

		assertEquals(
			"Wildy QoL: Prayer layout restored for Player Name.",
			context.getRestoreMessage(false));
	}

	@Test
	public void accountMessageCanHideRsn()
	{
		PrayerLayoutContext context = PrayerLayoutContext.account("profile", "Player Name");

		assertEquals(
			"Wildy QoL: Prayer layout restored.",
			context.getRestoreMessage(true));
	}

	@Test
	public void lmsAndPvpArenaMessageStillIdentifiesBuildWhenRsnIsHidden()
	{
		PrayerLayoutContext context = PrayerLayoutContext.minigame(PrayerLayoutBuild.PURE);

		assertEquals(
			"Wildy QoL: Prayer layout restored for the Pure LMS / PvP Arena setup.",
			context.getRestoreMessage(true));
	}
}
