package com.wildyqol.warnings.ammo;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.ToIntFunction;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemVariationMapping;

public enum RangedAmmoRequirement
{
	ATLATL_DARTS(
		"atlatl darts",
		"atlatl darts",
		AmmoThresholds::atlatlDarts,
		ItemID.ATLATL_DART,
		ItemID.ATLATL_DART_29852
	),
	RUNE_BOLTS(
		"rune crossbow ammo",
		"rune crossbow ammo",
		AmmoThresholds::bolts,
		runeCrossbowAmmo(),
		ItemID.RUNITE_BOLTS,
		ItemID.DIAMOND_BOLTS_E,
		ItemID.DRAGONSTONE_BOLTS_E,
		ItemID.ONYX_BOLTS_E
	),
	DRAGON_BOLTS(
		"dragon crossbow ammo",
		"dragon crossbow ammo",
		AmmoThresholds::bolts,
		dragonCrossbowAmmo(),
		ItemID.RUNITE_BOLTS,
		ItemID.DIAMOND_BOLTS_E,
		ItemID.DRAGONSTONE_BOLTS_E,
		ItemID.ONYX_BOLTS_E,
		ItemID.DIAMOND_DRAGON_BOLTS_E,
		ItemID.DRAGONSTONE_DRAGON_BOLTS_E,
		ItemID.ONYX_DRAGON_BOLTS_E,
		ItemID.OPAL_DRAGON_BOLTS_E,
		ItemID.OPAL_DRAGON_BOLTS_E_27192
	),
	ANTLER_BOLTS(
		"antler bolts",
		"antler bolts",
		AmmoThresholds::bolts,
		ItemID.SUNLIGHT_ANTLER_BOLTS,
		ItemID.MOONLIGHT_ANTLER_BOLTS
	),
	DRAGON_JAVELINS(
		"dragon javelins",
		"dragon javelins",
		AmmoThresholds::javelins,
		ItemID.DRAGON_JAVELIN,
		ItemID.DRAGON_JAVELINP,
		ItemID.DRAGON_JAVELINP_19488,
		ItemID.DRAGON_JAVELINP_19490,
		ItemID.DRAGON_JAVELIN_23648
	),
	DRAGON_ARROWS(
		"dragon arrows",
		"dragon arrows",
		AmmoThresholds::arrows,
		dragonArrowBowAmmo(),
		combine(
			optimalAmmoSet(
				ItemID.DRAGON_ARROW,
				ItemID.DRAGON_ARROWP,
				ItemID.DRAGON_ARROWP_11228,
				ItemID.DRAGON_ARROWP_11229,
				ItemID.DRAGON_ARROW_20389),
			seekingArrowStack(SeekingArrowIds.DRAGON_ARROW)),
		exactOptimalAmmoSet(
			ItemID.DRAGON_ARROW,
			ItemID.DRAGON_ARROWP,
			ItemID.DRAGON_ARROWP_11228,
			ItemID.DRAGON_ARROWP_11229,
			ItemID.DRAGON_ARROW_20389)
	);

	private final String requiredText;
	private final String lowText;
	private final ToIntFunction<AmmoThresholds> threshold;
	private final Set<Integer> acceptedAmmoIds;
	private final Set<Integer> optimalAmmoIds;
	private final Set<Integer> exactOptimalAmmoIds;

	RangedAmmoRequirement(String requiredText, String lowText, ToIntFunction<AmmoThresholds> threshold, Integer... acceptedAmmoIds)
	{
		this(requiredText, lowText, threshold, ammoSet(acceptedAmmoIds), optimalAmmoSet(acceptedAmmoIds), exactOptimalAmmoSet(acceptedAmmoIds));
	}

	RangedAmmoRequirement(
		String requiredText,
		String lowText,
		ToIntFunction<AmmoThresholds> threshold,
		Set<Integer> acceptedAmmoIds,
		Integer... optimalAmmoIds)
	{
		this(requiredText, lowText, threshold, acceptedAmmoIds, optimalAmmoSet(optimalAmmoIds), exactOptimalAmmoSet(optimalAmmoIds));
	}

	RangedAmmoRequirement(
		String requiredText,
		String lowText,
		ToIntFunction<AmmoThresholds> threshold,
		Set<Integer> acceptedAmmoIds,
		Set<Integer> optimalAmmoIds,
		Set<Integer> exactOptimalAmmoIds)
	{
		this.requiredText = requiredText;
		this.lowText = lowText;
		this.threshold = threshold;
		this.acceptedAmmoIds = acceptedAmmoIds;
		this.optimalAmmoIds = optimalAmmoIds;
		this.exactOptimalAmmoIds = exactOptimalAmmoIds;
	}

