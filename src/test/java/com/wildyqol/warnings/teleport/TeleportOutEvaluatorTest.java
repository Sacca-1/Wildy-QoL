package com.wildyqol.warnings.teleport;

import com.google.common.collect.ImmutableSet;
import com.wildyqol.WildyQoLConfig.TeleportOutWarningMode;
import com.wildyqol.warnings.magic.MagicInventoryState;
import com.wildyqol.warnings.magic.MagicRune;
import com.wildyqol.warnings.magic.MagicSpellbook;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.api.ItemID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TeleportOutEvaluatorTest
{
	private final TeleportOutEvaluator evaluator = new TeleportOutEvaluator();

	@Test
	public void warnsWhenNoTeleportOut()
	{
		List<TeleportOutWarning> warnings = evaluate(TeleportOutWarningMode.LEVEL_20, 0);

		assertEquals(1, warnings.size());
		assertEquals("Missing teleport out", warnings.get(0).getText());
	}

	@Test
	public void neverModeDoesNotWarn()
	{
		assertTrue(evaluate(TeleportOutWarningMode.NEVER, 0).isEmpty());
	}

	@Test
	public void rejectsLawRunes()
	{
		assertEquals(1, evaluate(1, ItemID.LAW_RUNE).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 1).size());
	}

	@Test
	public void acceptsStandardRuneTeleport()
	{
		assertTrue(evaluateSpell(
			MagicSpellbook.STANDARD,
			25,
			runes(MagicRune.AIR, 3, MagicRune.FIRE, 1, MagicRune.LAW, 1)).isEmpty());
	}

	@Test
	public void acceptsRuneTeleportWithElementalProvider()
	{
		assertTrue(evaluateSpell(
			MagicSpellbook.STANDARD,
			25,
			runes(MagicRune.FIRE, 1, MagicRune.LAW, 1),
			ImmutableSet.of(MagicRune.AIR),
			Collections.emptyMap()).isEmpty());
	}

	@Test
	public void rejectsRuneTeleportWithoutRequiredLevel()
	{
		assertEquals(1, evaluateSpell(
			MagicSpellbook.STANDARD,
			24,
			runes(MagicRune.AIR, 3, MagicRune.FIRE, 1, MagicRune.LAW, 1)).size());
	}

	@Test
	public void rejectsRuneTeleportInLevelThirtyMode()
	{
		assertEquals(1, evaluateSpell(
			TeleportOutWarningMode.LEVEL_30,
			MagicSpellbook.STANDARD,
			25,
			runes(MagicRune.AIR, 3, MagicRune.FIRE, 1, MagicRune.LAW, 1)).size());
	}

	@Test
	public void acceptsNonStandardRuneTeleportsThatLeaveWilderness()
	{
		assertTrue(evaluateSpell(
			MagicSpellbook.ANCIENT,
			54,
			runes(MagicRune.AIR, 1, MagicRune.FIRE, 1, MagicRune.LAW, 2)).isEmpty());
		assertTrue(evaluateSpell(
			MagicSpellbook.LUNAR,
			69,
			runes(MagicRune.EARTH, 2, MagicRune.ASTRAL, 2, MagicRune.LAW, 1)).isEmpty());
		assertTrue(evaluateSpell(
			MagicSpellbook.ARCEUUS,
			17,
			runes(MagicRune.EARTH, 1, MagicRune.WATER, 1, MagicRune.LAW, 1)).isEmpty());
	}

	@Test
	public void standardApeAtollTeleportRequiresBanana()
	{
		Map<Integer, Integer> items = new HashMap<>();
		items.put(ItemID.BANANA, 1);

		assertTrue(evaluateSpell(
			MagicSpellbook.STANDARD,
			64,
			runes(MagicRune.FIRE, 2, MagicRune.WATER, 2, MagicRune.LAW, 2),
			Collections.emptySet(),
			items).isEmpty());
	}

	@Test
	public void acceptsChargedJewelry()
	{
		assertTrue(evaluate(0, ItemID.AMULET_OF_GLORY1).isEmpty());
		assertTrue(evaluate(0, ItemID.RING_OF_WEALTH_1).isEmpty());
		assertTrue(evaluate(0, ItemID.COMBAT_BRACELET1).isEmpty());
		assertTrue(evaluate(0, ItemID.SKILLS_NECKLACE1).isEmpty());
		assertTrue(evaluate(0, ItemID.SLAYER_RING_1).isEmpty());
		assertTrue(evaluate(0, ItemID.SLAYER_RING_ETERNAL).isEmpty());
	}

	@Test
	public void rejectsUnchargedJewelry()
	{
		assertEquals(1, evaluate(0, ItemID.AMULET_OF_GLORY).size());
		assertEquals(1, evaluate(0, ItemID.RING_OF_WEALTH).size());
		assertEquals(1, evaluate(0, ItemID.COMBAT_BRACELET).size());
		assertEquals(1, evaluate(0, ItemID.SKILLS_NECKLACE).size());
	}

	@Test
	public void acceptsTeleportItems()
	{
		assertTrue(evaluate(0, ItemID.RADAS_BLESSING_2).isEmpty());
		assertTrue(evaluate(0, ItemID.GAMES_NECKLACE1).isEmpty());
		assertTrue(evaluate(0, ItemID.DIGSITE_PENDANT_1).isEmpty());
		assertTrue(evaluate(0, ItemID.NECKLACE_OF_PASSAGE1).isEmpty());
		assertTrue(evaluate(0, ItemID.ROYAL_SEED_POD).isEmpty());
		assertTrue(evaluate(0, ItemID.GRAND_SEED_POD).isEmpty());
		assertTrue(evaluate(0, ItemID.PHARAOHS_SCEPTRE_26948).isEmpty());
		assertTrue(evaluate(0, ItemID.RING_OF_DUELING1).isEmpty());
		assertTrue(evaluate(0, ItemID.RING_OF_RETURNING1).isEmpty());
		assertTrue(evaluate(0, ItemID.VARROCK_TELEPORT).isEmpty());
		assertTrue(evaluate(0, ItemID.CAMELOT_TELEPORT).isEmpty());
		assertTrue(evaluate(0, ItemID.TELEPORT_TO_HOUSE).isEmpty());
		assertTrue(evaluate(0, ItemID.ECTOPHIAL).isEmpty());
		assertTrue(evaluate(0, ItemID.CONSTRUCT_CAPE).isEmpty());
		assertTrue(evaluate(0, ItemID.MAX_CAPE).isEmpty());
		assertTrue(evaluate(0, ItemID.MYTHICAL_CAPE).isEmpty());
	}

	@Test
	public void levelThirtyOnlyAcceptsLevelThirtyTeleports()
	{
		assertTrue(evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.AMULET_OF_GLORY1).isEmpty());
		assertTrue(evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.RING_OF_WEALTH_1).isEmpty());
		assertTrue(evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.COMBAT_BRACELET1).isEmpty());
		assertTrue(evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.SKILLS_NECKLACE1).isEmpty());
		assertTrue(evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.SLAYER_RING_1).isEmpty());
		assertTrue(evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.ROYAL_SEED_POD).isEmpty());
		assertTrue(evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.GRAND_SEED_POD).isEmpty());
		assertTrue(evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_26948).isEmpty());
		assertTrue(evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_26950).isEmpty());
		assertTrue(evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.ESCAPE_CRYSTAL).isEmpty());

		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.RADAS_BLESSING_2).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.CORRUPTED_ESCAPE_CRYSTAL).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.ESCAPE_CRYSTAL_25961).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.GAMES_NECKLACE1).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.RING_OF_DUELING1).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.VARROCK_TELEPORT).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.ECTOPHIAL).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.TELEPORT_TO_HOUSE).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.CONSTRUCT_CAPE).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.MAX_CAPE).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_UNCHARGED).size());
	}

	@Test
	public void rejectsZeroChargeTeleportItems()
	{
		assertEquals(1, evaluate(0, ItemID.CHRONICLE).size());
		assertEquals(1, evaluate(0, ItemID.KHAREDSTS_MEMOIRS).size());
		assertEquals(1, evaluate(0, ItemID.BOOK_OF_THE_DEAD).size());
		assertEquals(1, evaluate(0, ItemID.DRAKANS_MEDALLION).size());
		assertEquals(1, evaluate(0, ItemID.XERICS_TALISMAN).size());
		assertEquals(1, evaluate(0, ItemID.RING_OF_SHADOWS).size());
		assertEquals(1, evaluate(0, ItemID.PENDANT_OF_ATES).size());
	}

	@Test
	public void rejectsDiscontinuedPharaohsSceptres()
	{
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_9045).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_9046).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_9047).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_9048).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_9049).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_9050).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_9051).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_13074).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_13075).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_13076).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_13077).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_13078).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_16176).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_21445).size());
		assertEquals(1, evaluate(TeleportOutWarningMode.LEVEL_30, 0, ItemID.PHARAOHS_SCEPTRE_21446).size());
	}

	@Test
	public void rejectsExcludedMaxCapeVariants()
	{
		assertEquals(1, evaluate(0, ItemID.FIRE_MAX_CAPE).size());
		assertEquals(1, evaluate(0, ItemID.SARADOMIN_MAX_CAPE).size());
		assertEquals(1, evaluate(0, ItemID.ZAMORAK_MAX_CAPE).size());
		assertEquals(1, evaluate(0, ItemID.GUTHIX_MAX_CAPE).size());
		assertEquals(1, evaluate(0, ItemID.INFERNAL_MAX_CAPE).size());
		assertEquals(1, evaluate(0, ItemID.ASSEMBLER_MAX_CAPE).size());
		assertEquals(1, evaluate(0, ItemID.DIZANAS_MAX_CAPE).size());
	}

	private List<TeleportOutWarning> evaluate(int lawRunes, Integer... itemIds)
	{
		return evaluate(TeleportOutWarningMode.LEVEL_20, lawRunes, itemIds);
	}

	private List<TeleportOutWarning> evaluate(TeleportOutWarningMode mode, int lawRunes, Integer... itemIds)
	{
		EnumMap<MagicRune, Integer> runes = new EnumMap<>(MagicRune.class);
		if (lawRunes > 0)
		{
			runes.put(MagicRune.LAW, lawRunes);
		}
		return evaluator.evaluateAll(TeleportOutEvaluator.state(new MagicInventoryState(
			MagicSpellbook.STANDARD,
			99,
			ImmutableSet.copyOf(itemIds),
			runes,
			Collections.emptyMap(),
			Collections.emptySet(),
			Collections.emptyMap(),
			false,
			false,
			false,
			false)), mode);
	}

	private List<TeleportOutWarning> evaluateSpell(
		MagicSpellbook spellbook,
		int magicLevel,
		Map<MagicRune, Integer> runes)
	{
		return evaluateSpell(TeleportOutWarningMode.LEVEL_20, spellbook, magicLevel, runes);
	}

	private List<TeleportOutWarning> evaluateSpell(
		TeleportOutWarningMode mode,
		MagicSpellbook spellbook,
		int magicLevel,
		Map<MagicRune, Integer> runes)
	{
		return evaluateSpell(spellbook, magicLevel, runes, Collections.emptySet(), Collections.emptyMap(), mode);
	}

	private List<TeleportOutWarning> evaluateSpell(
		MagicSpellbook spellbook,
		int magicLevel,
		Map<MagicRune, Integer> runes,
		Set<MagicRune> providedRunes,
		Map<Integer, Integer> itemCounts)
	{
		return evaluateSpell(spellbook, magicLevel, runes, providedRunes, itemCounts, TeleportOutWarningMode.LEVEL_20);
	}

	private List<TeleportOutWarning> evaluateSpell(
		MagicSpellbook spellbook,
		int magicLevel,
		Map<MagicRune, Integer> runes,
		Set<MagicRune> providedRunes,
		Map<Integer, Integer> itemCounts,
		TeleportOutWarningMode mode)
	{
		return evaluator.evaluateAll(TeleportOutEvaluator.state(new MagicInventoryState(
			spellbook,
			magicLevel,
			ImmutableSet.copyOf(itemCounts.keySet()),
			runes,
			itemCounts,
			providedRunes,
			Collections.emptyMap(),
			false,
			false,
			false,
			false)), mode);
	}

	private Map<MagicRune, Integer> runes(
		MagicRune rune1,
		int count1,
		MagicRune rune2,
		int count2)
	{
		EnumMap<MagicRune, Integer> runes = new EnumMap<>(MagicRune.class);
		runes.put(rune1, count1);
		runes.put(rune2, count2);
		return runes;
	}

	private Map<MagicRune, Integer> runes(
		MagicRune rune1,
		int count1,
		MagicRune rune2,
		int count2,
		MagicRune rune3,
		int count3)
	{
		EnumMap<MagicRune, Integer> runes = new EnumMap<>(MagicRune.class);
		runes.put(rune1, count1);
		runes.put(rune2, count2);
		runes.put(rune3, count3);
		return runes;
	}
}
