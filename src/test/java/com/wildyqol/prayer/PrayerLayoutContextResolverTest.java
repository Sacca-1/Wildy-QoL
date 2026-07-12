package com.wildyqol.prayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class PrayerLayoutContextResolverTest
{
	private final PrayerLayoutContextResolver resolver = new PrayerLayoutContextResolver();

	@Test
	public void resolvesAccountOutsideActiveMinigames()
	{
		PrayerLayoutContext context = resolve("account-one", "Player", false, false, 99, false, 0);

		assertEquals(PrayerLayoutContext.account("account-one", "Player"), context);
	}

	@Test
	public void waitsForGameTickBeforeClassifyingLms()
	{
		assertNull(resolve("account-one", "Player", true, false, 50, false, 0));
		assertEquals(
			PrayerLayoutContext.minigame(PrayerLayoutBuild.ZERKER),
			resolve("account-one", "Player", true, true, 50, false, 0));
	}

	@Test
	public void latchesLmsBuildForTheMatch()
	{
		assertEquals(
			PrayerLayoutContext.minigame(PrayerLayoutBuild.PURE),
			resolve("account-one", "Player", true, true, 1, false, 0));
		assertEquals(
			PrayerLayoutContext.minigame(PrayerLayoutBuild.PURE),
			resolve("account-one", "Player", true, true, 99, false, 0));

		assertEquals(
			PrayerLayoutContext.account("account-one", "Player"),
			resolve("account-one", "Player", false, true, 99, false, 0));
		assertEquals(
			PrayerLayoutContext.minigame(PrayerLayoutBuild.MAX_MED),
			resolve("account-one", "Player", true, true, 99, false, 0));
	}

	@Test
	public void classifiesAllLmsBuilds()
	{
		assertLmsBuild(1, PrayerLayoutBuild.PURE);
		assertLmsBuild(50, PrayerLayoutBuild.ZERKER);
		assertLmsBuild(85, PrayerLayoutBuild.MAX_MED);
	}

	@Test
	public void mapsPvpArenaBuildVarbit()
	{
		assertEquals(PrayerLayoutContext.minigame(PrayerLayoutBuild.MAX_MED), resolve(null, null, false, false, 0, true, 0));
		assertEquals(PrayerLayoutContext.minigame(PrayerLayoutBuild.ZERKER), resolve(null, null, false, false, 0, true, 1));
		assertEquals(PrayerLayoutContext.minigame(PrayerLayoutBuild.PURE), resolve(null, null, false, false, 0, true, 2));
		assertNull(resolve("account-one", "Player", false, false, 0, true, 99));
	}

	private void assertLmsBuild(int defenceLevel, PrayerLayoutBuild expected)
	{
		resolver.reset();
		assertEquals(
			PrayerLayoutContext.minigame(expected),
			resolve("account-one", "Player", true, true, defenceLevel, false, 0));
	}

	private PrayerLayoutContext resolve(
		String profileKey,
		String characterName,
		boolean activeLms,
		boolean allowLmsClassification,
		int defenceLevel,
		boolean activePvpArena,
		int pvpArenaBuild)
	{
		return resolver.resolve(
			profileKey,
			characterName,
			activeLms,
			allowLmsClassification,
			defenceLevel,
			activePvpArena,
			pvpArenaBuild);
	}
}
