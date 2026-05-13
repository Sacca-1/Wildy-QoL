package com.wildyqol.warnings.ammo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import net.runelite.api.ItemID;
import org.junit.Test;

public class RangedAmmoEvaluatorTest
{
	private final RangedAmmoEvaluator evaluator = new RangedAmmoEvaluator();
	private final AmmoThresholds thresholds = new AmmoThresholds()
	{
		@Override
		public int atlatlDarts()
		{
			return 250;
		}

		@Override
		public int bolts()
		{
			return 100;
		}

		@Override
		public int javelins()
		{
			return 100;
		}

		@Override
		public int arrows()
		{
			return 100;
		}
	};

	@Test
	public void acceptsAtlatlDarts()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.ATLATL_DARTS),
			ImmutableMap.of(ItemID.ATLATL_DART, 250));

		assertFalse(warning.isPresent());
	}

	@Test
	public void acceptsRuneCrossbowPvpBoltSet()
	{
		assertFalse(evaluate(
			ImmutableSet.of(RangedAmmoRequirement.RUNE_BOLTS),
			ImmutableMap.of(ItemID.RUNITE_BOLTS, 100)).isPresent());
		assertFalse(evaluate(
			ImmutableSet.of(RangedAmmoRequirement.RUNE_BOLTS),
			ImmutableMap.of(ItemID.ONYX_BOLTS_E, 100)).isPresent());
	}

	@Test
	public void warnsForUnenchantedOptimalGemBolts()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.RUNE_BOLTS),
			ImmutableMap.of(ItemID.ONYX_BOLTS, 100));

		assertWarning(warning, RangedAmmoWarning.WarningPriority.SUBOPTIMAL,
			"Suboptimal ammo: Onyx bolts");
	}

	@Test
	public void acceptsSuboptimalRuneCrossbowBolts()
	{
		List<RangedAmmoWarning> warnings = evaluateAll(
			ImmutableSet.of(RangedAmmoRequirement.RUNE_BOLTS),
			ImmutableMap.of(ItemID.BRONZE_BOLTS, 100),
			false);

		assertTrue(warnings.isEmpty());
	}

	@Test
	public void warnsSuboptimalRuneCrossbowBolts()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.RUNE_BOLTS),
			ImmutableMap.of(ItemID.BRONZE_BOLTS_P_6061, 100));

		assertWarning(warning, RangedAmmoWarning.WarningPriority.SUBOPTIMAL,
			"Suboptimal ammo: Bronze bolts");
	}

	@Test
	public void acceptsDragonCrossbowPvpBoltSet()
	{
		assertFalse(evaluate(
			ImmutableSet.of(RangedAmmoRequirement.DRAGON_BOLTS),
			ImmutableMap.of(ItemID.ONYX_DRAGON_BOLTS_E, 100)).isPresent());
		assertFalse(evaluate(
			ImmutableSet.of(RangedAmmoRequirement.DRAGON_BOLTS),
			ImmutableMap.of(ItemID.ONYX_BOLTS_E, 100)).isPresent());
	}

	@Test
	public void warnsForUnenchantedOptimalDragonGemBolts()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.DRAGON_BOLTS),
			ImmutableMap.of(ItemID.OPAL_DRAGON_BOLTS, 100));

		assertWarning(warning, RangedAmmoWarning.WarningPriority.SUBOPTIMAL,
			"Suboptimal ammo: Opal dragon bolts");
	}

	@Test
	public void warnsSuboptimalDragonCrossbowBolts()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.DRAGON_BOLTS),
			ImmutableMap.of(ItemID.RUBY_DRAGON_BOLTS_E, 100));

		assertWarning(warning, RangedAmmoWarning.WarningPriority.SUBOPTIMAL,
			"Suboptimal ammo: Ruby dragon bolts");
	}

	@Test
	public void warnsWhenRuneCrossbowHasDragonOnlyBolts()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.RUNE_BOLTS),
			ImmutableMap.of(ItemID.DRAGON_BOLTS, 100));

		assertWarning(warning, RangedAmmoWarning.WarningPriority.WRONG, "Wrong ammo: rune crossbow bolts required");
	}

	@Test
	public void warnsSuboptimalDragonArrowBowAmmo()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.DRAGON_ARROWS),
			ImmutableMap.of(ItemID.RUNE_ARROW, 100));

		assertWarning(warning, RangedAmmoWarning.WarningPriority.SUBOPTIMAL,
			"Suboptimal ammo: Rune arrows");
	}

	@Test
	public void acceptsDragonArrowVariantsAsOptimal()
	{
		assertFalse(evaluate(
			ImmutableSet.of(RangedAmmoRequirement.DRAGON_ARROWS),
			ImmutableMap.of(ItemID.DRAGON_ARROWP_11228, 100)).isPresent());
	}

	@Test
	public void warnsMissingAmmo()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.DRAGON_ARROWS),
			ImmutableMap.of());

		assertWarning(warning, RangedAmmoWarning.WarningPriority.MISSING, "Missing ammo: dragon arrows");
	}

	@Test
	public void warnsWrongAmmo()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.DRAGON_BOLTS),
			ImmutableMap.of(ItemID.DRAGON_ARROW, 100));

		assertWarning(warning, RangedAmmoWarning.WarningPriority.WRONG, "Wrong ammo: dragon crossbow bolts required");
	}

	@Test
	public void warnsLowAmmoOncePerRequirement()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.RUNE_BOLTS),
			ImmutableMap.of(ItemID.DIAMOND_BOLTS_E, 42));

		assertWarning(warning, RangedAmmoWarning.WarningPriority.LOW, "Low ammo: bolts 42/100");
	}

	@Test
	public void checksDifferentRequirementsIndependently()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.RUNE_BOLTS, RangedAmmoRequirement.DRAGON_ARROWS),
			ImmutableMap.of(ItemID.DIAMOND_BOLTS_E, 100));

		assertWarning(warning, RangedAmmoWarning.WarningPriority.MISSING, "Missing ammo: dragon arrows");
	}

	@Test
	public void returnsAllWarnings()
	{
		List<RangedAmmoWarning> warnings = evaluateAll(
			ImmutableSet.of(RangedAmmoRequirement.RUNE_BOLTS, RangedAmmoRequirement.DRAGON_ARROWS),
			ImmutableMap.of(ItemID.DIAMOND_BOLTS_E, 42));

		assertEquals(2, warnings.size());
		assertEquals("Missing ammo: dragon arrows", warnings.get(0).getText());
		assertEquals("Low ammo: bolts 42/100", warnings.get(1).getText());
	}

	@Test
	public void ignoresUnknownWeaponsByReceivingNoRequirements()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(),
			ImmutableMap.of(ItemID.DRAGON_ARROW, 10));

		assertFalse(warning.isPresent());
	}

	@Test
	public void mapsInitialWeaponTable()
	{
		assertEquals(RangedAmmoRequirement.ATLATL_DARTS, RangedAmmoTables.getRequirement(ItemID.ECLIPSE_ATLATL));
		assertEquals(RangedAmmoRequirement.RUNE_BOLTS, RangedAmmoTables.getRequirement(ItemID.RUNE_CROSSBOW));
		assertEquals(RangedAmmoRequirement.DRAGON_BOLTS, RangedAmmoTables.getRequirement(ItemID.ZARYTE_CROSSBOW));
		assertEquals(RangedAmmoRequirement.ANTLER_BOLTS, RangedAmmoTables.getRequirement(ItemID.HUNTERS_SUNLIGHT_CROSSBOW));
		assertEquals(RangedAmmoRequirement.DRAGON_JAVELINS, RangedAmmoTables.getRequirement(ItemID.HEAVY_BALLISTA));
		assertEquals(RangedAmmoRequirement.DRAGON_ARROWS, RangedAmmoTables.getRequirement(ItemID.SCORCHING_BOW));
		assertTrue(RangedAmmoTables.isQuiver(net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_CHARGED));
		assertTrue(RangedAmmoTables.isQuiver(net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_CHARGED_TROUVER));
		assertTrue(RangedAmmoTables.isQuiver(net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_INFINITE));
		assertTrue(RangedAmmoTables.isQuiver(net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_INFINITE_TROUVER));
		assertTrue(RangedAmmoTables.isQuiver(net.runelite.api.gameval.ItemID.SKILLCAPE_MAX_DIZANAS));
		assertTrue(RangedAmmoTables.isQuiver(net.runelite.api.gameval.ItemID.SKILLCAPE_MAX_DIZANAS_TROUVER));
		assertTrue(RangedAmmoTables.isSupportedAmmo(ItemID.DRAGON_ARROW));
		assertTrue(RangedAmmoTables.isSupportedAmmo(ItemID.BRONZE_BOLTS));
		assertTrue(RangedAmmoTables.isSupportedAmmo(ItemID.BRONZE_BOLTS_P_6061));
		assertTrue(RangedAmmoTables.isSupportedAmmo(ItemID.RUNE_ARROW));
		assertTrue(RangedAmmoTables.isSupportedAmmo(ItemID.RUNE_ARROWP_5621));
		assertTrue(RangedAmmoTables.isSupportedAmmo(ItemID.BROAD_BOLTS));
		assertFalse(RangedAmmoTables.isSupportedAmmo(ItemID.ICE_ARROWS));
		assertFalse(RangedAmmoTables.isSupportedAmmo(ItemID.OGRE_ARROW));
		assertFalse(RangedAmmoTables.isSupportedAmmo(ItemID.KEBBIT_BOLTS));
		assertFalse(RangedAmmoTables.isSupportedAmmo(ItemID.BARBED_BOLTS));
		assertFalse(RangedAmmoTables.isSupportedAmmo(ItemID.BARBED_ARROW));
		assertFalse(RangedAmmoTables.isSupportedAmmo(ItemID.ANGLERFISH));
	}

	private Optional<RangedAmmoWarning> evaluate(
		ImmutableSet<RangedAmmoRequirement> requirements,
		ImmutableMap<Integer, Integer> ammoCounts)
	{
		return evaluator.evaluate(
			RangedAmmoEvaluator.state(requirements, ammoCounts),
			thresholds);
	}

	private List<RangedAmmoWarning> evaluateAll(
		ImmutableSet<RangedAmmoRequirement> requirements,
		ImmutableMap<Integer, Integer> ammoCounts)
	{
		return evaluateAll(requirements, ammoCounts, true);
	}

	private List<RangedAmmoWarning> evaluateAll(
		ImmutableSet<RangedAmmoRequirement> requirements,
		ImmutableMap<Integer, Integer> ammoCounts,
		boolean warnSuboptimalAmmo)
	{
		return evaluator.evaluateAll(
			RangedAmmoEvaluator.state(requirements, ammoCounts),
			thresholds,
			warnSuboptimalAmmo);
	}

	private void assertWarning(
		Optional<RangedAmmoWarning> warning,
		RangedAmmoWarning.WarningPriority priority,
		String text)
	{
		assertTrue(warning.isPresent());
		assertEquals(priority, warning.get().getPriority());
		assertEquals(text, warning.get().getText());
	}
}
