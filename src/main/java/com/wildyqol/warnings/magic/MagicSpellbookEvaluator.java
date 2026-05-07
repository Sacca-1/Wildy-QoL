package com.wildyqol.warnings.magic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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

public class MagicSpellbookEvaluator
{
	private static final MagicSpellRequirement TELE_BLOCK = MagicSpellRequirement.of(
		MagicRune.LAW, 1, MagicRune.DEATH, 1, MagicRune.CHAOS, 1);
	private static final MagicSpellRequirement SNARE = MagicSpellRequirement.of(
		MagicRune.EARTH, 3, MagicRune.WATER, 3, MagicRune.NATURE, 2);
	private static final MagicSpellRequirement ENTANGLE = MagicSpellRequirement.of(
		MagicRune.EARTH, 5, MagicRune.WATER, 5, MagicRune.NATURE, 4);
	private static final MagicSpellRequirement FIRE_SURGE = MagicSpellRequirement.of(
		MagicRune.AIR, 7, MagicRune.FIRE, 10, MagicRune.WRATH, 1);
	private static final MagicSpellRequirement VENGEANCE = MagicSpellRequirement.of(
		MagicRune.ASTRAL, 4, MagicRune.DEATH, 2, MagicRune.EARTH, 10);

	private static final List<MagicSpellRequirement> GOD_SPELLS = ImmutableList.of(
		MagicSpellRequirement.of(MagicRune.AIR, 4, MagicRune.FIRE, 2, MagicRune.BLOOD, 2),
		MagicSpellRequirement.of(MagicRune.AIR, 4, MagicRune.FIRE, 1, MagicRune.BLOOD, 2),
		MagicSpellRequirement.of(MagicRune.AIR, 1, MagicRune.FIRE, 4, MagicRune.BLOOD, 2));

	private static final List<MagicSpellRequirement> ICE_SPELLS = ImmutableList.of(
		MagicSpellRequirement.of(MagicRune.CHAOS, 2, MagicRune.DEATH, 2, MagicRune.WATER, 2),
		MagicSpellRequirement.of(MagicRune.CHAOS, 4, MagicRune.DEATH, 2, MagicRune.WATER, 4),
		MagicSpellRequirement.of(MagicRune.BLOOD, 2, MagicRune.DEATH, 2, MagicRune.WATER, 3),
		MagicSpellRequirement.of(MagicRune.BLOOD, 2, MagicRune.DEATH, 4, MagicRune.WATER, 6));

	private static final List<MagicSpellRequirement> BLOOD_SPELLS = ImmutableList.of(
		MagicSpellRequirement.of(MagicRune.CHAOS, 2, MagicRune.DEATH, 2, MagicRune.BLOOD, 1),
		MagicSpellRequirement.of(MagicRune.CHAOS, 4, MagicRune.DEATH, 2, MagicRune.BLOOD, 2),
		MagicSpellRequirement.of(MagicRune.DEATH, 2, MagicRune.BLOOD, 4),
		MagicSpellRequirement.of(MagicRune.BLOOD, 4, MagicRune.DEATH, 4, MagicRune.SOUL, 1));

	public Optional<MagicSpellbookWarning> evaluate(MagicSpellbookState state, MagicThresholds thresholds)
	{
		List<MagicSpellbookWarning> warnings = evaluateAll(state, thresholds);
		return warnings.isEmpty() ? Optional.empty() : Optional.of(warnings.get(0));
	}

	public List<MagicSpellbookWarning> evaluateAll(MagicSpellbookState state, MagicThresholds thresholds)
	{
		if (state.isMagicCape())
		{
			return ImmutableList.of();
		}

		boolean standardResources = hasStandardResources(state);
		boolean ancientResources = hasAncientIceResources(state);
		boolean lunarResources = hasLunarResources(state);

		if (state.getSpellbook() == MagicSpellbook.STANDARD)
		{
			if (!standardResources && (ancientResources || lunarResources))
			{
				return mismatch();
			}
			return evaluateStandard(state, thresholds);
		}

		if (state.getSpellbook() == MagicSpellbook.ANCIENT)
		{
			if (!ancientResources && (standardResources || lunarResources))
			{
				return mismatch();
			}
			return evaluateAncient(state, thresholds);
		}

		if (state.getSpellbook() == MagicSpellbook.LUNAR)
		{
			if (!lunarResources && (standardResources || ancientResources))
			{
				return mismatch();
			}
			return evaluateLunar(state, thresholds);
		}

		return mismatch();
	}

