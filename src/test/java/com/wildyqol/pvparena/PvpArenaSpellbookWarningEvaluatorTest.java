package com.wildyqol.pvparena;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.wildyqol.WildyQoLConfig;
import com.wildyqol.WildyQoLConfig.PvpArenaSpellbookWarningMode;
import org.junit.Test;

public class PvpArenaSpellbookWarningEvaluatorTest
{
	private static final int BUILD_MAIN = 0;
	private static final int BUILD_ZERK = 1;
	private static final int BUILD_PURE = 2;
	private static final int OWN_ANCIENT = 0;
	private static final int OWN_STANDARD = 1;
	private static final int OWN_LUNAR = 2;
	private static final int OPPONENT_STANDARD = 0;
	private static final int OPPONENT_ANCIENT = 1;
	private static final int OPPONENT_LUNAR = 2;

	private final PvpArenaSpellbookWarningEvaluator evaluator = new PvpArenaSpellbookWarningEvaluator();

	@Test
	public void configDefaultsToAncient()
	{
		WildyQoLConfig config = new WildyQoLConfig()
		{
		};

		assertTrue(config.pvpArenaSpellbookWarningMode() == PvpArenaSpellbookWarningMode.ANCIENT);
	}

	@Test
	public void ancientModeAcceptsBothAncient()
	{
		PvpArenaSpellbookWarning warning = evaluate(
			PvpArenaSpellbookWarningMode.ANCIENT,
			BUILD_MAIN,
			OWN_ANCIENT,
			OWN_STANDARD,
			OWN_LUNAR,
			OPPONENT_ANCIENT);

		assertFalse(warning.hasWarning());
	}

	@Test
	public void ancientModeFlagsOwnAndOpponentIndependently()
	{
		PvpArenaSpellbookWarning warning = evaluate(
			PvpArenaSpellbookWarningMode.ANCIENT,
			BUILD_ZERK,
			OWN_ANCIENT,
			OWN_STANDARD,
			OWN_LUNAR,
			OPPONENT_STANDARD);

		assertTrue(warning.hasWarning());
		assertTrue(warning.isOwnSpellbookOffending());
		assertTrue(warning.isOpponentSpellbookOffending());
	}

	@Test
	public void fixedModesUseSelectedTargetSpellbook()
	{
		assertFalse(evaluate(PvpArenaSpellbookWarningMode.STANDARD, BUILD_MAIN, OWN_STANDARD, OWN_ANCIENT, OWN_LUNAR, OPPONENT_STANDARD).hasWarning());
		assertFalse(evaluate(PvpArenaSpellbookWarningMode.LUNAR, BUILD_PURE, OWN_STANDARD, OWN_ANCIENT, OWN_LUNAR, OPPONENT_LUNAR).hasWarning());
		assertTrue(evaluate(PvpArenaSpellbookWarningMode.STANDARD, BUILD_MAIN, OWN_ANCIENT, OWN_ANCIENT, OWN_LUNAR, OPPONENT_STANDARD).isOwnSpellbookOffending());
		assertTrue(evaluate(PvpArenaSpellbookWarningMode.LUNAR, BUILD_PURE, OWN_STANDARD, OWN_ANCIENT, OWN_LUNAR, OPPONENT_STANDARD).isOpponentSpellbookOffending());
	}

	@Test
	public void playersMatchModeWarnsWhenActiveOwnKitDiffersFromOpponent()
	{
		PvpArenaSpellbookWarning matching = evaluate(
			PvpArenaSpellbookWarningMode.PLAYERS_MATCH,
			BUILD_MAIN,
			OWN_ANCIENT,
			OWN_STANDARD,
			OWN_LUNAR,
			OPPONENT_ANCIENT);
		PvpArenaSpellbookWarning mismatching = evaluate(
			PvpArenaSpellbookWarningMode.PLAYERS_MATCH,
			BUILD_MAIN,
			OWN_ANCIENT,
			OWN_STANDARD,
			OWN_LUNAR,
			OPPONENT_STANDARD);

		assertFalse(matching.hasWarning());
		assertTrue(mismatching.isOwnSpellbookOffending());
		assertTrue(mismatching.isOpponentSpellbookOffending());
	}

	@Test
	public void neverModeDoesNotWarn()
	{
		PvpArenaSpellbookWarning warning = evaluate(
			PvpArenaSpellbookWarningMode.NEVER,
			BUILD_MAIN,
			OWN_STANDARD,
			OWN_STANDARD,
			OWN_LUNAR,
			OPPONENT_STANDARD);

		assertFalse(warning.hasWarning());
	}

	@Test
	public void activeBuildSelectsCorrectOwnSpellbook()
	{
		PvpArenaSpellbookWarning mainWarning = evaluate(
			PvpArenaSpellbookWarningMode.ANCIENT,
			BUILD_MAIN,
			OWN_STANDARD,
			OWN_ANCIENT,
			OWN_ANCIENT,
			OPPONENT_ANCIENT);
		PvpArenaSpellbookWarning zerkWarning = evaluate(
			PvpArenaSpellbookWarningMode.ANCIENT,
			BUILD_ZERK,
			OWN_ANCIENT,
			OWN_STANDARD,
			OWN_ANCIENT,
			OPPONENT_ANCIENT);
		PvpArenaSpellbookWarning pureWarning = evaluate(
			PvpArenaSpellbookWarningMode.ANCIENT,
			BUILD_PURE,
			OWN_ANCIENT,
			OWN_ANCIENT,
			OWN_STANDARD,
			OPPONENT_ANCIENT);

		assertTrue(mainWarning.isOwnSpellbookOffending());
		assertTrue(zerkWarning.isOwnSpellbookOffending());
		assertTrue(pureWarning.isOwnSpellbookOffending());
	}

	@Test
	public void unknownValuesDoNotWarn()
	{
		assertFalse(evaluate(PvpArenaSpellbookWarningMode.ANCIENT, 99, OWN_STANDARD, OWN_STANDARD, OWN_STANDARD, OPPONENT_STANDARD).hasWarning());
		assertFalse(evaluate(PvpArenaSpellbookWarningMode.ANCIENT, BUILD_MAIN, 99, OWN_STANDARD, OWN_STANDARD, OPPONENT_STANDARD).hasWarning());
		assertFalse(evaluate(PvpArenaSpellbookWarningMode.ANCIENT, BUILD_MAIN, OWN_STANDARD, OWN_STANDARD, OWN_STANDARD, 99).hasWarning());
	}

	private PvpArenaSpellbookWarning evaluate(
		PvpArenaSpellbookWarningMode mode,
		int activeBuild,
		int mainSpellbook,
		int zerkSpellbook,
		int pureSpellbook,
		int opponentSpellbook)
	{
		return evaluator.evaluate(
			mode,
			activeBuild,
			mainSpellbook,
			zerkSpellbook,
			pureSpellbook,
			opponentSpellbook);
	}
}
