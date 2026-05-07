package com.wildyqol.warnings.magic;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemVariationMapping;

final class MagicItemTables
{
	private static final Map<Integer, Set<MagicRune>> RUNE_ITEMS = ImmutableMap.<Integer, Set<MagicRune>>builder()
		.put(ItemID.AIR_RUNE, ImmutableSet.of(MagicRune.AIR))
			.put(ItemID.WATER_RUNE, ImmutableSet.of(MagicRune.WATER))
			.put(ItemID.EARTH_RUNE, ImmutableSet.of(MagicRune.EARTH))
			.put(ItemID.FIRE_RUNE, ImmutableSet.of(MagicRune.FIRE))
			.put(ItemID.SUNFIRE_RUNE, ImmutableSet.of(MagicRune.FIRE))
			.put(ItemID.MIND_RUNE, ImmutableSet.of(MagicRune.MIND))
			.put(ItemID.CHAOS_RUNE, ImmutableSet.of(MagicRune.CHAOS))
			.put(ItemID.DEATH_RUNE, ImmutableSet.of(MagicRune.DEATH))
			.put(ItemID.BLOOD_RUNE, ImmutableSet.of(MagicRune.BLOOD))
			.put(ItemID.LAW_RUNE, ImmutableSet.of(MagicRune.LAW))
			.put(ItemID.NATURE_RUNE, ImmutableSet.of(MagicRune.NATURE))
			.put(ItemID.ASTRAL_RUNE, ImmutableSet.of(MagicRune.ASTRAL))
			.put(ItemID.WRATH_RUNE, ImmutableSet.of(MagicRune.WRATH))
			.put(ItemID.SOUL_RUNE, ImmutableSet.of(MagicRune.SOUL))
		.put(ItemID.MIST_RUNE, ImmutableSet.of(MagicRune.AIR, MagicRune.WATER))
		.put(ItemID.MUD_RUNE, ImmutableSet.of(MagicRune.WATER, MagicRune.EARTH))
		.put(ItemID.DUST_RUNE, ImmutableSet.of(MagicRune.AIR, MagicRune.EARTH))
		.put(ItemID.LAVA_RUNE, ImmutableSet.of(MagicRune.EARTH, MagicRune.FIRE))
		.put(ItemID.STEAM_RUNE, ImmutableSet.of(MagicRune.WATER, MagicRune.FIRE))
		.put(ItemID.SMOKE_RUNE, ImmutableSet.of(MagicRune.AIR, MagicRune.FIRE))
		.build();

