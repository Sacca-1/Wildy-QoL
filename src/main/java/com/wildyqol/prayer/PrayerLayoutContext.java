package com.wildyqol.prayer;

import java.util.Objects;

final class PrayerLayoutContext
{
	private final String profileKey;
	private final String characterName;
	private final PrayerLayoutBuild build;

	private PrayerLayoutContext(String profileKey, String characterName, PrayerLayoutBuild build)
	{
		this.profileKey = profileKey;
		this.characterName = characterName;
		this.build = build;
	}

	static PrayerLayoutContext account(String profileKey, String characterName)
	{
		return new PrayerLayoutContext(Objects.requireNonNull(profileKey), characterName, null);
	}

	static PrayerLayoutContext minigame(PrayerLayoutBuild build)
	{
		return new PrayerLayoutContext(null, null, Objects.requireNonNull(build));
	}

	boolean isAccount()
	{
		return profileKey != null;
	}

	String getProfileKey()
	{
		return profileKey;
	}

	String getStorageKey()
	{
		return isAccount() ? PrayerLayoutStore.ACCOUNT_STORAGE_KEY : build.getStorageKey();
	}

	String getRestoreMessage(boolean hideRsn)
	{
		if (isAccount())
		{
			if (hideRsn)
			{
				return "Wildy QoL: Prayer layout restored.";
			}

			String name = characterName == null || characterName.isEmpty() ? "this character" : characterName;
			return "Wildy QoL: Prayer layout restored for " + name + ".";
		}

		return "Wildy QoL: Prayer layout restored for the "
			+ build.getDisplayName() + " LMS / PvP Arena setup.";
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}

		if (!(other instanceof PrayerLayoutContext))
		{
			return false;
		}

		PrayerLayoutContext that = (PrayerLayoutContext) other;
		return Objects.equals(profileKey, that.profileKey) && build == that.build;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(profileKey, build);
	}
}
