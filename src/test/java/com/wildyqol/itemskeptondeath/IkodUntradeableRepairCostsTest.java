package com.wildyqol.itemskeptondeath;

import net.runelite.api.gameval.ItemID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class IkodUntradeableRepairCostsTest
{
	@Test
	public void usesBrokenRepairCostBelowLevel20()
	{
		assertEquals(150_000L, cost(ItemID.TZHAAR_CAPE_FIRE_TROUVER, "Fire cape (l)", false));
		assertEquals(600_000L, cost(ItemID.INFERNAL_DEFENDER_TROUVER, "Avernic defender (l)", false));
		assertEquals(250_000L, cost(ItemID.ELITE_VOID_KNIGHT_TOP_TROUVER, "Elite void top (l)", false));
	}

	@Test
	public void usesMangledCostAboveLevel20ForMangledItems()
	{
		assertEquals(500_000L, cost(ItemID.INFERNAL_CAPE_TROUVER, "Infernal cape (l)", true));
		assertEquals(500_000L, cost(ItemID.DIZANAS_QUIVER_INFINITE_TROUVER, "Blessed dizana's quiver (l)", true));
		assertEquals(500_000L, cost(ItemID.BARBASSAULT_PENANCE_FIGHTER_TORSO_TROUVER, "Fighter torso (l)", true));
		assertEquals(500_000L, cost(ItemID.BH_BARBASSAULT_PENANCE_FIGHTER_TORSO_CORRUPTED_TROUVER, "Fighter torso (or) (l)", true));
		assertEquals(500_000L, cost(ItemID.BH_RUNE_POUCH_TROUVER, "Rune pouch (l)", true));
		assertEquals(500_000L, cost(ItemID.DIVINE_RUNE_POUCH_TROUVER, "Divine rune pouch (l)", true));
		assertEquals(500_000L, cost(ItemID.LEAGUE_3_VOID_KNIGHT_TOP_ELITE_TROUVER, "Elite void top (or) (l)", true));
	}

	@Test
	public void onlyCountsItemsThatWillActuallyBreakOrMangle()
	{
		assertFalse(IkodParchmentRiskService.isRepairCostCandidate(
			0,
			ItemID.INFERNAL_CAPE_TROUVER,
			"Infernal cape (l)",
			ItemID.INFERNAL_CAPE_TROUVER,
			"Infernal cape (l)"));
		assertTrue(IkodParchmentRiskService.isRepairCostCandidate(
			2,
			ItemID.DIZANAS_QUIVER_INFINITE_TROUVER,
			"Blessed dizana's quiver (l)",
			ItemID.DIZANAS_QUIVER_INFINITE_TROUVER,
			"Blessed dizana's quiver (l)"));
		assertTrue(IkodParchmentRiskService.isRepairCostCandidate(
			0,
			ItemID.INFERNAL_CAPE_TROUVER_MANGLED,
			"Infernal cape (l) (mangled)",
			ItemID.INFERNAL_CAPE_TROUVER_MANGLED,
			"Infernal cape (l) (mangled)"));
		assertTrue(IkodParchmentRiskService.isRepairCostCandidate(
			0,
			ItemID.TZHAAR_CAPE_FIRE_BROKEN,
			"Fire cape (broken)",
			ItemID.TZHAAR_CAPE_FIRE_BROKEN,
			"Fire cape (broken)"));
		assertFalse(IkodParchmentRiskService.isRepairCostCandidate(
			2,
			ItemID.COINS,
			"Coins",
			ItemID.COINS,
			"Coins"));
	}

	@Test
	public void recognizesSelectedDeathkeepScenario()
	{
		assertEquals(Boolean.TRUE, IkodParchmentRiskService.parseSelectedDepth(
			"<col=ff981f>Wilderness beyond level 20</col>"));
		assertEquals(Boolean.FALSE, IkodParchmentRiskService.parseSelectedDepth("Killed by a player"));
		assertEquals(Boolean.FALSE, IkodParchmentRiskService.parseSelectedDepth("Below level 20 Wilderness"));
		assertNull(IkodParchmentRiskService.parseSelectedDepth(
			"Killed by a player | Wilderness beyond level 20"));
		assertNull(IkodParchmentRiskService.parseSelectedDepth(""));
	}

	@Test
	public void recognizesCurrentWildernessLevelWidgetText()
	{
		assertTrue(IkodParchmentRiskService.isAboveLevel20("Level: 21"));
		assertTrue(IkodParchmentRiskService.isAboveLevel20("<col=ff0000>Level: 42</col>"));
		assertFalse(IkodParchmentRiskService.isAboveLevel20("Level: 20"));
		assertFalse(IkodParchmentRiskService.isAboveLevel20(""));
	}

	@Test
	public void runePouchesUsePerduFeeBelowLevel20()
	{
		assertEquals(500_000L, cost(ItemID.BH_RUNE_POUCH_TROUVER, "Rune pouch (l)", false));
		assertEquals(500_000L, cost(ItemID.DIVINE_RUNE_POUCH_TROUVER, "Divine rune pouch (l)", false));
		assertTrue(IkodUntradeableRepairCosts.hasRepairCost(ItemID.BH_RUNE_POUCH_TROUVER, "Rune pouch (l)"));
		assertTrue(IkodUntradeableRepairCosts.hasRepairCost(ItemID.DIVINE_RUNE_POUCH_TROUVER, "Divine rune pouch (l)"));
	}

	@Test
	public void keepsBrokenCostAboveLevel20ForItemsThatStillBreak()
	{
		assertEquals(240_000L, cost(ItemID.AVAS_ASSEMBLER_TROUVER, "Ava's assembler (l)", true));
		assertEquals(35_000L, cost(ItemID.RUNE_PARRYINGDAGGER_TROUVER, "Rune defender (l)", true));
		assertEquals(240_000L, cost(ItemID.DRAGON_PARRYINGDAGGER_T_TROUVER, "Dragon defender (t) (l)", true));
		assertEquals(25_000L, cost(ItemID.ARMADYL_HALO_TROUVER, "Armadyl halo (l)", true));
	}

	@Test
	public void supportsVoidOrnamentKitVariants()
	{
		assertEquals(160_000L, cost(ItemID.LEAGUE_3_VOID_RANGE_HELM_TROUVER, "Void ranger helm (or) (l)", false));
		assertEquals(180_000L, cost(ItemID.LEAGUE_3_VOID_KNIGHT_ROBES_TROUVER, "Void knight robe (or) (l)", false));
		assertEquals(120_000L, cost(ItemID.LEAGUE_3_VOID_KNIGHT_GLOVES_TROUVER, "Void knight gloves (or) (l)", false));
	}

	@Test
	public void ancientSceptreBrokenCostIncludesAncientStaffPrice()
	{
		assertEquals(323_456L, cost(ItemID.ANCIENT_SCEPTRE, "Ancient sceptre", false));
		assertEquals(323_456L, cost(ItemID.ANCIENT_SCEPTRE_TROUVER, "Ancient sceptre (l)", false));
		assertEquals(323_456L, cost(ItemID.ANCIENT_SCEPTRE_BLOOD_BROKEN, "Blood ancient sceptre (broken)", false));
		assertEquals(323_456L, cost(ItemID.ANCIENT_SCEPTRE_BLOOD_TROUVER_BROKEN, "Blood ancient sceptre (l) (broken)", false));
		assertEquals(500_000L, cost(ItemID.ANCIENT_SCEPTRE_TROUVER, "Ancient sceptre (l)", true));
		assertEquals(500_000L, cost(ItemID.ANCIENT_SCEPTRE_BLOOD_TROUVER, "Blood ancient sceptre (l)", true));
		assertEquals(500_000L, cost(ItemID.ANCIENT_SCEPTRE_BLOOD_TROUVER_MANGLED, "Blood ancient sceptre (l) (mangled)", false));
	}

	@Test
	public void unparchmentedHigherTierItemsHaveNoRepairCostAboveLevel20()
	{
		assertEquals(0L, cost(ItemID.INFERNAL_CAPE, "Infernal cape", true));
		assertEquals(0L, cost(ItemID.SKILLCAPE_MAX_INFERNALCAPE, "Infernal max cape", true));
		assertEquals(0L, cost(ItemID.BH_RUNE_POUCH, "Rune pouch", true));
		assertEquals(0L, cost(ItemID.DIVINE_RUNE_POUCH, "Divine rune pouch", true));
		assertEquals(0L, cost(ItemID.ELITE_VOID_KNIGHT_TOP, "Elite void top", true));
		assertEquals(0L, cost(ItemID.LEAGUE_3_VOID_KNIGHT_TOP_ELITE, "Elite void top (or)", true));
		assertEquals(0L, cost(ItemID.BARBASSAULT_PENANCE_FIGHTER_TORSO, "Fighter torso", true));
		assertEquals(0L, cost(ItemID.ANCIENT_SCEPTRE, "Ancient sceptre", true));
		assertEquals(0L, cost(ItemID.ANCIENT_SCEPTRE_BLOOD, "Blood ancient sceptre", true));
		assertEquals(0L, cost(ItemID.DIZANAS_QUIVER_CHARGED, "Dizana's quiver", true));
		assertEquals(0L, cost(ItemID.SKILLCAPE_MAX_DIZANAS, "Dizana's max cape", true));
	}

	@Test
	public void supportsExplicitTrouverBrokenAndMangledItemIds()
	{
		assertEquals(225_000L, cost(ItemID.INFERNAL_CAPE_TROUVER_BROKEN, "Infernal cape (l) (broken)", false));
		assertEquals(500_000L, cost(ItemID.INFERNAL_CAPE_TROUVER_MANGLED, "Infernal cape (l) (mangled)", false));
		assertEquals(160_000L, cost(ItemID.GAME_PEST_MELEE_HELM_TROUVER_BROKEN, "Void melee helm (l) (broken)", false));
		assertEquals(500_000L, cost(ItemID.GAME_PEST_MELEE_HELM_TROUVER_MANGLED, "Void melee helm (l) (mangled)", false));
		assertEquals(150_000L, cost(ItemID.BARBASSAULT_PENANCE_FIGHTER_TORSO_TROUVER_BROKEN, "Fighter torso (l) (broken)", false));
		assertEquals(500_000L, cost(ItemID.BARBASSAULT_PENANCE_FIGHTER_TORSO_TROUVER_MANGLED, "Fighter torso (l) (mangled)", false));
		assertEquals(270_000L, cost(ItemID.DIZANAS_QUIVER_TROUVER_BROKEN, "Dizana's quiver (l) (broken)", false));
		assertEquals(500_000L, cost(ItemID.DIZANAS_QUIVER_TROUVER_MANGLED, "Dizana's quiver (l) (mangled)", false));
	}

	@Test
	public void displayedMangledNameUsesFixedCostWithoutCompileTimeMangledIds()
	{
		assertEquals(500_000L, cost(ItemID.INFERNAL_CAPE_TROUVER, "Infernal cape (l) (mangled)", false));
		assertTrue(IkodUntradeableRepairCosts.isDisplayedBrokenOrMangled("Infernal cape (l) (mangled)"));
	}

	@Test
	public void unknownItemsHaveNoRepairCost()
	{
		assertEquals(0L, cost(ItemID.COINS, "Coins", true));
		assertFalse(IkodUntradeableRepairCosts.hasRepairCost(ItemID.COINS, "Coins"));
	}

	private static long cost(int itemId, String itemName, boolean aboveLevel20)
	{
		return IkodUntradeableRepairCosts.getRepairCost(
			itemId,
			itemName,
			aboveLevel20,
			priceItemId -> priceItemId == ItemID.STAFF_OF_ZAROS ? 123_456L : 0L);
	}
}
