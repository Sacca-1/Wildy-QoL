package com.wildyqol.pvparena;

public class PvpArenaSpellbookWarning
{
	private static final PvpArenaSpellbookWarning NONE = new PvpArenaSpellbookWarning(null, false, false);

	private final PvpArenaBuild activeBuild;
	private final boolean ownSpellbookOffending;
	private final boolean opponentSpellbookOffending;

	private PvpArenaSpellbookWarning(
		PvpArenaBuild activeBuild,
		boolean ownSpellbookOffending,
		boolean opponentSpellbookOffending)
	{
		this.activeBuild = activeBuild;
		this.ownSpellbookOffending = ownSpellbookOffending;
		this.opponentSpellbookOffending = opponentSpellbookOffending;
	}

	static PvpArenaSpellbookWarning none()
	{
		return NONE;
	}

	static PvpArenaSpellbookWarning of(
		PvpArenaBuild activeBuild,
		boolean ownSpellbookOffending,
		boolean opponentSpellbookOffending)
	{
		if (!ownSpellbookOffending && !opponentSpellbookOffending)
		{
			return NONE;
		}

		return new PvpArenaSpellbookWarning(activeBuild, ownSpellbookOffending, opponentSpellbookOffending);
	}

	public boolean hasWarning()
	{
		return ownSpellbookOffending || opponentSpellbookOffending;
	}

	PvpArenaBuild getActiveBuild()
	{
		return activeBuild;
	}

	public boolean isOwnSpellbookOffending()
	{
		return ownSpellbookOffending;
	}

	public boolean isOpponentSpellbookOffending()
	{
		return opponentSpellbookOffending;
	}
}