	String getRequiredText()
	{
		return requiredText;
	}

	String getLowText()
	{
		return lowText;
	}

	int getThreshold(AmmoThresholds thresholds)
	{
		return threshold.applyAsInt(thresholds);
	}

	Set<Integer> getAcceptedAmmoIds()
	{
		return acceptedAmmoIds;
	}

	Set<Integer> getOptimalAmmoIds()
	{
		return optimalAmmoIds;
	}

	Set<Integer> getExactOptimalAmmoIds()
	{
		return exactOptimalAmmoIds;
	}

	private static Set<Integer> runeCrossbowAmmo()
	{
		return combine(
			bronzeBolts(),
			bluriteBolts(),
			ironBolts(),
			steelBolts(),
			mithrilBolts(),
			adamantBolts(),
			runiteBolts());
	}

	private static Set<Integer> dragonCrossbowAmmo()
	{
		return combine(
			runeCrossbowAmmo(),
			dragonBolts());
	}

	private static Set<Integer> dragonArrowBowAmmo()
	{
		return combine(
			bronzeArrows(),
			ironArrows(),
			steelArrows(),
			mithrilArrows(),
			adamantArrows(),
			runeArrows(),
			amethystArrows(),
			dragonArrows());
	}

	private static Set<Integer> bronzeBolts()
	{
		return ammoSet(
			ItemID.BRONZE_BOLTS,
			ItemID.OPAL_BOLTS_E);
	}

	private static Set<Integer> bluriteBolts()
	{
		return ammoSet(
			ItemID.BLURITE_BOLTS,
			ItemID.JADE_BOLTS_E);
	}

	private static Set<Integer> ironBolts()
	{
		return ammoSet(
			ItemID.IRON_BOLTS,
			ItemID.SILVER_BOLTS,
			ItemID.PEARL_BOLTS_E);
	}

	private static Set<Integer> steelBolts()
	{
		return ammoSet(
			ItemID.STEEL_BOLTS,
			ItemID.TOPAZ_BOLTS_E);
	}

	private static Set<Integer> mithrilBolts()
	{
		return ammoSet(
			ItemID.MITHRIL_BOLTS,
			ItemID.SAPPHIRE_BOLTS_E,
			ItemID.EMERALD_BOLTS_E);
	}

	private static Set<Integer> adamantBolts()
	{
		return ammoSet(
			ItemID.ADAMANT_BOLTS,
			ItemID.RUBY_BOLTS_E,
			ItemID.DIAMOND_BOLTS_E);
	}

	private static Set<Integer> runiteBolts()
	{
		return ammoSet(
			ItemID.RUNITE_BOLTS,
			ItemID.DRAGONSTONE_BOLTS_E,
			ItemID.ONYX_BOLTS_E,
			ItemID.BROAD_BOLTS,
			ItemID.AMETHYST_BROAD_BOLTS);
	}

	private static Set<Integer> dragonBolts()
	{
		return ammoSet(
			ItemID.DRAGON_BOLTS,
			ItemID.OPAL_DRAGON_BOLTS_E,
			ItemID.JADE_DRAGON_BOLTS_E,
			ItemID.PEARL_DRAGON_BOLTS_E,
			ItemID.TOPAZ_DRAGON_BOLTS_E,
			ItemID.SAPPHIRE_DRAGON_BOLTS_E,
			ItemID.EMERALD_DRAGON_BOLTS_E,
			ItemID.RUBY_DRAGON_BOLTS_E,
			ItemID.DIAMOND_DRAGON_BOLTS_E,
			ItemID.DRAGONSTONE_DRAGON_BOLTS_E,
			ItemID.ONYX_DRAGON_BOLTS_E);
	}

	private static Set<Integer> bronzeArrows()
	{
		return combine(
			ammoSet(ItemID.BRONZE_ARROW),
			seekingArrowStack(SeekingArrowIds.BRONZE_ARROW));
	}

	private static Set<Integer> ironArrows()
	{
		return combine(
			ammoSet(ItemID.IRON_ARROW),
			seekingArrowStack(SeekingArrowIds.IRON_ARROW));
	}

	private static Set<Integer> steelArrows()
	{
		return combine(
			ammoSet(ItemID.STEEL_ARROW),
			seekingArrowStack(SeekingArrowIds.STEEL_ARROW));
	}

