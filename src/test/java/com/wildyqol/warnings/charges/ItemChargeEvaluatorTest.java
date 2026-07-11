package com.wildyqol.warnings.charges;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import net.runelite.api.ItemID;
import org.junit.Test;

public class ItemChargeEvaluatorTest
{
	private final ItemChargeEvaluator evaluator = new ItemChargeEvaluator();

	@Test
	public void acceptsExactThreshold()
	{
		Optional<ItemChargeWarning> warning = evaluate(
			ImmutableSet.of(ItemChargeKind.BOWFA),
			ImmutableSet.of(),
			charges(ItemChargeKind.BOWFA, 250),
			new TestThresholds());

		assertFalse(warning.isPresent());
	}

	@Test
	public void warnsLowCharges()
	{
		Optional<ItemChargeWarning> warning = evaluate(
			ImmutableSet.of(ItemChargeKind.BOWFA),
			ImmutableSet.of(),
			charges(ItemChargeKind.BOWFA, 80),
			new TestThresholds());

		assertWarning(warning, ItemChargeWarning.WarningPriority.LOW, "Low charges: Bowfa 80/250");
	}

	@Test
	public void chargedBowfaWithoutKnownChargesPromptsTracking()
	{
		Optional<ItemChargeWarning> warning = evaluate(
			ImmutableSet.of(ItemChargeKind.BOWFA),
			ImmutableSet.of(),
			charges(),
			new TestThresholds());

		assertWarning(
			warning,
			ItemChargeWarning.WarningPriority.UNKNOWN,
			"Unknown charges: check Bowfa to start tracking");
	}

	@Test
	public void warnsMissingCharges()
	{
		Optional<ItemChargeWarning> warning = evaluate(
			ImmutableSet.of(),
			ImmutableSet.of(ItemChargeKind.TOXIC_STAFF),
			charges(),
			new TestThresholds());

		assertWarning(warning, ItemChargeWarning.WarningPriority.MISSING, "No charges: toxic SOTD");
	}

	@Test
	public void chargedCopyTakesPriorityOverUnchargedCopy()
	{
		Optional<ItemChargeWarning> warning = evaluate(
			ImmutableSet.of(ItemChargeKind.TOXIC_STAFF),
			ImmutableSet.of(ItemChargeKind.TOXIC_STAFF),
			charges(ItemChargeKind.TOXIC_STAFF, 300),
			new TestThresholds());

		assertWarning(warning, ItemChargeWarning.WarningPriority.LOW, "Low charges: toxic SOTD ~300/500");
	}

	@Test
	public void disabledThresholdSuppressesLowWarning()
	{
		TestThresholds thresholds = new TestThresholds();
		thresholds.bowfaCharges = 0;

		Optional<ItemChargeWarning> warning = evaluate(
			ImmutableSet.of(ItemChargeKind.BOWFA),
			ImmutableSet.of(),
			charges(ItemChargeKind.BOWFA, 0),
			thresholds);

		assertFalse(warning.isPresent());
	}

	@Test
	public void problemChargeItemsDoNotWarnLowCharges()
	{
		List<ItemChargeWarning> warnings = evaluateAll(
			ImmutableSet.of(ItemChargeKind.CRAWS_WEBWEAVER),
			ImmutableSet.of(),
			charges(ItemChargeKind.CRAWS_WEBWEAVER, 0),
			new TestThresholds());

		assertTrue(warnings.isEmpty());
	}

	@Test
	public void chargedTomeOfFireWithTrackedZeroChargesWarns()
	{
		Optional<ItemChargeWarning> warning = evaluate(
			ImmutableSet.of(ItemChargeKind.TOME_OF_FIRE),
			ImmutableSet.of(),
			charges(ItemChargeKind.TOME_OF_FIRE, 0),
			new TestThresholds());

		assertWarning(warning, ItemChargeWarning.WarningPriority.LOW, "Low charges: tome of fire ~0/50");
	}

	@Test
	public void chargedTomeOfFireWithTrackedLowChargesWarns()
	{
		Optional<ItemChargeWarning> warning = evaluate(
			ImmutableSet.of(ItemChargeKind.TOME_OF_FIRE),
			ImmutableSet.of(),
			charges(ItemChargeKind.TOME_OF_FIRE, 25),
			new TestThresholds());

		assertWarning(warning, ItemChargeWarning.WarningPriority.LOW, "Low charges: tome of fire ~25/50");
	}

	@Test
	public void chargedTomeOfFireWithoutKnownChargesPromptsTracking()
	{
		Optional<ItemChargeWarning> warning = evaluate(
			ImmutableSet.of(ItemChargeKind.TOME_OF_FIRE),
			ImmutableSet.of(),
			charges(),
			new TestThresholds());

		assertWarning(
			warning,
			ItemChargeWarning.WarningPriority.UNKNOWN,
			"Unknown charges: check tome of fire to start tracking");
	}

