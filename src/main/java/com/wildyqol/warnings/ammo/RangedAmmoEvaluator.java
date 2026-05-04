package com.wildyqol.warnings.ammo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.runelite.client.game.ItemVariationMapping;

public class RangedAmmoEvaluator
{
	public Optional<RangedAmmoWarning> evaluate(RangedAmmoState state, AmmoThresholds thresholds)
	{
		List<RangedAmmoWarning> warnings = evaluateAll(state, thresholds);
		return warnings.isEmpty() ? Optional.empty() : Optional.of(warnings.get(0));
	}

	public List<RangedAmmoWarning> evaluateAll(RangedAmmoState state, AmmoThresholds thresholds)
	{
		Set<RangedAmmoRequirement> requirements = state.getRequirements();
		if (requirements.isEmpty() && !state.isInactiveBowfa() && !state.isChargedBowfa())
		{
			return ImmutableList.of();
		}

		List<RangedAmmoWarning> warnings = new ArrayList<>();
		RangedAmmoWarning bowfaWarning = evaluateBowfa(state, thresholds);
		if (bowfaWarning != null)
		{
			warnings.add(bowfaWarning);
		}

		warnings.addAll(evaluateAmmo(requirements, state.getAmmoCounts(), thresholds));
		warnings.sort((left, right) ->
		{
			int priority = left.getPriority().compareTo(right.getPriority());
			return priority != 0 ? priority : left.getText().compareTo(right.getText());
		});
		return ImmutableList.copyOf(warnings);
	}

	@Nullable
	private RangedAmmoWarning evaluateBowfa(RangedAmmoState state, AmmoThresholds thresholds)
	{
		if (state.isInactiveBowfa())
		{
			return new RangedAmmoWarning(RangedAmmoWarning.WarningPriority.MISSING, "Missing Bowfa charges");
		}

		if (!state.isChargedBowfa())
		{
			return null;
		}

		int threshold = thresholds.bowfaCharges();
		if (threshold > 0 && state.getBowfaCharges() < threshold)
		{
			return new RangedAmmoWarning(
				RangedAmmoWarning.WarningPriority.LOW,
				"Low Bowfa charges: " + state.getBowfaCharges() + "/" + threshold);
		}

		return null;
	}

	private List<RangedAmmoWarning> evaluateAmmo(
		Set<RangedAmmoRequirement> requirements,
		Map<Integer, Integer> ammoCounts,
		AmmoThresholds thresholds)
	{
		if (requirements.isEmpty())
		{
			return ImmutableList.of();
		}

		List<RangedAmmoWarning> warnings = new ArrayList<>();
		for (RangedAmmoRequirement requirement : requirements)
		{
			int compatibleCount = countCompatibleAmmo(requirement, ammoCounts);
			if (compatibleCount == 0)
			{
				boolean wrongAmmo = requirements.size() == 1 && !ammoCounts.isEmpty();
				warnings.add(new RangedAmmoWarning(
					wrongAmmo ? RangedAmmoWarning.WarningPriority.WRONG : RangedAmmoWarning.WarningPriority.MISSING,
					(wrongAmmo ? "Wrong ammo: " : "Missing ammo: ") + requirement.getRequiredText()
						+ (wrongAmmo ? " required" : "")));
			}
		}

		for (RangedAmmoRequirement requirement : requirements)
		{
			int threshold = requirement.getThreshold(thresholds);
			if (threshold <= 0)
			{
				continue;
			}

			int compatibleCount = countCompatibleAmmo(requirement, ammoCounts);
			if (compatibleCount > 0 && compatibleCount < threshold)
			{
				warnings.add(new RangedAmmoWarning(
					RangedAmmoWarning.WarningPriority.LOW,
					"Low " + requirement.getLowText() + ": " + compatibleCount + "/" + threshold));
			}
		}

		return ImmutableList.copyOf(warnings);
	}

	private int countCompatibleAmmo(RangedAmmoRequirement requirement, Map<Integer, Integer> ammoCounts)
	{
		int count = 0;
		Set<Integer> acceptedIds = new HashSet<>();
		for (int acceptedAmmoId : requirement.getAcceptedAmmoIds())
		{
			acceptedIds.add(ItemVariationMapping.map(acceptedAmmoId));
			acceptedIds.add(acceptedAmmoId);
		}

		for (int acceptedId : acceptedIds)
		{
			count += ammoCounts.getOrDefault(acceptedId, 0);
		}
		return count;
	}

	public static RangedAmmoState state(
		Set<RangedAmmoRequirement> requirements,
		Map<Integer, Integer> ammoCounts,
		boolean chargedBowfa,
		boolean inactiveBowfa,
		int bowfaCharges)
	{
		EnumMap<RangedAmmoRequirement, RangedAmmoRequirement> orderedRequirements = new EnumMap<>(RangedAmmoRequirement.class);
		for (RangedAmmoRequirement requirement : requirements)
		{
			orderedRequirements.put(requirement, requirement);
		}

		Map<Integer, Integer> normalizedAmmoCounts = new HashMap<>();
		ammoCounts.forEach((itemId, quantity) ->
		{
			if (quantity > 0)
			{
				normalizedAmmoCounts.merge(ItemVariationMapping.map(itemId), quantity, Integer::sum);
			}
		});

		return new RangedAmmoState(
			ImmutableSet.copyOf(orderedRequirements.keySet()),
			ImmutableMap.copyOf(normalizedAmmoCounts),
			chargedBowfa,
			inactiveBowfa,
			Math.max(0, bowfaCharges));
	}
}