	private static Set<Integer> mithrilArrows()
	{
		return combine(
			ammoSet(ItemID.MITHRIL_ARROW),
			seekingArrowStack(SeekingArrowIds.MITHRIL_ARROW));
	}

	private static Set<Integer> adamantArrows()
	{
		return combine(
			ammoSet(ItemID.ADAMANT_ARROW),
			seekingArrowStack(SeekingArrowIds.ADAMANT_ARROW));
	}

	private static Set<Integer> runeArrows()
	{
		return combine(
			ammoSet(ItemID.RUNE_ARROW),
			seekingArrowStack(SeekingArrowIds.RUNE_ARROW));
	}

	private static Set<Integer> amethystArrows()
	{
		return combine(
			ammoSet(ItemID.AMETHYST_ARROW),
			seekingArrowStack(SeekingArrowIds.AMETHYST_ARROW));
	}

	private static Set<Integer> dragonArrows()
	{
		return combine(
			ammoSet(ItemID.DRAGON_ARROW),
			seekingArrowStack(SeekingArrowIds.DRAGON_ARROW));
	}

	private static Set<Integer> ammoSet(Integer... itemIds)
	{
		ImmutableSet.Builder<Integer> ammoIds = ImmutableSet.builder();
		for (int itemId : itemIds)
		{
			ammoIds.add(ItemVariationMapping.map(itemId));
		}
		return ammoIds.build();
	}

	private static Set<Integer> optimalAmmoSet(Integer... itemIds)
	{
		ImmutableSet.Builder<Integer> ammoIds = ImmutableSet.builder();
		for (int itemId : itemIds)
		{
			if (!isExactOptimalAmmo(itemId))
			{
				ammoIds.add(ItemVariationMapping.map(itemId));
			}
		}
		return ammoIds.build();
	}

	private static Set<Integer> exactOptimalAmmoSet(Integer... itemIds)
	{
		ImmutableSet.Builder<Integer> ammoIds = ImmutableSet.builder();
		for (int itemId : itemIds)
		{
			if (isExactOptimalAmmo(itemId))
			{
				ammoIds.add(itemId);
			}
		}
		return ammoIds.build();
	}

	private static Set<Integer> seekingArrowStack(int firstItemId)
	{
		ImmutableSet.Builder<Integer> ammoIds = ImmutableSet.builder();
		for (int itemId = firstItemId; itemId < firstItemId + SeekingArrowIds.STACK_VARIANTS; itemId++)
		{
			ammoIds.add(ItemVariationMapping.map(itemId));
		}
		return ammoIds.build();
	}

	private static boolean isExactOptimalAmmo(int itemId)
	{
		switch (itemId)
		{
			case ItemID.OPAL_BOLTS_E:
			case ItemID.JADE_BOLTS_E:
			case ItemID.PEARL_BOLTS_E:
			case ItemID.TOPAZ_BOLTS_E:
			case ItemID.SAPPHIRE_BOLTS_E:
			case ItemID.EMERALD_BOLTS_E:
			case ItemID.RUBY_BOLTS_E:
			case ItemID.DIAMOND_BOLTS_E:
			case ItemID.DRAGONSTONE_BOLTS_E:
			case ItemID.ONYX_BOLTS_E:
			case ItemID.OPAL_DRAGON_BOLTS_E:
			case ItemID.OPAL_DRAGON_BOLTS_E_27192:
			case ItemID.JADE_DRAGON_BOLTS_E:
			case ItemID.PEARL_DRAGON_BOLTS_E:
			case ItemID.TOPAZ_DRAGON_BOLTS_E:
			case ItemID.SAPPHIRE_DRAGON_BOLTS_E:
			case ItemID.EMERALD_DRAGON_BOLTS_E:
			case ItemID.RUBY_DRAGON_BOLTS_E:
			case ItemID.DIAMOND_DRAGON_BOLTS_E:
			case ItemID.DRAGONSTONE_DRAGON_BOLTS_E:
			case ItemID.ONYX_DRAGON_BOLTS_E:
				return true;
			default:
				return false;
		}
	}

	@SafeVarargs
	private static Set<Integer> combine(Set<Integer>... ammoSets)
	{
		ImmutableSet.Builder<Integer> combined = ImmutableSet.builder();
		for (Set<Integer> ammoSet : ammoSets)
		{
			combined.addAll(ammoSet);
		}
		return combined.build();
	}
}
