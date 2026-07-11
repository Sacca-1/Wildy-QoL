package com.wildyqol.warnings.charges;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ItemChargeEvaluator
{
	public Optional<ItemChargeWarning> evaluate(ItemChargeState state, ItemChargeThresholds thresholds)
	{
		List<ItemChargeWarning> warnings = evaluateAll(state, thresholds);
		return warnings.isEmpty() ? Optional.empty() : Optional.of(warnings.get(0));
	}

	public List<ItemChargeWarning> evaluateAll(ItemChargeState state, ItemChargeThresholds thresholds)
	{
		List<ItemChargeWarning> warnings = new ArrayList<>();
		List<String> unknownTrackedChargeItems = new ArrayList<>();
		for (ItemChargeKind kind : ItemChargeKind.values())
		{
			if (state.getUnchargedItems().contains(kind) && !state.getChargedItems().contains(kind))
			{
				warnings.add(missing("No charges: " + kind.getMissingText()));
				continue;
			}

			if (!kind.supportsLowWarning() || !state.getChargedItems().contains(kind))
			{
				continue;
			}

			int threshold = kind.threshold(thresholds);
			Integer charges = state.getCharges().get(kind);
			if (charges == null)
			{
				if (threshold > 0 && kind.requiresManualTracking())
				{
					unknownTrackedChargeItems.add(kind.getLowText());
				}
				continue;
			}

			if (threshold > 0 && charges < threshold)
			{
				String chargeText = kind.hasEstimatedCharges() ? "~" + charges : Integer.toString(charges);
				warnings.add(low("Low charges: " + kind.getLowText() + " " + chargeText + "/" + threshold));
			}
		}

		if (!unknownTrackedChargeItems.isEmpty())
		{
			warnings.add(unknown("Unknown charges: check "
				+ String.join(", ", unknownTrackedChargeItems)
				+ " to start tracking"));
		}

		warnings.sort((left, right) ->
		{
			int priority = left.getPriority().compareTo(right.getPriority());
			return priority != 0 ? priority : left.getText().compareTo(right.getText());
		});
		return ImmutableList.copyOf(warnings);
	}

	public static ItemChargeState state(
		Set<ItemChargeKind> chargedItems,
		Set<ItemChargeKind> unchargedItems,
		EnumMap<ItemChargeKind, Integer> charges)
	{
		EnumMap<ItemChargeKind, ItemChargeKind> orderedCharged = new EnumMap<>(ItemChargeKind.class);
		for (ItemChargeKind kind : chargedItems)
		{
			orderedCharged.put(kind, kind);
		}

		EnumMap<ItemChargeKind, ItemChargeKind> orderedUncharged = new EnumMap<>(ItemChargeKind.class);
		for (ItemChargeKind kind : unchargedItems)
		{
			orderedUncharged.put(kind, kind);
		}

		EnumMap<ItemChargeKind, Integer> normalizedCharges = new EnumMap<>(ItemChargeKind.class);
		charges.forEach((kind, quantity) -> normalizedCharges.put(kind, Math.max(0, quantity)));
		return new ItemChargeState(
			ImmutableSet.copyOf(orderedCharged.keySet()),
			ImmutableSet.copyOf(orderedUncharged.keySet()),
			ImmutableMap.copyOf(normalizedCharges));
	}

	private ItemChargeWarning missing(String text)
	{
		return new ItemChargeWarning(ItemChargeWarning.WarningPriority.MISSING, text);
	}

	private ItemChargeWarning unknown(String text)
	{
		return new ItemChargeWarning(ItemChargeWarning.WarningPriority.UNKNOWN, text);
	}

	private ItemChargeWarning low(String text)
	{
		return new ItemChargeWarning(ItemChargeWarning.WarningPriority.LOW, text);
	}
}
