package com.wildyqol.warnings.ammo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemVariationMapping;

final class RangedAmmoTables
{
	private static final Set<Integer> QUIVERS = ImmutableSet.<Integer>builder()
		.addAll(ItemVariationMapping.getVariations(ItemVariationMapping.map(net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_CHARGED)))
		.addAll(ItemVariationMapping.getVariations(ItemVariationMapping.map(net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_INFINITE)))
		.addAll(ItemVariationMapping.getVariations(ItemVariationMapping.map(net.runelite.api.gameval.ItemID.SKILLCAPE_MAX_DIZANAS)))
		.build();

	private static final Set<Integer> BOWFA_CHARGED = ImmutableSet.of(
		ItemID.BOW_OF_FAERDHINEN
	);

	private static final Set<Integer> BOWFA_INACTIVE = ImmutableSet.of(
		ItemID.BOW_OF_FAERDHINEN_INACTIVE
	);

	private static final Set<Integer> BOWFA_CORRUPTED = ImmutableSet.of(
		ItemID.BOW_OF_FAERDHINEN_C,
		ItemID.BOW_OF_FAERDHINEN_C_25869,
		ItemID.BOW_OF_FAERDHINEN_C_25884,
		ItemID.BOW_OF_FAERDHINEN_C_25886,
		ItemID.BOW_OF_FAERDHINEN_C_25888,
		ItemID.BOW_OF_FAERDHINEN_C_25890,
		ItemID.BOW_OF_FAERDHINEN_C_25892,
		ItemID.BOW_OF_FAERDHINEN_C_25894,
		ItemID.BOW_OF_FAERDHINEN_C_25896
	);

	private static final Map<Integer, RangedAmmoRequirement> WEAPON_REQUIREMENTS = ImmutableMap.<Integer, RangedAmmoRequirement>builder()
		.put(ItemID.ECLIPSE_ATLATL, RangedAmmoRequirement.ATLATL_DARTS)
		.put(ItemID.ECLIPSE_ATLATL_29851, RangedAmmoRequirement.ATLATL_DARTS)
		.put(ItemID.RUNE_CROSSBOW, RangedAmmoRequirement.RUNE_BOLTS)
		.put(ItemID.RUNE_CROSSBOW_OR, RangedAmmoRequirement.RUNE_BOLTS)
		.put(ItemID.RUNE_CROSSBOW_23601, RangedAmmoRequirement.RUNE_BOLTS)
		.put(ItemID.DRAGON_CROSSBOW, RangedAmmoRequirement.DRAGON_BOLTS)
		.put(ItemID.DRAGON_CROSSBOW_CR, RangedAmmoRequirement.DRAGON_BOLTS)
		.put(ItemID.ARMADYL_CROSSBOW, RangedAmmoRequirement.DRAGON_BOLTS)
		.put(ItemID.ARMADYL_CROSSBOW_23611, RangedAmmoRequirement.DRAGON_BOLTS)
		.put(ItemID.ZARYTE_CROSSBOW, RangedAmmoRequirement.DRAGON_BOLTS)
		.put(ItemID.ZARYTE_CROSSBOW_27186, RangedAmmoRequirement.DRAGON_BOLTS)
		.put(ItemID.DRAGON_HUNTER_CROSSBOW, RangedAmmoRequirement.DRAGON_BOLTS)
		.put(ItemID.DRAGON_HUNTER_CROSSBOW_T, RangedAmmoRequirement.DRAGON_BOLTS)
		.put(ItemID.DRAGON_HUNTER_CROSSBOW_B, RangedAmmoRequirement.DRAGON_BOLTS)
		.put(ItemID.HUNTERS_SUNLIGHT_CROSSBOW, RangedAmmoRequirement.ANTLER_BOLTS)
		.put(ItemID.HEAVY_BALLISTA, RangedAmmoRequirement.DRAGON_JAVELINS)
		.put(ItemID.HEAVY_BALLISTA_23630, RangedAmmoRequirement.DRAGON_JAVELINS)
		.put(ItemID.HEAVY_BALLISTA_OR, RangedAmmoRequirement.DRAGON_JAVELINS)
		.put(ItemID.LIGHT_BALLISTA, RangedAmmoRequirement.DRAGON_JAVELINS)
		.put(ItemID.LIGHT_BALLISTA_27188, RangedAmmoRequirement.DRAGON_JAVELINS)
		.put(ItemID.DARK_BOW, RangedAmmoRequirement.DRAGON_ARROWS)
		.put(ItemID.DARK_BOW_12765, RangedAmmoRequirement.DRAGON_ARROWS)
		.put(ItemID.DARK_BOW_12766, RangedAmmoRequirement.DRAGON_ARROWS)
		.put(ItemID.DARK_BOW_12767, RangedAmmoRequirement.DRAGON_ARROWS)
		.put(ItemID.DARK_BOW_12768, RangedAmmoRequirement.DRAGON_ARROWS)
		.put(ItemID.DARK_BOW_20408, RangedAmmoRequirement.DRAGON_ARROWS)
		.put(ItemID.DARK_BOW_BH, RangedAmmoRequirement.DRAGON_ARROWS)
		.put(ItemID.DARK_BOW_DEADMAN, RangedAmmoRequirement.DRAGON_ARROWS)
		.put(ItemID.CORRUPTED_DARK_BOW, RangedAmmoRequirement.DRAGON_ARROWS)
		.put(ItemID.SCORCHING_BOW, RangedAmmoRequirement.DRAGON_ARROWS)
		.build();

	private static final Set<Integer> SUPPORTED_AMMO_IDS = buildSupportedAmmoIds();

	private RangedAmmoTables()
	{
	}

	private static Set<Integer> buildSupportedAmmoIds()
	{
		ImmutableSet.Builder<Integer> ammoIds = ImmutableSet.builder();
		for (RangedAmmoRequirement requirement : RangedAmmoRequirement.values())
		{
			for (int itemId : requirement.getAcceptedAmmoIds())
			{
				ammoIds.add(itemId);
				ammoIds.add(ItemVariationMapping.map(itemId));
			}
		}
		return ammoIds.build();
	}

	static boolean isQuiver(int itemId)
	{
		return QUIVERS.contains(itemId);
	}

	static RangedAmmoRequirement getRequirement(int weaponId)
	{
		return WEAPON_REQUIREMENTS.get(ItemVariationMapping.map(weaponId));
	}

	static boolean isSupportedAmmo(int itemId)
	{
		return SUPPORTED_AMMO_IDS.contains(itemId) || SUPPORTED_AMMO_IDS.contains(ItemVariationMapping.map(itemId));
	}

	static boolean isBowfaWithCharges(int itemId)
	{
		return BOWFA_CHARGED.contains(itemId);
	}

	static boolean isInactiveBowfa(int itemId)
	{
		return BOWFA_INACTIVE.contains(itemId);
	}

	static boolean isCorruptedBowfa(int itemId)
	{
		return BOWFA_CORRUPTED.contains(itemId);
	}
}
