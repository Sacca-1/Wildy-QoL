package com.wildyqol.warnings.ammo;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.ToIntFunction;
import net.runelite.api.ItemID;

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
		"rune crossbow bolts",
		"bolts",
		AmmoThresholds::bolts,
		ItemID.RUNITE_BOLTS,
		ItemID.DIAMOND_BOLTS_E,
		ItemID.DRAGONSTONE_BOLTS_E,
		ItemID.ONYX_BOLTS_E
	),
	DRAGON_BOLTS(
		"dragon crossbow bolts",
		"bolts",
		AmmoThresholds::bolts,
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
		"bolts",
		AmmoThresholds::bolts,
		ItemID.SUNLIGHT_ANTLER_BOLTS,
		ItemID.MOONLIGHT_ANTLER_BOLTS
	),
	DRAGON_JAVELINS(
		"dragon javelins",
		"javelins",
		AmmoThresholds::javelins,
		ItemID.DRAGON_JAVELIN,
		ItemID.DRAGON_JAVELINP,
		ItemID.DRAGON_JAVELINP_19488,
		ItemID.DRAGON_JAVELINP_19490,
		ItemID.DRAGON_JAVELIN_23648
	),
	DRAGON_ARROWS(
		"dragon arrows",
		"arrows",
		AmmoThresholds::arrows,
		ItemID.DRAGON_ARROW,
		ItemID.DRAGON_ARROWP,
		ItemID.DRAGON_ARROWP_11228,
		ItemID.DRAGON_ARROWP_11229,
		ItemID.DRAGON_ARROW_20389
	);

	private final String requiredText;
	private final String lowText;
	private final ToIntFunction<AmmoThresholds> threshold;
	private final Set<Integer> acceptedAmmoIds;

	RangedAmmoRequirement(String requiredText, String lowText, ToIntFunction<AmmoThresholds> threshold, Integer... acceptedAmmoIds)
	{
		this.requiredText = requiredText;
		this.lowText = lowText;
		this.threshold = threshold;
		this.acceptedAmmoIds = ImmutableSet.copyOf(acceptedAmmoIds);
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
}
