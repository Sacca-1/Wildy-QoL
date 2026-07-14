package com.wildyqol.warnings.ammo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemVariationMapping;

public class RangedAmmoEvaluator
{
	private static final Map<Integer, String> AMMO_NAMES = buildAmmoNames();

	public Optional<RangedAmmoWarning> evaluate(RangedAmmoState state, AmmoThresholds thresholds)
	{
		List<RangedAmmoWarning> warnings = evaluateAll(state, thresholds);
		return warnings.isEmpty() ? Optional.empty() : Optional.of(warnings.get(0));
	}

	public List<RangedAmmoWarning> evaluateAll(RangedAmmoState state, AmmoThresholds thresholds)
	{
		return evaluateAll(state, thresholds, true);
	}

	public List<RangedAmmoWarning> evaluateAll(
		RangedAmmoState state,
		AmmoThresholds thresholds,
		boolean warnSuboptimalAmmo)
	{
		Set<RangedAmmoRequirement> requirements = state.getRequirements();
		if (requirements.isEmpty())
		{
			return ImmutableList.of();
		}

		List<RangedAmmoWarning> warnings = new ArrayList<>();
		warnings.addAll(evaluateAmmo(requirements, state.getAmmoCounts(), thresholds));
		if (warnSuboptimalAmmo)
		{
			warnings.addAll(evaluateSuboptimalAmmo(requirements, state.getAmmoCounts()));
		}
		warnings.sort((left, right) ->
		{
			int priority = left.getPriority().compareTo(right.getPriority());
			return priority != 0 ? priority : left.getText().compareTo(right.getText());
		});
		return ImmutableList.copyOf(warnings);
	}

	private List<RangedAmmoWarning> evaluateSuboptimalAmmo(
		Set<RangedAmmoRequirement> requirements,
		Map<Integer, Integer> ammoCounts)
	{
		List<RangedAmmoWarning> warnings = new ArrayList<>();
		for (RangedAmmoRequirement requirement : requirements)
		{
			int compatibleCount = countAmmo(requirement.getAcceptedAmmoIds(), ammoCounts);
			int optimalCount = countOptimalAmmo(requirement, ammoCounts);
			if (compatibleCount > 0 && optimalCount == 0)
			{
				String ammoText = findSuboptimalAmmoText(requirement, ammoCounts)
					.map(ammoName -> "Suboptimal ammo: " + ammoName)
					.orElse("Suboptimal ammo");
				warnings.add(new RangedAmmoWarning(
					RangedAmmoWarning.WarningPriority.SUBOPTIMAL,
					ammoText));
			}
		}
		return ImmutableList.copyOf(warnings);
	}

