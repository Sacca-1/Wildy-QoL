package com.wildyqol.pvparena;

import com.wildyqol.WildyQoLConfig.PvpArenaSpellbookWarningMode;

public class PvpArenaSpellbookWarningEvaluator
{
	public PvpArenaSpellbookWarning evaluate(
		PvpArenaSpellbookWarningMode mode,
		int activeBuildVarbit,
		int mainSpellbookVarbit,
		int zerkSpellbookVarbit,
		int pureSpellbookVarbit,
		int opponentSpellbookVarbit)
	{
		if (mode == null || mode == PvpArenaSpellbookWarningMode.NEVER)
		{
			return PvpArenaSpellbookWarning.none();
		}

		PvpArenaBuild activeBuild = PvpArenaBuild.fromVarbit(activeBuildVarbit);
		if (activeBuild == null)
		{
			return PvpArenaSpellbookWarning.none();
		}

		PvpArenaSpellbook ownSpellbook = ownSpellbook(activeBuild, mainSpellbookVarbit, zerkSpellbookVarbit, pureSpellbookVarbit);
		PvpArenaSpellbook opponentSpellbook = PvpArenaSpellbook.fromOpponentVarbit(opponentSpellbookVarbit);
		if (ownSpellbook == null || opponentSpellbook == null)
		{
			return PvpArenaSpellbookWarning.none();
		}

		if (mode == PvpArenaSpellbookWarningMode.PLAYERS_MATCH)
		{
			boolean mismatch = ownSpellbook != opponentSpellbook;
			return PvpArenaSpellbookWarning.of(activeBuild, mismatch, mismatch);
		}

		PvpArenaSpellbook targetSpellbook = targetSpellbook(mode);
		if (targetSpellbook == null)
		{
			return PvpArenaSpellbookWarning.none();
		}

		return PvpArenaSpellbookWarning.of(
			activeBuild,
			ownSpellbook != targetSpellbook,
			opponentSpellbook != targetSpellbook);
	}

	private PvpArenaSpellbook ownSpellbook(
		PvpArenaBuild activeBuild,
		int mainSpellbookVarbit,
		int zerkSpellbookVarbit,
		int pureSpellbookVarbit)
	{
		switch (activeBuild)
		{
			case MAIN:
				return PvpArenaSpellbook.fromOwnVarbit(mainSpellbookVarbit);
			case ZERK:
				return PvpArenaSpellbook.fromOwnVarbit(zerkSpellbookVarbit);
			case PURE:
				return PvpArenaSpellbook.fromOwnVarbit(pureSpellbookVarbit);
			default:
				return null;
		}
	}

	private PvpArenaSpellbook targetSpellbook(PvpArenaSpellbookWarningMode mode)
	{
		switch (mode)
		{
			case STANDARD:
				return PvpArenaSpellbook.STANDARD;
			case ANCIENT:
				return PvpArenaSpellbook.ANCIENT;
			case LUNAR:
				return PvpArenaSpellbook.LUNAR;
			default:
				return null;
		}
	}
}