	private List<MagicSpellbookWarning> evaluateStandard(MagicSpellbookState state, MagicThresholds thresholds)
	{
		List<MagicSpellbookWarning> warnings = new ArrayList<>();
		addCategoryWarning(warnings, "Tele Block", "TB casts", max(
			countItem(state, ItemID.BLIGHTED_TELEPORT_SPELL_SACK),
			casts(TELE_BLOCK, state)), thresholds.teleBlock());
		addCategoryWarning(warnings, "freeze", "freeze casts", max(
			countItem(state, ItemID.BLIGHTED_ENTANGLE_SACK),
			casts(SNARE, state),
			casts(ENTANGLE, state)), thresholds.entangle());

		warnings.addAll(evaluateStandardDamage(state, thresholds));
		return sort(warnings);
	}

	private List<MagicSpellbookWarning> evaluateStandardDamage(MagicSpellbookState state, MagicThresholds thresholds)
	{
		List<MagicSpellbookWarning> warnings = new ArrayList<>();
		int threshold = thresholds.surge();
		int runeFireSurgeCasts = casts(FIRE_SURGE, state);
		int fireSurgeCasts = max(countItem(state, ItemID.BLIGHTED_SURGE_SACK), runeFireSurgeCasts);
		int godCasts = state.hasValidGodStaff() ? maxCasts(GOD_SPELLS, state) : 0;
		int bestRuneDamage = max(fireSurgeCasts, godCasts);
		if (runeFireSurgeCasts > 0 || godCasts > 0)
		{
			addTomeChargeWarning(warnings, state, MagicRune.FIRE, "tome of fire", thresholds.tomeCharges());
		}

		if (meets(bestRuneDamage, threshold) || state.isChargedWildySceptre())
		{
			return warnings;
		}

		if (bestRuneDamage > 0)
		{
			warnings.add(low("Low damage casts: " + bestRuneDamage + "/" + threshold));
			return ImmutableList.copyOf(warnings);
		}

		warnings.add(missing("Missing runes: damage"));
		return ImmutableList.copyOf(warnings);
	}

	private List<MagicSpellbookWarning> evaluateAncient(MagicSpellbookState state, MagicThresholds thresholds)
	{
		List<MagicSpellbookWarning> warnings = new ArrayList<>();
		addCategoryWarning(warnings, "ice spells", "ice casts", max(
			countItem(state, ItemID.BLIGHTED_ANCIENT_ICE_SACK),
			maxCasts(ICE_SPELLS, state)), thresholds.ice());

		int bloodCasts = maxCasts(BLOOD_SPELLS, state);
		if (bloodCasts > 0 && !meets(bloodCasts, thresholds.blood()))
		{
			warnings.add(low("Low blood casts: " + bloodCasts + "/" + thresholds.blood()));
		}
		return sort(warnings);
	}

	private List<MagicSpellbookWarning> evaluateLunar(MagicSpellbookState state, MagicThresholds thresholds)
	{
		List<MagicSpellbookWarning> warnings = new ArrayList<>();
		addCategoryWarning(warnings, "Vengeance", "vengeance casts", max(
			countItem(state, ItemID.BLIGHTED_VENGEANCE_SACK),
			casts(VENGEANCE, state)), thresholds.vengeance());
		return sort(warnings);
	}

	private boolean hasStandardResources(MagicSpellbookState state)
	{
		return countItem(state, ItemID.BLIGHTED_TELEPORT_SPELL_SACK) > 0
			|| countItem(state, ItemID.BLIGHTED_ENTANGLE_SACK) > 0
			|| countItem(state, ItemID.BLIGHTED_SURGE_SACK) > 0
			|| casts(TELE_BLOCK, state) > 0
			|| casts(SNARE, state) > 0
			|| casts(ENTANGLE, state) > 0
			|| casts(FIRE_SURGE, state) > 0
			|| maxCasts(GOD_SPELLS, state) > 0
			|| state.hasValidGodStaff()
			|| state.isChargedWildySceptre()
			|| state.isUnchargedWildySceptre();
	}

	private boolean hasAncientIceResources(MagicSpellbookState state)
	{
		return countItem(state, ItemID.BLIGHTED_ANCIENT_ICE_SACK) > 0
			|| maxCasts(ICE_SPELLS, state) > 0;
	}

	private boolean hasLunarResources(MagicSpellbookState state)
	{
		return countItem(state, ItemID.BLIGHTED_VENGEANCE_SACK) > 0
			|| casts(VENGEANCE, state) > 0;
	}

	private void addCategoryWarning(
		List<MagicSpellbookWarning> warnings,
		String missingText,
		String lowText,
		int casts,
		int threshold)
	{
		if (meets(casts, threshold))
		{
			return;
		}

		if (casts > 0)
		{
			warnings.add(low("Low " + lowText + ": " + casts + "/" + threshold));
			return;
		}

		warnings.add(missing("Missing runes: " + missingText));
	}

