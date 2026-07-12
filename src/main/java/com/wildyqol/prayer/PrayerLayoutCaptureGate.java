package com.wildyqol.prayer;

final class PrayerLayoutCaptureGate
{
	private long activeProfileId;
	private long generation;
	private boolean active;

	synchronized void activate(long profileId)
	{
		activeProfileId = profileId;
		active = true;
		generation++;
	}

	synchronized void invalidate()
	{
		active = false;
		generation++;
	}

	synchronized Token token(long profileId)
	{
		return active && profileId == activeProfileId
			? new Token(profileId, generation)
			: null;
	}

	synchronized boolean isCurrent(Token token, long profileId)
	{
		return token != null
			&& active
			&& token.profileId == activeProfileId
			&& token.profileId == profileId
			&& token.generation == generation;
	}

	static final class Token
	{
		private final long profileId;
		private final long generation;

		private Token(long profileId, long generation)
		{
			this.profileId = profileId;
			this.generation = generation;
		}
	}
}