	private Optional<String> findSuboptimalAmmoText(
		RangedAmmoRequirement requirement,
		Map<Integer, Integer> ammoCounts)
	{
		String bestAmmoText = null;
		int bestQuantity = 0;
		for (Map.Entry<Integer, Integer> ammoCount : ammoCounts.entrySet())
		{
			int quantity = ammoCount.getValue();
			int ammoId = ammoCount.getKey();
			int mappedAmmoId = ItemVariationMapping.map(ammoId);
			if (quantity <= 0
				|| !requirement.getAcceptedAmmoIds().contains(mappedAmmoId)
				|| isOptimalAmmo(requirement, ammoId))
			{
				continue;
			}

			String ammoText = AMMO_NAMES.get(mappedAmmoId);
			if (ammoText == null)
			{
				continue;
			}

			if (quantity > bestQuantity
				|| quantity == bestQuantity && (bestAmmoText == null || ammoText.compareTo(bestAmmoText) < 0))
			{
				bestAmmoText = ammoText;
				bestQuantity = quantity;
			}
		}
		return Optional.ofNullable(bestAmmoText);
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
				String missingText = hasMultipleRequirementsForAmmoFamily(requirement, requirements)
					? requirement.getSpecificText()
					: requirement.getAmmoFamilyText();
				String text = wrongAmmo
					? "Wrong " + requirement.getWrongText()
					: "No " + missingText;
				warnings.add(new RangedAmmoWarning(
					wrongAmmo ? RangedAmmoWarning.WarningPriority.WRONG : RangedAmmoWarning.WarningPriority.MISSING,
					text));
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
				String lowText = hasMultipleRequirementsForAmmoFamily(requirement, requirements)
					? requirement.getSpecificText()
					: requirement.getAmmoFamilyText();
				warnings.add(new RangedAmmoWarning(
					RangedAmmoWarning.WarningPriority.LOW,
					"Low " + lowText + ": " + compatibleCount + "/" + threshold));
			}
		}

		return ImmutableList.copyOf(warnings);
	}

	private boolean hasMultipleRequirementsForAmmoFamily(
		RangedAmmoRequirement requirement,
		Set<RangedAmmoRequirement> requirements)
	{
		int matchingRequirements = 0;
		for (RangedAmmoRequirement other : requirements)
		{
			if (other.getAmmoFamilyText().equals(requirement.getAmmoFamilyText()) && ++matchingRequirements > 1)
			{
				return true;
			}
		}
		return false;
	}

	private int countCompatibleAmmo(RangedAmmoRequirement requirement, Map<Integer, Integer> ammoCounts)
	{
		return countAmmo(requirement.getAcceptedAmmoIds(), ammoCounts);
	}

	private int countAmmo(Set<Integer> ammoIds, Map<Integer, Integer> ammoCounts)
	{
		int count = 0;
		for (Map.Entry<Integer, Integer> ammoCount : ammoCounts.entrySet())
		{
			if (ammoIds.contains(ItemVariationMapping.map(ammoCount.getKey())))
			{
				count += ammoCount.getValue();
			}
		}
		return count;
	}

	private int countOptimalAmmo(RangedAmmoRequirement requirement, Map<Integer, Integer> ammoCounts)
	{
		int count = 0;
		for (Map.Entry<Integer, Integer> ammoCount : ammoCounts.entrySet())
		{
			if (isOptimalAmmo(requirement, ammoCount.getKey()))
			{
				count += ammoCount.getValue();
			}
		}
		return count;
	}

	private boolean isOptimalAmmo(RangedAmmoRequirement requirement, int itemId)
	{
		return requirement.getExactOptimalAmmoIds().contains(itemId)
			|| requirement.getOptimalAmmoIds().contains(ItemVariationMapping.map(itemId));
	}

	public static RangedAmmoState state(
		Set<RangedAmmoRequirement> requirements,
		Map<Integer, Integer> ammoCounts)
	{
		EnumMap<RangedAmmoRequirement, RangedAmmoRequirement> orderedRequirements = new EnumMap<>(RangedAmmoRequirement.class);
		for (RangedAmmoRequirement requirement : requirements)
		{
			orderedRequirements.put(requirement, requirement);
		}

		Map<Integer, Integer> rawAmmoCounts = new HashMap<>();
		ammoCounts.forEach((itemId, quantity) ->
		{
			if (quantity > 0)
			{
				rawAmmoCounts.merge(itemId, quantity, Integer::sum);
			}
		});

		return new RangedAmmoState(
			ImmutableSet.copyOf(orderedRequirements.keySet()),
			ImmutableMap.copyOf(rawAmmoCounts));
	}

	private static Map<Integer, String> buildAmmoNames()
	{
		ImmutableMap.Builder<Integer, String> ammoNames = ImmutableMap.builder();
		putAmmoName(ammoNames, ItemID.BRONZE_BOLTS, "Bronze bolts");
		putAmmoName(ammoNames, ItemID.OPAL_BOLTS_E, "Opal bolts");
		putAmmoName(ammoNames, ItemID.BLURITE_BOLTS, "Blurite bolts");
		putAmmoName(ammoNames, ItemID.JADE_BOLTS_E, "Jade bolts");
		putAmmoName(ammoNames, ItemID.IRON_BOLTS, "Iron bolts");
		putAmmoName(ammoNames, ItemID.SILVER_BOLTS, "Silver bolts");
		putAmmoName(ammoNames, ItemID.PEARL_BOLTS_E, "Pearl bolts");
		putAmmoName(ammoNames, ItemID.STEEL_BOLTS, "Steel bolts");
		putAmmoName(ammoNames, ItemID.TOPAZ_BOLTS_E, "Topaz bolts");
		putAmmoName(ammoNames, ItemID.MITHRIL_BOLTS, "Mithril bolts");
		putAmmoName(ammoNames, ItemID.SAPPHIRE_BOLTS_E, "Sapphire bolts");
		putAmmoName(ammoNames, ItemID.EMERALD_BOLTS_E, "Emerald bolts");
		putAmmoName(ammoNames, ItemID.ADAMANT_BOLTS, "Adamant bolts");
		putAmmoName(ammoNames, ItemID.RUBY_BOLTS_E, "Ruby bolts");
		putAmmoName(ammoNames, ItemID.DIAMOND_BOLTS_E, "Diamond bolts");
		putAmmoName(ammoNames, ItemID.RUNITE_BOLTS, "Runite bolts");
		putAmmoName(ammoNames, ItemID.DRAGONSTONE_BOLTS_E, "Dragonstone bolts");
		putAmmoName(ammoNames, ItemID.ONYX_BOLTS_E, "Onyx bolts");
		putAmmoName(ammoNames, ItemID.BROAD_BOLTS, "Broad bolts");
		putAmmoName(ammoNames, ItemID.AMETHYST_BROAD_BOLTS, "Amethyst broad bolts");
		putAmmoName(ammoNames, ItemID.DRAGON_BOLTS, "Dragon bolts");
		putAmmoName(ammoNames, ItemID.OPAL_DRAGON_BOLTS_E, "Opal dragon bolts");
		putAmmoName(ammoNames, ItemID.JADE_DRAGON_BOLTS_E, "Jade dragon bolts");
		putAmmoName(ammoNames, ItemID.PEARL_DRAGON_BOLTS_E, "Pearl dragon bolts");
		putAmmoName(ammoNames, ItemID.TOPAZ_DRAGON_BOLTS_E, "Topaz dragon bolts");
		putAmmoName(ammoNames, ItemID.SAPPHIRE_DRAGON_BOLTS_E, "Sapphire dragon bolts");
		putAmmoName(ammoNames, ItemID.EMERALD_DRAGON_BOLTS_E, "Emerald dragon bolts");
		putAmmoName(ammoNames, ItemID.RUBY_DRAGON_BOLTS_E, "Ruby dragon bolts");
		putAmmoName(ammoNames, ItemID.DIAMOND_DRAGON_BOLTS_E, "Diamond dragon bolts");
		putAmmoName(ammoNames, ItemID.DRAGONSTONE_DRAGON_BOLTS_E, "Dragonstone dragon bolts");
		putAmmoName(ammoNames, ItemID.ONYX_DRAGON_BOLTS_E, "Onyx dragon bolts");
		putAmmoName(ammoNames, ItemID.BRONZE_ARROW, "Bronze arrows");
		putSeekingArrowNames(ammoNames, SeekingArrowIds.BRONZE_ARROW, "Bronze arrows");
		putAmmoName(ammoNames, ItemID.IRON_ARROW, "Iron arrows");
		putSeekingArrowNames(ammoNames, SeekingArrowIds.IRON_ARROW, "Iron arrows");
		putAmmoName(ammoNames, ItemID.STEEL_ARROW, "Steel arrows");
		putSeekingArrowNames(ammoNames, SeekingArrowIds.STEEL_ARROW, "Steel arrows");
		putAmmoName(ammoNames, ItemID.MITHRIL_ARROW, "Mithril arrows");
		putSeekingArrowNames(ammoNames, SeekingArrowIds.MITHRIL_ARROW, "Mithril arrows");
		putAmmoName(ammoNames, ItemID.ADAMANT_ARROW, "Adamant arrows");
		putSeekingArrowNames(ammoNames, SeekingArrowIds.ADAMANT_ARROW, "Adamant arrows");
		putAmmoName(ammoNames, ItemID.RUNE_ARROW, "Rune arrows");
		putSeekingArrowNames(ammoNames, SeekingArrowIds.RUNE_ARROW, "Rune arrows");
		putAmmoName(ammoNames, ItemID.AMETHYST_ARROW, "Amethyst arrows");
		putSeekingArrowNames(ammoNames, SeekingArrowIds.AMETHYST_ARROW, "Amethyst arrows");
		putAmmoName(ammoNames, ItemID.DRAGON_ARROW, "Dragon arrows");
		putSeekingArrowNames(ammoNames, SeekingArrowIds.DRAGON_ARROW, "Dragon arrows");
		return ammoNames.build();
	}

	private static void putSeekingArrowNames(ImmutableMap.Builder<Integer, String> ammoNames, int firstItemId, String text)
	{
		for (int itemId = firstItemId; itemId < firstItemId + SeekingArrowIds.STACK_VARIANTS; itemId++)
		{
			putAmmoName(ammoNames, itemId, text);
		}
	}

	private static void putAmmoName(ImmutableMap.Builder<Integer, String> ammoNames, int itemId, String text)
	{
		ammoNames.put(ItemVariationMapping.map(itemId), text);
	}
}