	@Test
	public void multipleUnknownTrackedChargesCollapseToSingleWarning()
	{
		List<ItemChargeWarning> warnings = evaluateAll(
			ImmutableSet.of(
				ItemChargeKind.BOWFA,
				ItemChargeKind.TOME_OF_FIRE,
				ItemChargeKind.TOXIC_STAFF,
				ItemChargeKind.SERPENTINE_HELM),
			ImmutableSet.of(),
			charges(),
			new TestThresholds());

		assertEquals(1, warnings.size());
		assertWarning(
			warnings.get(0),
			ItemChargeWarning.WarningPriority.UNKNOWN,
			"Unknown charges: check Bowfa, serpentine helm, toxic SOTD, tome of fire to start tracking");
	}

	@Test
	public void disabledThresholdSuppressesUnknownTrackingWarning()
	{
		TestThresholds thresholds = new TestThresholds();
		thresholds.tomeCharges = 0;

		Optional<ItemChargeWarning> warning = evaluate(
			ImmutableSet.of(ItemChargeKind.TOME_OF_FIRE),
			ImmutableSet.of(),
			charges(),
			thresholds);

		assertFalse(warning.isPresent());
	}

	@Test
	public void chargedToxicStaffWithTrackedLowChargesWarns()
	{
		Optional<ItemChargeWarning> warning = evaluate(
			ImmutableSet.of(ItemChargeKind.TOXIC_STAFF),
			ImmutableSet.of(),
			charges(ItemChargeKind.TOXIC_STAFF, 300),
			new TestThresholds());

		assertWarning(warning, ItemChargeWarning.WarningPriority.LOW, "Low charges: toxic SOTD ~300/500");
	}

	@Test
	public void chargedSerpentineHelmWithTrackedLowChargesWarns()
	{
		Optional<ItemChargeWarning> warning = evaluate(
			ImmutableSet.of(ItemChargeKind.SERPENTINE_HELM),
			ImmutableSet.of(),
			charges(ItemChargeKind.SERPENTINE_HELM, 300),
			new TestThresholds());

		assertWarning(warning, ItemChargeWarning.WarningPriority.LOW, "Low charges: serpentine helm ~300/500");
	}

	@Test
	public void returnsSortedWarnings()
	{
		List<ItemChargeWarning> warnings = evaluateAll(
			ImmutableSet.of(ItemChargeKind.BOWFA),
			ImmutableSet.of(ItemChargeKind.TOME_OF_FIRE),
			charges(ItemChargeKind.BOWFA, 80),
			new TestThresholds());

		assertEquals(2, warnings.size());
		assertWarning(warnings.get(0), ItemChargeWarning.WarningPriority.MISSING, "No charges: tome of fire");
		assertWarning(warnings.get(1), ItemChargeWarning.WarningPriority.LOW, "Low charges: Bowfa 80/250");
	}

