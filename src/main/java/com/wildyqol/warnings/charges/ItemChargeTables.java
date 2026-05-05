package com.wildyqol.warnings.charges;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.runelite.api.ItemID;

final class ItemChargeTables
{
	private static final Map<ItemChargeKind, Set<Integer>> CHARGED_ITEMS = ImmutableMap.<ItemChargeKind, Set<Integer>>builder()
		.put(ItemChargeKind.BOWFA, buildSet(ItemID.BOW_OF_FAERDHINEN))
		.put(ItemChargeKind.SERPENTINE_HELM, buildSet(
			ItemID.SERPENTINE_HELM,
			ItemID.TANZANITE_HELM,
			ItemID.MAGMA_HELM))
		.put(ItemChargeKind.TOXIC_STAFF, buildSet(
			ItemID.TOXIC_STAFF_OF_THE_DEAD,
			ItemID.TOXIC_STAFF_DEADMAN))
		.put(ItemChargeKind.ACCURSED_THAMMARONS, buildSet(
			ItemID.THAMMARONS_SCEPTRE,
			ItemID.THAMMARONS_SCEPTRE_A,
			ItemID.ACCURSED_SCEPTRE,
			ItemID.ACCURSED_SCEPTRE_A))
		.put(ItemChargeKind.CRAWS_WEBWEAVER, buildSet(
			ItemID.CRAWS_BOW,
			ItemID.WEBWEAVER_BOW))
		.put(ItemChargeKind.URSINE_VIGGORAS, buildSet(
			ItemID.VIGGORAS_CHAINMACE,
			ItemID.URSINE_CHAINMACE))
		.put(ItemChargeKind.TOME_OF_FIRE, buildSet(
			ItemID.TOME_OF_FIRE,
			ItemID.TOME_OF_FIRE_27358))
		.put(ItemChargeKind.TOME_OF_WATER, buildSet(ItemID.TOME_OF_WATER))
		.put(ItemChargeKind.TOME_OF_EARTH, buildSet(ItemID.TOME_OF_EARTH))
		.build();

	private static final Map<ItemChargeKind, Set<Integer>> UNCHARGED_ITEMS = ImmutableMap.<ItemChargeKind, Set<Integer>>builder()
		.put(ItemChargeKind.BOWFA, buildSet(ItemID.BOW_OF_FAERDHINEN_INACTIVE))
		.put(ItemChargeKind.SERPENTINE_HELM, buildSet(
			ItemID.SERPENTINE_HELM_UNCHARGED,
			ItemID.TANZANITE_HELM_UNCHARGED,
			ItemID.MAGMA_HELM_UNCHARGED))
		.put(ItemChargeKind.TOXIC_STAFF, buildSet(
			ItemID.TOXIC_STAFF_UNCHARGED,
			ItemID.TOXIC_STAFF_UNCHARGED_33035))
		.put(ItemChargeKind.ACCURSED_THAMMARONS, buildSet(
			ItemID.THAMMARONS_SCEPTRE_U,
			ItemID.THAMMARONS_SCEPTRE_AU,
			ItemID.ACCURSED_SCEPTRE_U,
			ItemID.ACCURSED_SCEPTRE_AU))
		.put(ItemChargeKind.CRAWS_WEBWEAVER, buildSet(
			ItemID.CRAWS_BOW_U,
			ItemID.WEBWEAVER_BOW_U))
		.put(ItemChargeKind.URSINE_VIGGORAS, buildSet(
			ItemID.VIGGORAS_CHAINMACE_U,
			ItemID.URSINE_CHAINMACE_U))
		.put(ItemChargeKind.TOME_OF_FIRE, buildSet(ItemID.TOME_OF_FIRE_EMPTY))
		.put(ItemChargeKind.TOME_OF_WATER, buildSet(ItemID.TOME_OF_WATER_EMPTY))
		.put(ItemChargeKind.TOME_OF_EARTH, buildSet(ItemID.TOME_OF_EARTH_EMPTY))
		.put(ItemChargeKind.DRAGONFIRE_SHIELD, buildSet(ItemID.DRAGONFIRE_SHIELD_11284))
		.put(ItemChargeKind.DRAGONFIRE_WARD, buildSet(ItemID.DRAGONFIRE_WARD_22003))
		.put(ItemChargeKind.ANCIENT_WYVERN_SHIELD, buildSet(ItemID.ANCIENT_WYVERN_SHIELD_21634))
		.build();

	private static final Set<Integer> IGNORED_BOWFAS = buildSet(
		ItemID.BOW_OF_FAERDHINEN_C,
		ItemID.BOW_OF_FAERDHINEN_C_25869,
		ItemID.BOW_OF_FAERDHINEN_C_25884,
		ItemID.BOW_OF_FAERDHINEN_C_25886,
		ItemID.BOW_OF_FAERDHINEN_C_25888,
		ItemID.BOW_OF_FAERDHINEN_C_25890,
		ItemID.BOW_OF_FAERDHINEN_C_25892,
		ItemID.BOW_OF_FAERDHINEN_C_25894,
		ItemID.BOW_OF_FAERDHINEN_C_25896,
		ItemID.BOW_OF_FAERDHINEN_C_27187,
		ItemID.BOW_OF_FAERDHINEN_C_33021);

	private ItemChargeTables()
	{
	}

	private static Set<Integer> buildSet(Integer... itemIds)
	{
		return ImmutableSet.copyOf(itemIds);
	}

	@Nullable
	static ItemChargeKind getChargedKind(int itemId)
	{
		return getKind(CHARGED_ITEMS, itemId);
	}

	@Nullable
	static ItemChargeKind getUnchargedKind(int itemId)
	{
		return getKind(UNCHARGED_ITEMS, itemId);
	}

	static boolean isIgnoredBowfa(int itemId)
	{
		return IGNORED_BOWFAS.contains(itemId);
	}

	@Nullable
	private static ItemChargeKind getKind(Map<ItemChargeKind, Set<Integer>> items, int itemId)
	{
		for (Map.Entry<ItemChargeKind, Set<Integer>> entry : items.entrySet())
		{
			if (entry.getValue().contains(itemId))
			{
				return entry.getKey();
			}
		}
		return null;
	}
}
