package com.wildyqol.warnings.teleport;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wildyqol.WildyQoLConfig.TeleportOutWarningMode;
import com.wildyqol.warnings.magic.MagicInventoryState;
import com.wildyqol.warnings.magic.MagicRune;
import com.wildyqol.warnings.magic.MagicSpellbook;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.ItemID;

class TeleportOutEvaluator
{
	private static final TeleportOutWarning WARNING = new TeleportOutWarning("Missing teleport out");
	private static final List<TeleportSpell> SPELLS = ImmutableList.of(
		spell(MagicSpellbook.STANDARD, 25, runes(MagicRune.AIR, 3, MagicRune.FIRE, 1, MagicRune.LAW, 1)),
		spell(MagicSpellbook.STANDARD, 31, runes(MagicRune.AIR, 3, MagicRune.EARTH, 1, MagicRune.LAW, 1)),
		spell(MagicSpellbook.STANDARD, 37, runes(MagicRune.AIR, 3, MagicRune.WATER, 1, MagicRune.LAW, 1)),
		spell(MagicSpellbook.STANDARD, 40, runes(MagicRune.AIR, 1, MagicRune.EARTH, 1, MagicRune.LAW, 1)),
		spell(MagicSpellbook.STANDARD, 45, runes(MagicRune.AIR, 5, MagicRune.LAW, 1)),
		spell(MagicSpellbook.STANDARD, 48, runes(MagicRune.FIRE, 1, MagicRune.WATER, 1, MagicRune.LAW, 2)),
		spell(MagicSpellbook.STANDARD, 51, runes(MagicRune.WATER, 2, MagicRune.LAW, 2)),
		spell(MagicSpellbook.STANDARD, 54, runes(MagicRune.EARTH, 1, MagicRune.FIRE, 1, MagicRune.LAW, 2)),
		spell(MagicSpellbook.STANDARD, 58, runes(MagicRune.EARTH, 2, MagicRune.LAW, 2)),
		spell(MagicSpellbook.STANDARD, 61, runes(MagicRune.FIRE, 2, MagicRune.LAW, 2)),
		spell(MagicSpellbook.STANDARD, 64,
			runes(MagicRune.FIRE, 2, MagicRune.WATER, 2, MagicRune.LAW, 2),
			ItemID.BANANA),
		spell(MagicSpellbook.ANCIENT, 54, runes(MagicRune.AIR, 1, MagicRune.FIRE, 1, MagicRune.LAW, 2)),
		spell(MagicSpellbook.ANCIENT, 60, runes(MagicRune.LAW, 2, MagicRune.SOUL, 1)),
		spell(MagicSpellbook.ANCIENT, 66, runes(MagicRune.BLOOD, 1, MagicRune.LAW, 2)),
		spell(MagicSpellbook.ANCIENT, 72, runes(MagicRune.WATER, 4, MagicRune.LAW, 2)),
		spell(MagicSpellbook.LUNAR, 69, runes(MagicRune.EARTH, 2, MagicRune.ASTRAL, 2, MagicRune.LAW, 1)),
		spell(MagicSpellbook.LUNAR, 71, runes(MagicRune.EARTH, 6, MagicRune.ASTRAL, 2, MagicRune.LAW, 1)),
		spell(MagicSpellbook.LUNAR, 72, runes(MagicRune.WATER, 1, MagicRune.ASTRAL, 2, MagicRune.LAW, 1)),
		spell(MagicSpellbook.LUNAR, 75, runes(MagicRune.FIRE, 3, MagicRune.ASTRAL, 2, MagicRune.LAW, 2)),
		spell(MagicSpellbook.LUNAR, 78, runes(MagicRune.WATER, 4, MagicRune.ASTRAL, 2, MagicRune.LAW, 2)),
		spell(MagicSpellbook.LUNAR, 85, runes(MagicRune.WATER, 10, MagicRune.ASTRAL, 3, MagicRune.LAW, 3)),
		spell(MagicSpellbook.LUNAR, 87, runes(MagicRune.WATER, 10, MagicRune.ASTRAL, 3, MagicRune.LAW, 3)),
		spell(MagicSpellbook.LUNAR, 70, runes(MagicRune.EARTH, 4, MagicRune.ASTRAL, 2, MagicRune.LAW, 1)),
		spell(MagicSpellbook.LUNAR, 73, runes(MagicRune.WATER, 5, MagicRune.ASTRAL, 2, MagicRune.LAW, 1)),
		spell(MagicSpellbook.LUNAR, 76, runes(MagicRune.FIRE, 6, MagicRune.ASTRAL, 2, MagicRune.LAW, 2)),
		spell(MagicSpellbook.LUNAR, 79, runes(MagicRune.WATER, 8, MagicRune.ASTRAL, 2, MagicRune.LAW, 2)),
		spell(MagicSpellbook.LUNAR, 86, runes(MagicRune.WATER, 14, MagicRune.ASTRAL, 3, MagicRune.LAW, 3)),
		spell(MagicSpellbook.LUNAR, 88, runes(MagicRune.WATER, 15, MagicRune.ASTRAL, 3, MagicRune.LAW, 3)),
		spell(MagicSpellbook.ARCEUUS, 6, runes(MagicRune.EARTH, 2, MagicRune.LAW, 1)),
		spell(MagicSpellbook.ARCEUUS, 17, runes(MagicRune.EARTH, 1, MagicRune.WATER, 1, MagicRune.LAW, 1)),
		spell(MagicSpellbook.ARCEUUS, 23, runes(MagicRune.EARTH, 1, MagicRune.FIRE, 1, MagicRune.LAW, 1)),
		spell(MagicSpellbook.ARCEUUS, 28, runes(MagicRune.MIND, 2, MagicRune.LAW, 1)),
		spell(MagicSpellbook.ARCEUUS, 34, runes(MagicRune.SOUL, 1, MagicRune.LAW, 1)),
		spell(MagicSpellbook.ARCEUUS, 40, runes(MagicRune.SOUL, 2, MagicRune.LAW, 1)),
		spell(MagicSpellbook.ARCEUUS, 48, runes(MagicRune.EARTH, 1, MagicRune.SOUL, 1, MagicRune.LAW, 1)),
		spell(MagicSpellbook.ARCEUUS, 61, runes(MagicRune.SOUL, 2, MagicRune.LAW, 2)),
		spell(MagicSpellbook.ARCEUUS, 65, runes(MagicRune.NATURE, 1, MagicRune.SOUL, 1, MagicRune.LAW, 1)),
		spell(MagicSpellbook.ARCEUUS, 83, runes(MagicRune.BLOOD, 1, MagicRune.SOUL, 2, MagicRune.LAW, 2)),
		spell(MagicSpellbook.ARCEUUS, 90, runes(MagicRune.BLOOD, 2, MagicRune.SOUL, 2, MagicRune.LAW, 2)));