	@Test
	public void mapsTrackedItemVariants()
	{
		assertEquals(ItemChargeKind.BOWFA, ItemChargeTables.getChargedKind(ItemID.BOW_OF_FAERDHINEN));
		assertEquals(ItemChargeKind.BOWFA, ItemChargeTables.getUnchargedKind(ItemID.BOW_OF_FAERDHINEN_INACTIVE));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.BOW_OF_FAERDHINEN));
		assertNull(ItemChargeTables.getChargedKind(ItemID.BOW_OF_FAERDHINEN_INACTIVE));
		assertTrue(ItemChargeTables.isIgnoredBowfa(ItemID.BOW_OF_FAERDHINEN_C));

		assertEquals(ItemChargeKind.SERPENTINE_HELM, ItemChargeTables.getChargedKind(ItemID.SERPENTINE_HELM));
		assertEquals(ItemChargeKind.SERPENTINE_HELM, ItemChargeTables.getChargedKind(ItemID.TANZANITE_HELM));
		assertEquals(ItemChargeKind.SERPENTINE_HELM, ItemChargeTables.getChargedKind(ItemID.MAGMA_HELM));
		assertEquals(ItemChargeKind.SERPENTINE_HELM, ItemChargeTables.getUnchargedKind(ItemID.SERPENTINE_HELM_UNCHARGED));
		assertEquals(ItemChargeKind.SERPENTINE_HELM, ItemChargeTables.getUnchargedKind(ItemID.TANZANITE_HELM_UNCHARGED));
		assertEquals(ItemChargeKind.SERPENTINE_HELM, ItemChargeTables.getUnchargedKind(ItemID.MAGMA_HELM_UNCHARGED));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.SERPENTINE_HELM));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.TANZANITE_HELM));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.MAGMA_HELM));
		assertNull(ItemChargeTables.getChargedKind(ItemID.SERPENTINE_HELM_UNCHARGED));
		assertNull(ItemChargeTables.getChargedKind(ItemID.TANZANITE_HELM_UNCHARGED));
		assertNull(ItemChargeTables.getChargedKind(ItemID.MAGMA_HELM_UNCHARGED));

		assertEquals(ItemChargeKind.TOXIC_STAFF, ItemChargeTables.getChargedKind(ItemID.TOXIC_STAFF_OF_THE_DEAD));
		assertEquals(ItemChargeKind.TOXIC_STAFF, ItemChargeTables.getChargedKind(ItemID.TOXIC_STAFF_DEADMAN));
		assertEquals(ItemChargeKind.TOXIC_STAFF, ItemChargeTables.getUnchargedKind(ItemID.TOXIC_STAFF_UNCHARGED));
		assertEquals(ItemChargeKind.TOXIC_STAFF, ItemChargeTables.getUnchargedKind(ItemID.TOXIC_STAFF_UNCHARGED_33035));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.TOXIC_STAFF_OF_THE_DEAD));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.TOXIC_STAFF_DEADMAN));
		assertNull(ItemChargeTables.getChargedKind(ItemID.TOXIC_STAFF_UNCHARGED));
		assertNull(ItemChargeTables.getChargedKind(ItemID.TOXIC_STAFF_UNCHARGED_33035));

		assertEquals(ItemChargeKind.ACCURSED_THAMMARONS, ItemChargeTables.getChargedKind(ItemID.THAMMARONS_SCEPTRE));
		assertEquals(ItemChargeKind.ACCURSED_THAMMARONS, ItemChargeTables.getChargedKind(ItemID.ACCURSED_SCEPTRE_A));
		assertEquals(ItemChargeKind.ACCURSED_THAMMARONS, ItemChargeTables.getUnchargedKind(ItemID.THAMMARONS_SCEPTRE_U));
		assertEquals(ItemChargeKind.ACCURSED_THAMMARONS, ItemChargeTables.getUnchargedKind(ItemID.ACCURSED_SCEPTRE_AU));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.THAMMARONS_SCEPTRE));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.ACCURSED_SCEPTRE_A));
		assertNull(ItemChargeTables.getChargedKind(ItemID.THAMMARONS_SCEPTRE_U));
		assertNull(ItemChargeTables.getChargedKind(ItemID.ACCURSED_SCEPTRE_AU));
		assertEquals(ItemChargeKind.CRAWS_WEBWEAVER, ItemChargeTables.getChargedKind(ItemID.CRAWS_BOW));
		assertEquals(ItemChargeKind.CRAWS_WEBWEAVER, ItemChargeTables.getChargedKind(ItemID.WEBWEAVER_BOW));
		assertEquals(ItemChargeKind.CRAWS_WEBWEAVER, ItemChargeTables.getUnchargedKind(ItemID.CRAWS_BOW_U));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.CRAWS_BOW));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.WEBWEAVER_BOW));
		assertNull(ItemChargeTables.getChargedKind(ItemID.CRAWS_BOW_U));
		assertEquals(ItemChargeKind.URSINE_VIGGORAS, ItemChargeTables.getChargedKind(ItemID.VIGGORAS_CHAINMACE));
		assertEquals(ItemChargeKind.URSINE_VIGGORAS, ItemChargeTables.getChargedKind(ItemID.URSINE_CHAINMACE));
		assertEquals(ItemChargeKind.URSINE_VIGGORAS, ItemChargeTables.getUnchargedKind(ItemID.URSINE_CHAINMACE_U));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.VIGGORAS_CHAINMACE));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.URSINE_CHAINMACE));
		assertNull(ItemChargeTables.getChargedKind(ItemID.URSINE_CHAINMACE_U));
		assertEquals(ItemChargeKind.RING_OF_SUFFERING, ItemChargeTables.getChargedKind(ItemID.RING_OF_SUFFERING_R));
		assertEquals(ItemChargeKind.RING_OF_SUFFERING, ItemChargeTables.getChargedKind(ItemID.RING_OF_SUFFERING_RI));
		assertEquals(ItemChargeKind.RING_OF_SUFFERING, ItemChargeTables.getChargedKind(ItemID.RING_OF_SUFFERING_RI_25248));
		assertEquals(ItemChargeKind.RING_OF_SUFFERING, ItemChargeTables.getChargedKind(ItemID.RING_OF_SUFFERING_RI_26762));
		assertEquals(ItemChargeKind.RING_OF_SUFFERING, ItemChargeTables.getUnchargedKind(ItemID.RING_OF_SUFFERING));
		assertEquals(ItemChargeKind.RING_OF_SUFFERING, ItemChargeTables.getUnchargedKind(ItemID.RING_OF_SUFFERING_I));
		assertEquals(ItemChargeKind.RING_OF_SUFFERING, ItemChargeTables.getUnchargedKind(ItemID.RING_OF_SUFFERING_I_25246));
		assertEquals(ItemChargeKind.RING_OF_SUFFERING, ItemChargeTables.getUnchargedKind(ItemID.RING_OF_SUFFERING_I_26761));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.RING_OF_SUFFERING_R));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.RING_OF_SUFFERING_RI));
		assertNull(ItemChargeTables.getChargedKind(ItemID.RING_OF_SUFFERING));
		assertNull(ItemChargeTables.getChargedKind(ItemID.RING_OF_SUFFERING_I));

		assertEquals(ItemChargeKind.TOME_OF_FIRE, ItemChargeTables.getChargedKind(ItemID.TOME_OF_FIRE));
		assertEquals(ItemChargeKind.TOME_OF_FIRE, ItemChargeTables.getChargedKind(ItemID.TOME_OF_FIRE_27358));
		assertEquals(ItemChargeKind.TOME_OF_FIRE, ItemChargeTables.getUnchargedKind(ItemID.TOME_OF_FIRE_EMPTY));
		assertEquals(ItemChargeKind.TOME_OF_WATER, ItemChargeTables.getChargedKind(ItemID.TOME_OF_WATER));
		assertEquals(ItemChargeKind.TOME_OF_WATER, ItemChargeTables.getUnchargedKind(ItemID.TOME_OF_WATER_EMPTY));
		assertEquals(ItemChargeKind.TOME_OF_EARTH, ItemChargeTables.getChargedKind(ItemID.TOME_OF_EARTH));
		assertEquals(ItemChargeKind.TOME_OF_EARTH, ItemChargeTables.getUnchargedKind(ItemID.TOME_OF_EARTH_EMPTY));

		assertEquals(ItemChargeKind.DRAGONFIRE_SHIELD, ItemChargeTables.getUnchargedKind(ItemID.DRAGONFIRE_SHIELD_11284));
		assertEquals(ItemChargeKind.DRAGONFIRE_WARD, ItemChargeTables.getUnchargedKind(ItemID.DRAGONFIRE_WARD_22003));
		assertEquals(ItemChargeKind.ANCIENT_WYVERN_SHIELD, ItemChargeTables.getUnchargedKind(ItemID.ANCIENT_WYVERN_SHIELD_21634));
		assertNull(ItemChargeTables.getChargedKind(ItemID.DRAGONFIRE_SHIELD));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.DRAGONFIRE_SHIELD));
		assertNull(ItemChargeTables.getUnchargedKind(ItemID.TOME_OF_FIRE));
	}

	private Optional<ItemChargeWarning> evaluate(
		ImmutableSet<ItemChargeKind> charged,
		ImmutableSet<ItemChargeKind> uncharged,
		EnumMap<ItemChargeKind, Integer> charges,
		ItemChargeThresholds thresholds)
	{
		return evaluator.evaluate(ItemChargeEvaluator.state(charged, uncharged, charges), thresholds);
	}

	private List<ItemChargeWarning> evaluateAll(
		ImmutableSet<ItemChargeKind> charged,
		ImmutableSet<ItemChargeKind> uncharged,
		EnumMap<ItemChargeKind, Integer> charges,
		ItemChargeThresholds thresholds)
	{
		return evaluator.evaluateAll(ItemChargeEvaluator.state(charged, uncharged, charges), thresholds);
	}

	private EnumMap<ItemChargeKind, Integer> charges()
	{
		return new EnumMap<>(ItemChargeKind.class);
	}

	private EnumMap<ItemChargeKind, Integer> charges(ItemChargeKind kind, int quantity)
	{
		EnumMap<ItemChargeKind, Integer> charges = charges();
		charges.put(kind, quantity);
		return charges;
	}

	private void assertWarning(
		Optional<ItemChargeWarning> warning,
		ItemChargeWarning.WarningPriority priority,
		String text)
	{
		assertTrue(warning.isPresent());
		assertWarning(warning.get(), priority, text);
	}

	private void assertWarning(
		ItemChargeWarning warning,
		ItemChargeWarning.WarningPriority priority,
		String text)
	{
		assertEquals(priority, warning.getPriority());
		assertEquals(text, warning.getText());
	}

	private static class TestThresholds implements ItemChargeThresholds
	{
		private int bowfaCharges = 250;
		private int tomeCharges = 50;
		private int toxicStaffCharges = 500;
		private int serpentineHelmCharges = 500;

		@Override
		public int bowfaCharges()
		{
			return bowfaCharges;
		}

		@Override
		public int tomeCharges()
		{
			return tomeCharges;
		}

		@Override
		public int toxicStaffCharges()
		{
			return toxicStaffCharges;
		}

		@Override
		public int serpentineHelmCharges()
		{
			return serpentineHelmCharges;
		}
	}
}
