package com.wildyqol.warnings.magic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.api.ItemID;
import org.junit.Test;

public class MagicSpellbookEvaluatorTest
{
	private final MagicSpellbookEvaluator evaluator = new MagicSpellbookEvaluator();
	private final MagicThresholds thresholds = new MagicThresholds()
	{
		@Override
		public int teleBlock()
		{
			return 10;
		}

		@Override
		public int entangle()
		{
			return 50;
		}

		@Override
		public int surge()
		{
			return 100;
		}

		@Override
		public int tomeCharges()
		{
			return 50;
		}

		@Override
		public int ice()
		{
			return 100;
		}

		@Override
		public int blood()
		{
			return 50;
		}

		@Override
		public int vengeance()
		{
			return 10;
		}
	};

	@Test
	public void acceptsStandardSackSetup()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.STANDARD,
			ImmutableMap.of(),
			ImmutableMap.of(
				ItemID.BLIGHTED_TELEPORT_SPELL_SACK, 10,
				ItemID.BLIGHTED_ENTANGLE_SACK, 50,
				ItemID.BLIGHTED_SURGE_SACK, 100),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertTrue(warnings.isEmpty());
	}

	@Test
	public void warnsMismatchWhenResourcesDoNotMatchSpellbook()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.ANCIENT,
			ImmutableMap.of(),
			ImmutableMap.of(ItemID.BLIGHTED_TELEPORT_SPELL_SACK, 10),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertEquals(1, warnings.size());
		assertWarning(warnings.get(0), MagicSpellbookWarning.WarningPriority.MISMATCH, "Spellbook and runes do not match");
	}

	@Test
	public void lunarWarnsMismatchWhenResourcesDoNotMatchSpellbook()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.LUNAR,
			ImmutableMap.of(),
			ImmutableMap.of(ItemID.BLIGHTED_TELEPORT_SPELL_SACK, 10),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertEquals(1, warnings.size());
		assertWarning(warnings.get(0), MagicSpellbookWarning.WarningPriority.MISMATCH, "Spellbook and runes do not match");
	}

	@Test
	public void nonLunarSpellbooksWarnMismatchForVengeanceSacks()
	{
		List<MagicSpellbookWarning> standardWarnings = evaluateAll(
			MagicSpellbook.STANDARD,
			ImmutableMap.of(),
			ImmutableMap.of(ItemID.BLIGHTED_VENGEANCE_SACK, 10),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);
		List<MagicSpellbookWarning> ancientWarnings = evaluateAll(
			MagicSpellbook.ANCIENT,
			ImmutableMap.of(),
			ImmutableMap.of(ItemID.BLIGHTED_VENGEANCE_SACK, 10),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertEquals(1, standardWarnings.size());
		assertWarning(standardWarnings.get(0), MagicSpellbookWarning.WarningPriority.MISMATCH, "Spellbook and runes do not match");
		assertEquals(1, ancientWarnings.size());
		assertWarning(ancientWarnings.get(0), MagicSpellbookWarning.WarningPriority.MISMATCH, "Spellbook and runes do not match");
	}

	@Test
	public void nonLunarSpellbooksWarnMismatchForVengeanceRunes()
	{
		ImmutableMap<MagicRune, Integer> vengeanceRunes = ImmutableMap.of(
			MagicRune.ASTRAL, 40,
			MagicRune.DEATH, 20,
			MagicRune.EARTH, 100);
		List<MagicSpellbookWarning> standardWarnings = evaluateAll(
			MagicSpellbook.STANDARD,
			vengeanceRunes,
			ImmutableMap.of(),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);
		List<MagicSpellbookWarning> ancientWarnings = evaluateAll(
			MagicSpellbook.ANCIENT,
			vengeanceRunes,
			ImmutableMap.of(),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertEquals(1, standardWarnings.size());
		assertWarning(standardWarnings.get(0), MagicSpellbookWarning.WarningPriority.MISMATCH, "Spellbook and runes do not match");
		assertEquals(1, ancientWarnings.size());
		assertWarning(ancientWarnings.get(0), MagicSpellbookWarning.WarningPriority.MISMATCH, "Spellbook and runes do not match");
	}

	@Test
	public void warnsMissingStandardCategoriesWithOnlyTeleportSacks()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.STANDARD,
			ImmutableMap.of(),
			ImmutableMap.of(ItemID.BLIGHTED_TELEPORT_SPELL_SACK, 10),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertEquals(2, warnings.size());
		assertWarning(warnings.get(0), MagicSpellbookWarning.WarningPriority.MISSING, "Missing runes: damage");
		assertWarning(warnings.get(1), MagicSpellbookWarning.WarningPriority.MISSING, "Missing runes: freeze");
	}

	@Test
	public void warnsMissingStandardCategoriesWithNoRunes()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.STANDARD,
			ImmutableMap.of(),
			ImmutableMap.of(),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertEquals(3, warnings.size());
		assertWarning(warnings.get(0), MagicSpellbookWarning.WarningPriority.MISSING, "Missing runes: Tele Block");
		assertWarning(warnings.get(1), MagicSpellbookWarning.WarningPriority.MISSING, "Missing runes: damage");
		assertWarning(warnings.get(2), MagicSpellbookWarning.WarningPriority.MISSING, "Missing runes: freeze");
	}

	@Test
	public void ancientIceUsesIceThreshold()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.ANCIENT,
			ImmutableMap.of(),
			ImmutableMap.of(ItemID.BLIGHTED_ANCIENT_ICE_SACK, 50),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertEquals(1, warnings.size());
		assertWarning(warnings.get(0), MagicSpellbookWarning.WarningPriority.LOW, "Low casts: ice 50/100");
	}

	@Test
	public void ancientBloodUsesSeparateBloodThreshold()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.ANCIENT,
			ImmutableMap.of(
				MagicRune.DEATH, 100,
				MagicRune.BLOOD, 100,
				MagicRune.SOUL, 25),
			ImmutableMap.of(ItemID.BLIGHTED_ANCIENT_ICE_SACK, 100),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertEquals(1, warnings.size());
		assertWarning(warnings.get(0), MagicSpellbookWarning.WarningPriority.LOW, "Low casts: blood barrage 25/50");
	}

	@Test
	public void lowerBloodSpellsDoNotHideLowBloodBarrage()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.ANCIENT,
			ImmutableMap.of(
				MagicRune.CHAOS, 1_000,
				MagicRune.DEATH, 200,
				MagicRune.BLOOD, 200,
				MagicRune.SOUL, 25),
			ImmutableMap.of(ItemID.BLIGHTED_ANCIENT_ICE_SACK, 100),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertEquals(1, warnings.size());
		assertWarning(warnings.get(0), MagicSpellbookWarning.WarningPriority.LOW, "Low casts: blood barrage 25/50");
	}

	@Test
	public void standardWarnsMismatchForBloodBarrageRunes()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.STANDARD,
			ImmutableMap.of(
				MagicRune.DEATH, 200,
				MagicRune.BLOOD, 200,
				MagicRune.SOUL, 50),
			ImmutableMap.of(),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertEquals(1, warnings.size());
		assertWarning(warnings.get(0), MagicSpellbookWarning.WarningPriority.MISMATCH, "Spellbook and runes do not match");
	}

	@Test
	public void standardWarnsLowTomeOfFireChargesWithOtherFireSource()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.STANDARD,
			ImmutableMap.of(
				MagicRune.AIR, 700,
				MagicRune.WRATH, 100),
			ImmutableMap.of(
				ItemID.BLIGHTED_TELEPORT_SPELL_SACK, 10,
				ItemID.BLIGHTED_ENTANGLE_SACK, 50),
			ImmutableSet.of(MagicRune.FIRE),
			ImmutableMap.of(MagicRune.FIRE, 25),
			false,
			false,
			false,
			false);

		assertEquals(1, warnings.size());
		assertWarning(warnings.get(0), MagicSpellbookWarning.WarningPriority.LOW, "Low charges: tome of fire 25/50");
	}

	@Test
	public void doesNotRequireAncientBloodSpells()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.ANCIENT,
			ImmutableMap.of(),
			ImmutableMap.of(ItemID.BLIGHTED_ANCIENT_ICE_SACK, 100),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertTrue(warnings.isEmpty());
	}

	@Test
	public void lunarAcceptsVengeanceSacks()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.LUNAR,
			ImmutableMap.of(),
			ImmutableMap.of(ItemID.BLIGHTED_VENGEANCE_SACK, 10),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertTrue(warnings.isEmpty());
	}

	@Test
	public void lunarChecksVengeanceRunes()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.LUNAR,
			ImmutableMap.of(
				MagicRune.ASTRAL, 40,
				MagicRune.DEATH, 20,
				MagicRune.EARTH, 100),
			ImmutableMap.of(),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertTrue(warnings.isEmpty());
	}

	@Test
	public void lunarWarnsLowVengeanceCasts()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.LUNAR,
			ImmutableMap.of(
				MagicRune.ASTRAL, 20,
				MagicRune.DEATH, 10,
				MagicRune.EARTH, 50),
			ImmutableMap.of(),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertEquals(1, warnings.size());
		assertWarning(warnings.get(0), MagicSpellbookWarning.WarningPriority.LOW, "Low casts: Vengeance 5/10");
	}

	@Test
	public void lunarWarnsMissingVengeanceRunes()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.LUNAR,
			ImmutableMap.of(),
			ImmutableMap.of(),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertEquals(1, warnings.size());
		assertWarning(warnings.get(0), MagicSpellbookWarning.WarningPriority.MISSING, "Missing runes: Vengeance");
	}

	@Test
	public void standardWithBloodOnlyResourcesEvaluatesStandardMissing()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.STANDARD,
			ImmutableMap.of(MagicRune.BLOOD, 100),
			ImmutableMap.of(),
			ImmutableSet.of(),
			false,
			false,
			false,
			false);

		assertEquals(3, warnings.size());
		assertWarning(warnings.get(0), MagicSpellbookWarning.WarningPriority.MISSING, "Missing runes: Tele Block");
	}

	@Test
	public void ignoresUnchargedAccursedWhenOtherDamageIsValid()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.STANDARD,
			ImmutableMap.of(),
			ImmutableMap.of(
				ItemID.BLIGHTED_TELEPORT_SPELL_SACK, 10,
				ItemID.BLIGHTED_ENTANGLE_SACK, 50,
				ItemID.BLIGHTED_SURGE_SACK, 100),
			ImmutableSet.of(),
			false,
			false,
			false,
			true);

		assertTrue(warnings.isEmpty());
	}

	@Test
	public void treatsUnchargedAccursedAsMissingDamageRunes()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.STANDARD,
			ImmutableMap.of(),
			ImmutableMap.of(
				ItemID.BLIGHTED_TELEPORT_SPELL_SACK, 10,
				ItemID.BLIGHTED_ENTANGLE_SACK, 50),
			ImmutableSet.of(),
			false,
			false,
			false,
			true);

		assertEquals(1, warnings.size());
		assertWarning(warnings.get(0), MagicSpellbookWarning.WarningPriority.MISSING, "Missing runes: damage");
	}

	@Test
	public void magicCapeSuppressesIncompleteLookingSetup()
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(
			MagicSpellbook.STANDARD,
			ImmutableMap.of(),
			ImmutableMap.of(),
			ImmutableSet.of(),
			true,
			false,
			false,
			false);

		assertTrue(warnings.isEmpty());
	}

	@Test
	public void sunfireRunesCountAsFireRunes()
	{
		assertEquals(ImmutableSet.of(MagicRune.FIRE), MagicItemTables.getRuneTypes(ItemID.SUNFIRE_RUNE));
	}

	@Test
	public void onlyMagicCapeAndPlainMaxCapeSuppressWarnings()
	{
		assertTrue(MagicItemTables.isMagicCape(ItemID.MAGIC_CAPE));
		assertTrue(MagicItemTables.isMagicCape(ItemID.MAGIC_CAPET));
		assertTrue(MagicItemTables.isMagicCape(ItemID.MAX_CAPE));
		assertTrue(MagicItemTables.isMagicCape(ItemID.MAX_CAPE_13342));
		assertFalse(MagicItemTables.isMagicCape(ItemID.IMBUED_SARADOMIN_MAX_CAPE));
		assertFalse(MagicItemTables.isMagicCape(ItemID.INFERNAL_MAX_CAPE));
		assertFalse(MagicItemTables.isMagicCape(ItemID.ASSEMBLER_MAX_CAPE));
		assertFalse(MagicItemTables.isMagicCape(ItemID.DIZANAS_MAX_CAPE));
	}

	@Test
	public void recognizesRunePouches()
	{
		assertTrue(MagicItemTables.isRunePouch(ItemID.RUNE_POUCH));
		assertTrue(MagicItemTables.isRunePouch(ItemID.RUNE_POUCH_L));
		assertTrue(MagicItemTables.isRunePouch(ItemID.DIVINE_RUNE_POUCH));
		assertTrue(MagicItemTables.isRunePouch(ItemID.DIVINE_RUNE_POUCH_L));
		assertFalse(MagicItemTables.isRunePouch(ItemID.RUNE_POUCH_NOTE));
		assertFalse(MagicItemTables.isRunePouch(ItemID.LAW_RUNE));
	}

	private List<MagicSpellbookWarning> evaluateAll(
		MagicSpellbook spellbook,
		Map<MagicRune, Integer> runeCounts,
		Map<Integer, Integer> itemCounts,
		Set<MagicRune> providedRunes,
		boolean magicCape,
		boolean validGodStaff,
		boolean chargedWildySceptre,
		boolean unchargedWildySceptre)
	{
		return evaluateAll(
			spellbook,
			runeCounts,
			itemCounts,
			providedRunes,
			ImmutableMap.of(),
			magicCape,
			validGodStaff,
			chargedWildySceptre,
			unchargedWildySceptre);
	}

	private List<MagicSpellbookWarning> evaluateAll(
		MagicSpellbook spellbook,
		Map<MagicRune, Integer> runeCounts,
		Map<Integer, Integer> itemCounts,
		Set<MagicRune> providedRunes,
		Map<MagicRune, Integer> tomeCharges,
		boolean magicCape,
		boolean validGodStaff,
		boolean chargedWildySceptre,
		boolean unchargedWildySceptre)
	{
		return evaluator.evaluateAll(
			MagicSpellbookEvaluator.state(
				spellbook,
				runeCounts,
				itemCounts,
				providedRunes,
				tomeCharges,
				magicCape,
				validGodStaff,
				chargedWildySceptre,
				unchargedWildySceptre),
			thresholds);
	}

	private void assertWarning(
		MagicSpellbookWarning warning,
		MagicSpellbookWarning.WarningPriority priority,
		String text)
	{
		assertEquals(priority, warning.getPriority());
		assertEquals(text, warning.getText());
	}
}