	List<TeleportOutWarning> evaluateAll(TeleportOutState state, TeleportOutWarningMode mode)
	{
		if (mode == TeleportOutWarningMode.NEVER
			|| hasRuneTeleport(state, mode)
			|| hasTeleportItem(state.getMagicInventoryState(), mode))
		{
			return ImmutableList.of();
		}

		return ImmutableList.of(WARNING);
	}

	static TeleportOutState state(MagicInventoryState magicInventoryState)
	{
		return new TeleportOutState(magicInventoryState);
	}

	private boolean hasTeleportItem(MagicInventoryState state, TeleportOutWarningMode mode)
	{
		for (int itemId : state.getItemIds())
		{
			if (TeleportOutTables.isTeleport(itemId, mode))
			{
				return true;
			}
		}
		return false;
	}

	private boolean hasRuneTeleport(TeleportOutState state, TeleportOutWarningMode mode)
	{
		if (mode != TeleportOutWarningMode.LEVEL_20)
		{
			return false;
		}

		MagicInventoryState magicInventoryState = state.getMagicInventoryState();
		for (TeleportSpell spell : SPELLS)
		{
			if (spell.canCast(magicInventoryState))
			{
				return true;
			}
		}

		return false;
	}

	private static TeleportSpell spell(MagicSpellbook spellbook, int magicLevel, Map<MagicRune, Integer> runes)
	{
		return spell(spellbook, magicLevel, runes, 0);
	}

	private static TeleportSpell spell(
		MagicSpellbook spellbook,
		int magicLevel,
		Map<MagicRune, Integer> runes,
		int requiredItemId)
	{
		return new TeleportSpell(spellbook, magicLevel, runes, requiredItemId);
	}

	private static Map<MagicRune, Integer> runes(
		MagicRune rune1,
		int count1,
		MagicRune rune2,
		int count2)
	{
		return ImmutableMap.of(rune1, count1, rune2, count2);
	}

	private static Map<MagicRune, Integer> runes(
		MagicRune rune1,
		int count1,
		MagicRune rune2,
		int count2,
		MagicRune rune3,
		int count3)
	{
		return ImmutableMap.of(rune1, count1, rune2, count2, rune3, count3);
	}

	private static class TeleportSpell
	{
		private final MagicSpellbook spellbook;
		private final int magicLevel;
		private final Map<MagicRune, Integer> runeRequirements;
		private final int requiredItemId;

		private TeleportSpell(
			MagicSpellbook spellbook,
			int magicLevel,
			Map<MagicRune, Integer> runeRequirements,
			int requiredItemId)
		{
			this.spellbook = spellbook;
			this.magicLevel = magicLevel;
			this.runeRequirements = new EnumMap<>(runeRequirements);
			this.requiredItemId = requiredItemId;
		}

		private boolean canCast(MagicInventoryState state)
		{
			if (state.getSpellbook() != spellbook || state.getMagicLevel() < magicLevel)
			{
				return false;
			}

			if (requiredItemId > 0 && state.getItemCounts().getOrDefault(requiredItemId, 0) <= 0)
			{
				return false;
			}

			for (Map.Entry<MagicRune, Integer> requirement : runeRequirements.entrySet())
			{
				if (!hasRune(state, requirement.getKey(), requirement.getValue()))
				{
					return false;
				}
			}

			return true;
		}

		private boolean hasRune(MagicInventoryState state, MagicRune rune, int count)
		{
			return state.getProvidedRunes().contains(rune)
				|| state.getRuneCounts().getOrDefault(rune, 0) >= count;
		}
	}
}