	private void addTomeChargeWarning(
		List<MagicSpellbookWarning> warnings,
		MagicSpellbookState state,
		MagicRune rune,
		String tomeName,
		int threshold)
	{
		Integer charges = state.getTomeCharges().get(rune);
		if (charges != null && threshold > 0 && charges < threshold)
		{
			warnings.add(low("Low " + tomeName + " charges: " + charges + "/" + threshold));
		}
	}

	private boolean meets(int casts, int threshold)
	{
		return threshold <= 0 || casts >= threshold;
	}

	private int maxCasts(List<MagicSpellRequirement> spells, MagicSpellbookState state)
	{
		int max = 0;
		for (MagicSpellRequirement spell : spells)
		{
			max = Math.max(max, casts(spell, state));
		}
		return max;
	}

	private int casts(MagicSpellRequirement spell, MagicSpellbookState state)
	{
		int casts = Integer.MAX_VALUE;
		for (Map.Entry<MagicRune, Integer> requirement : spell.getRunes().entrySet())
		{
			MagicRune rune = requirement.getKey();
			int available = state.getProvidedRunes().contains(rune)
				? Integer.MAX_VALUE / 4
				: state.getRuneCounts().getOrDefault(rune, 0);
			casts = Math.min(casts, available / requirement.getValue());
		}
		return casts == Integer.MAX_VALUE ? 0 : casts;
	}

	private int countItem(MagicSpellbookState state, int itemId)
	{
		int mappedItemId = ItemVariationMapping.map(itemId);
		if (mappedItemId == itemId)
		{
			return state.getItemCounts().getOrDefault(itemId, 0);
		}

		return state.getItemCounts().getOrDefault(itemId, 0)
			+ state.getItemCounts().getOrDefault(mappedItemId, 0);
	}

	private List<MagicSpellbookWarning> mismatch()
	{
		return ImmutableList.of(new MagicSpellbookWarning(
			MagicSpellbookWarning.WarningPriority.MISMATCH,
			"Spellbook and runes do not match"));
	}

	private MagicSpellbookWarning missing(String text)
	{
		return new MagicSpellbookWarning(MagicSpellbookWarning.WarningPriority.MISSING, text);
	}

	private MagicSpellbookWarning low(String text)
	{
		return new MagicSpellbookWarning(MagicSpellbookWarning.WarningPriority.LOW, text);
	}

	private List<MagicSpellbookWarning> sort(List<MagicSpellbookWarning> warnings)
	{
		warnings.sort((left, right) ->
		{
			int priority = left.getPriority().compareTo(right.getPriority());
			return priority != 0 ? priority : left.getText().compareTo(right.getText());
		});
		return ImmutableList.copyOf(warnings);
	}

	private int max(int... values)
	{
		int max = 0;
		for (int value : values)
		{
			max = Math.max(max, value);
		}
		return max;
	}

	static MagicSpellbookState state(
		MagicInventoryState inventoryState)
	{
		return state(
			inventoryState.getSpellbook(),
			inventoryState.getRuneCounts(),
			inventoryState.getItemCounts(),
			inventoryState.getProvidedRunes(),
			inventoryState.getTomeCharges(),
			inventoryState.isMagicCape(),
			inventoryState.isValidGodStaff(),
			inventoryState.isChargedWildySceptre(),
			inventoryState.isUnchargedWildySceptre());
	}

	static MagicSpellbookState state(
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
		EnumMap<MagicRune, Integer> normalizedRunes = new EnumMap<>(MagicRune.class);
		runeCounts.forEach((rune, quantity) ->
		{
			if (quantity > 0)
			{
				normalizedRunes.merge(rune, quantity, Integer::sum);
			}
		});

		Map<Integer, Integer> normalizedItems = new HashMap<>();
		itemCounts.forEach((itemId, quantity) ->
		{
			if (itemId > 0 && quantity > 0)
			{
				normalizedItems.merge(ItemVariationMapping.map(itemId), quantity, Integer::sum);
			}
		});

		EnumMap<MagicRune, Integer> normalizedTomeCharges = new EnumMap<>(MagicRune.class);
		tomeCharges.forEach((rune, quantity) -> normalizedTomeCharges.put(rune, Math.max(0, quantity)));

		return new MagicSpellbookState(
			spellbook,
			ImmutableMap.copyOf(normalizedRunes),
			ImmutableMap.copyOf(normalizedItems),
			ImmutableSet.copyOf(providedRunes),
			ImmutableMap.copyOf(normalizedTomeCharges),
			magicCape,
			validGodStaff,
			chargedWildySceptre,
			unchargedWildySceptre);
	}
}
