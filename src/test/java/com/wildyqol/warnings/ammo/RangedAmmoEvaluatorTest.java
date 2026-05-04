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

		@Override
		public int bowfaCharges()
		{
			return 250;
		}
	};

	@Test
	public void acceptsAtlatlDarts()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.ATLATL_DARTS),
			ImmutableMap.of(ItemID.ATLATL_DART, 250),
			false,
			false,
			0);

		assertFalse(warning.isPresent());
	}

	@Test
	public void acceptsRuneCrossbowPvpBoltSet()
	{
		assertFalse(evaluate(
			ImmutableSet.of(RangedAmmoRequirement.RUNE_BOLTS),
			ImmutableMap.of(ItemID.RUNITE_BOLTS, 100),
			false,
			false,
			0).isPresent());
		assertFalse(evaluate(
			ImmutableSet.of(RangedAmmoRequirement.RUNE_BOLTS),
			ImmutableMap.of(ItemID.ONYX_BOLTS_E, 100),
			false,
			false,
			0).isPresent());
	}

	@Test
	public void acceptsDragonCrossbowPvpBoltSet()
	{
		assertFalse(evaluate(
			ImmutableSet.of(RangedAmmoRequirement.DRAGON_BOLTS),
			ImmutableMap.of(ItemID.ONYX_DRAGON_BOLTS_E, 100),
			false,
			false,
			0).isPresent());
		assertFalse(evaluate(
			ImmutableSet.of(RangedAmmoRequirement.DRAGON_BOLTS),
			ImmutableMap.of(ItemID.ONYX_BOLTS_E, 100),
			false,
			false,
			0).isPresent());
	}

	@Test
	public void warnsMissingAmmo()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.DRAGON_ARROWS),
			ImmutableMap.of(),
			false,
			false,
			0);

		assertWarning(warning, RangedAmmoWarning.WarningPriority.MISSING, "Missing ammo: dragon arrows");
	}

	@Test
	public void warnsWrongAmmo()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.DRAGON_BOLTS),
			ImmutableMap.of(ItemID.DRAGON_ARROW, 100),
			false,
			false,
			0);

		assertWarning(warning, RangedAmmoWarning.WarningPriority.WRONG, "Wrong ammo: dragon crossbow bolts required");
	}

	@Test
	public void warnsLowAmmoOncePerRequirement()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.RUNE_BOLTS),
			ImmutableMap.of(ItemID.DIAMOND_BOLTS_E, 42),
			false,
			false,
			0);

		assertWarning(warning, RangedAmmoWarning.WarningPriority.LOW, "Low bolts: 42/100");
	}

	@Test
	public void checksDifferentRequirementsIndependently()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(RangedAmmoRequirement.RUNE_BOLTS, RangedAmmoRequirement.DRAGON_ARROWS),
			ImmutableMap.of(ItemID.DIAMOND_BOLTS_E, 100),
			false,
			false,
			0);

		assertWarning(warning, RangedAmmoWarning.WarningPriority.MISSING, "Missing ammo: dragon arrows");
	}

	@Test
	public void returnsAllWarnings()
	{
		List<RangedAmmoWarning> warnings = evaluateAll(
			ImmutableSet.of(RangedAmmoRequirement.RUNE_BOLTS, RangedAmmoRequirement.DRAGON_ARROWS),
			ImmutableMap.of(ItemID.DIAMOND_BOLTS_E, 42),
			false,
			false,
			0);

		assertEquals(2, warnings.size());
		assertEquals("Missing ammo: dragon arrows", warnings.get(0).getText());
		assertEquals("Low bolts: 42/100", warnings.get(1).getText());
	}

	@Test
	public void acceptsBowfaWithEnoughCharges()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(),
			ImmutableMap.of(),
			true,
			false,
			250);

		assertFalse(warning.isPresent());
	}

	@Test
	public void warnsLowBowfaCharges()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(),
			ImmutableMap.of(),
			true,
			false,
			80);

		assertWarning(warning, RangedAmmoWarning.WarningPriority.LOW, "Low Bowfa charges: 80/250");
	}

	@Test
	public void warnsInactiveBowfa()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(),
			ImmutableMap.of(),
			false,
			true,
			0);

		assertWarning(warning, RangedAmmoWarning.WarningPriority.MISSING, "Missing Bowfa charges");
	}

	@Test
	public void ignoresUnknownWeaponsByReceivingNoRequirements()
	{
		Optional<RangedAmmoWarning> warning = evaluate(
			ImmutableSet.of(),
			ImmutableMap.of(ItemID.DRAGON_ARROW, 10),
			false,
			false,
			0);

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
		assertTrue(RangedAmmoTables.isQuiver(net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_INFINITE));
		assertTrue(RangedAmmoTables.isQuiver(net.runelite.api.gameval.ItemID.SKILLCAPE_MAX_DIZANAS));
		assertTrue(RangedAmmoTables.isBowfaWithCharges(ItemID.BOW_OF_FAERDHINEN));
		assertTrue(RangedAmmoTables.isInactiveBowfa(ItemID.BOW_OF_FAERDHINEN_INACTIVE));
		assertTrue(RangedAmmoTables.isCorruptedBowfa(ItemID.BOW_OF_FAERDHINEN_C));
		assertTrue(RangedAmmoTables.isSupportedAmmo(ItemID.DRAGON_ARROW));
		assertFalse(RangedAmmoTables.isSupportedAmmo(ItemID.ANGLERFISH));
	}

	private Optional<RangedAmmoWarning> evaluate(
		ImmutableSet<RangedAmmoRequirement> requirements,
		ImmutableMap<Integer, Integer> ammoCounts,
		boolean chargedBowfa,
		boolean inactiveBowfa,
		int bowfaCharges)
	{
		return evaluator.evaluate(
			RangedAmmoEvaluator.state(requirements, ammoCounts, chargedBowfa, inactiveBowfa, bowfaCharges),
			thresholds);
	}

	private List<RangedAmmoWarning> evaluateAll(
		ImmutableSet<RangedAmmoRequirement> requirements,
		ImmutableMap<Integer, Integer> ammoCounts,
		boolean chargedBowfa,
		boolean inactiveBowfa,
		int bowfaCharges)
	{
		return evaluator.evaluateAll(
			RangedAmmoEvaluator.state(requirements, ammoCounts, chargedBowfa, inactiveBowfa, bowfaCharges),
			thresholds);
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