	private static final Set<Integer> AIR_PROVIDERS = buildSet(
		ItemID.STAFF_OF_AIR, ItemID.AIR_BATTLESTAFF, ItemID.MYSTIC_AIR_STAFF,
		ItemID.MIST_BATTLESTAFF, ItemID.MYSTIC_MIST_STAFF,
		ItemID.DUST_BATTLESTAFF, ItemID.MYSTIC_DUST_STAFF,
		ItemID.SMOKE_BATTLESTAFF, ItemID.MYSTIC_SMOKE_STAFF);
	private static final Set<Integer> WATER_PROVIDERS = buildSet(
		ItemID.STAFF_OF_WATER, ItemID.WATER_BATTLESTAFF, ItemID.MYSTIC_WATER_STAFF,
		ItemID.MIST_BATTLESTAFF, ItemID.MYSTIC_MIST_STAFF,
		ItemID.MUD_BATTLESTAFF, ItemID.MYSTIC_MUD_STAFF,
		ItemID.STEAM_BATTLESTAFF, ItemID.MYSTIC_STEAM_STAFF,
		ItemID.KODAI_WAND, ItemID.KODAI_WAND_23626);
	private static final Set<Integer> EARTH_PROVIDERS = buildSet(
		ItemID.STAFF_OF_EARTH, ItemID.EARTH_BATTLESTAFF, ItemID.MYSTIC_EARTH_STAFF,
		ItemID.DUST_BATTLESTAFF, ItemID.MYSTIC_DUST_STAFF,
		ItemID.MUD_BATTLESTAFF, ItemID.MYSTIC_MUD_STAFF,
		ItemID.LAVA_BATTLESTAFF, ItemID.MYSTIC_LAVA_STAFF);
	private static final Set<Integer> FIRE_PROVIDERS = buildSet(
		ItemID.STAFF_OF_FIRE, ItemID.FIRE_BATTLESTAFF, ItemID.MYSTIC_FIRE_STAFF,
		ItemID.SMOKE_BATTLESTAFF, ItemID.MYSTIC_SMOKE_STAFF,
		ItemID.STEAM_BATTLESTAFF, ItemID.MYSTIC_STEAM_STAFF,
		ItemID.LAVA_BATTLESTAFF, ItemID.MYSTIC_LAVA_STAFF);
	private static final Set<Integer> NATURE_PROVIDERS = buildSet(ItemID.BRYOPHYTAS_STAFF);
	private static final Set<Integer> GOD_STAFFS = buildSet(
		ItemID.SARADOMIN_STAFF, ItemID.GUTHIX_STAFF, ItemID.ZAMORAK_STAFF,
		ItemID.VOID_KNIGHT_MACE, ItemID.STAFF_OF_THE_DEAD, ItemID.STAFF_OF_THE_DEAD_23613,
		ItemID.TOXIC_STAFF_OF_THE_DEAD, ItemID.STAFF_OF_LIGHT, ItemID.STAFF_OF_BALANCE);
	private static final Set<Integer> CHARGED_WILDY_SCEPTRES = buildSet(
		ItemID.THAMMARONS_SCEPTRE, ItemID.THAMMARONS_SCEPTRE_A,
		ItemID.ACCURSED_SCEPTRE, ItemID.ACCURSED_SCEPTRE_A);
	private static final Set<Integer> UNCHARGED_WILDY_SCEPTRES = buildSet(
		ItemID.THAMMARONS_SCEPTRE_U, ItemID.THAMMARONS_SCEPTRE_AU,
		ItemID.ACCURSED_SCEPTRE_U, ItemID.ACCURSED_SCEPTRE_AU);
	private static final Set<Integer> MAGIC_CAPES = buildSet(
		ItemID.MAGIC_CAPE, ItemID.MAGIC_CAPET, ItemID.MAX_CAPE, ItemID.MAX_CAPE_13342);
	private static final Set<Integer> RUNE_POUCHES = buildSet(
		ItemID.RUNE_POUCH,
		ItemID.RUNE_POUCH_23650,
		ItemID.RUNE_POUCH_L,
		ItemID.RUNE_POUCH_27086,
		ItemID.DIVINE_RUNE_POUCH,
		ItemID.DIVINE_RUNE_POUCH_L);

	private MagicItemTables()
	{
	}

	private static Set<Integer> buildSet(Integer... itemIds)
	{
		ImmutableSet.Builder<Integer> ids = ImmutableSet.builder();
		for (int itemId : itemIds)
		{
			ids.add(itemId);
			ids.add(ItemVariationMapping.map(itemId));
			ids.addAll(ItemVariationMapping.getVariations(ItemVariationMapping.map(itemId)));
		}
		return ids.build();
	}

	static Set<MagicRune> getRuneTypes(int itemId)
	{
		Set<MagicRune> runeTypes = RUNE_ITEMS.get(itemId);
		if (runeTypes != null)
		{
			return runeTypes;
		}
		return RUNE_ITEMS.getOrDefault(ItemVariationMapping.map(itemId), ImmutableSet.of());
	}

	static boolean provides(MagicRune rune, int itemId)
	{
		switch (rune)
		{
			case AIR:
				return AIR_PROVIDERS.contains(itemId);
			case WATER:
				return WATER_PROVIDERS.contains(itemId);
			case EARTH:
				return EARTH_PROVIDERS.contains(itemId);
			case FIRE:
				return FIRE_PROVIDERS.contains(itemId);
			case NATURE:
				return NATURE_PROVIDERS.contains(itemId);
			default:
				return false;
		}
	}

	static boolean isGodStaff(int itemId)
	{
		return GOD_STAFFS.contains(ItemVariationMapping.map(itemId)) || GOD_STAFFS.contains(itemId);
	}

	static boolean isChargedWildySceptre(int itemId)
	{
		return CHARGED_WILDY_SCEPTRES.contains(ItemVariationMapping.map(itemId)) || CHARGED_WILDY_SCEPTRES.contains(itemId);
	}

	static boolean isUnchargedWildySceptre(int itemId)
	{
		return UNCHARGED_WILDY_SCEPTRES.contains(ItemVariationMapping.map(itemId)) || UNCHARGED_WILDY_SCEPTRES.contains(itemId);
	}

	static boolean isMagicCape(int itemId)
	{
		return MAGIC_CAPES.contains(ItemVariationMapping.map(itemId)) || MAGIC_CAPES.contains(itemId);
	}

	static boolean isRunePouch(int itemId)
	{
		return RUNE_POUCHES.contains(ItemVariationMapping.map(itemId)) || RUNE_POUCHES.contains(itemId);
	}
}
