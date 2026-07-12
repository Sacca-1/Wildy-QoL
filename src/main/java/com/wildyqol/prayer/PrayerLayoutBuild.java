package com.wildyqol.prayer;

enum PrayerLayoutBuild
{
	PURE("Pure", "prayerLayoutPure"),
	ZERKER("Zerker", "prayerLayoutZerker"),
	MAX_MED("Max/Med", "prayerLayoutMaxMed");

	private final String displayName;
	private final String storageKey;

	PrayerLayoutBuild(String displayName, String storageKey)
	{
		this.displayName = displayName;
		this.storageKey = storageKey;
	}

	static PrayerLayoutBuild fromLmsDefence(int defenceLevel)
	{
		if (defenceLevel <= 1)
		{
			return PURE;
		}

		if (defenceLevel <= 50)
		{
			return ZERKER;
		}

		return MAX_MED;
	}

	static PrayerLayoutBuild fromPvpArenaVarbit(int value)
	{
		switch (value)
		{
			case 0:
				return MAX_MED;
			case 1:
				return ZERKER;
			case 2:
				return PURE;
			default:
				return null;
		}
	}

	String getDisplayName()
	{
		return displayName;
	}

	String getStorageKey()
	{
		return storageKey;
	}
}
