package com.wildyqol.prayer;

final class PrayerLayoutContextResolver
{
	private PrayerLayoutBuild latchedLmsBuild;

	PrayerLayoutContext resolve(
		String profileKey,
		String characterName,
		boolean activeLms,
		boolean allowLmsClassification,
		int defenceLevel,
		boolean activePvpArena,
		int pvpArenaBuildVarbit)
	{
		if (!activeLms)
		{
			latchedLmsBuild = null;
		}

		if (activeLms)
		{
			if (latchedLmsBuild == null && allowLmsClassification)
			{
				latchedLmsBuild = PrayerLayoutBuild.fromLmsDefence(defenceLevel);
			}

			return latchedLmsBuild == null ? null : PrayerLayoutContext.minigame(latchedLmsBuild);
		}

		if (activePvpArena)
		{
			PrayerLayoutBuild build = PrayerLayoutBuild.fromPvpArenaVarbit(pvpArenaBuildVarbit);
			return build == null ? null : PrayerLayoutContext.minigame(build);
		}

		return profileKey == null ? null : PrayerLayoutContext.account(profileKey, characterName);
	}

	void reset()
	{
		latchedLmsBuild = null;
	}
}
