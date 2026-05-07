package com.wildyqol.warnings.magic;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.game.ItemVariationMapping;

public class MagicInventoryStateBuilder
{
	private static final int[] RUNE_POUCH_TYPE_VARBITS = {
		VarbitID.RUNE_POUCH_TYPE_1,
		VarbitID.RUNE_POUCH_TYPE_2,
		VarbitID.RUNE_POUCH_TYPE_3,
		VarbitID.RUNE_POUCH_TYPE_4,
		VarbitID.RUNE_POUCH_TYPE_5,
		VarbitID.RUNE_POUCH_TYPE_6
	};
	private static final int[] RUNE_POUCH_QUANTITY_VARBITS = {
		VarbitID.RUNE_POUCH_QUANTITY_1,
		VarbitID.RUNE_POUCH_QUANTITY_2,
		VarbitID.RUNE_POUCH_QUANTITY_3,
		VarbitID.RUNE_POUCH_QUANTITY_4,
		VarbitID.RUNE_POUCH_QUANTITY_5,
		VarbitID.RUNE_POUCH_QUANTITY_6
	};
	private static final int[] RUNE_POUCH_ITEM_IDS = {
		0,
		ItemID.AIR_RUNE,
		ItemID.WATER_RUNE,
		ItemID.EARTH_RUNE,
		ItemID.FIRE_RUNE,
		ItemID.MIND_RUNE,
		ItemID.CHAOS_RUNE,
		ItemID.DEATH_RUNE,
		ItemID.BLOOD_RUNE,
		ItemID.COSMIC_RUNE,
		ItemID.NATURE_RUNE,
		ItemID.LAW_RUNE,
		ItemID.BODY_RUNE,
		ItemID.SOUL_RUNE,
		ItemID.ASTRAL_RUNE,
		ItemID.MIST_RUNE,
		ItemID.MUD_RUNE,
		ItemID.DUST_RUNE,
		ItemID.LAVA_RUNE,
		ItemID.STEAM_RUNE,
		ItemID.SMOKE_RUNE,
		ItemID.WRATH_RUNE
	};

	private final Client client;

	public MagicInventoryStateBuilder(Client client)
	{
		this.client = client;
	}

	public MagicInventoryState build()
	{
		StateBuilder builder = new StateBuilder();
		collectItems(builder);
		if (builder.runePouch)
		{
			collectRunePouch(builder);
		}
		return new MagicInventoryState(
			currentSpellbook(),
			client.getBoostedSkillLevel(Skill.MAGIC),
			builder.itemIds.build(),
			ImmutableMap.copyOf(builder.runeCounts),
			ImmutableMap.copyOf(builder.itemCounts),
			ImmutableSet.copyOf(builder.providedRunes),
			builder.magicCape,
			builder.validGodStaff,
			builder.chargedWildySceptre,
			builder.unchargedWildySceptre);
	}

	public static boolean isTrackedVarbit(int varbitId)
	{
		return varbitId == VarbitID.SPELLBOOK
			|| varbitId == VarbitID.CHARGES_TOME_OF_WATER_QUANTITY
			|| varbitId == VarbitID.CHARGES_TOME_OF_FIRE_QUANTITY
			|| varbitId == VarbitID.CHARGES_TOME_OF_EARTH_QUANTITY
			|| isRunePouchVarbit(varbitId);
	}

	private void collectItems(StateBuilder builder)
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
		if (equipment != null)
		{
			for (Item item : equipment.getItems())
			{
				addItem(builder, item);
			}
		}

		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory != null)
		{
			for (Item item : inventory.getItems())
			{
				addItem(builder, item);
			}
		}
	}

	private void addItem(StateBuilder builder, @Nullable Item item)
	{
		if (item == null || item.getId() <= 0 || item.getQuantity() <= 0)
		{
			return;
		}

		int itemId = item.getId();
		builder.itemIds.add(itemId);
		builder.itemCounts.merge(ItemVariationMapping.map(itemId), item.getQuantity(), Integer::sum);
		addRuneItem(builder, itemId, item.getQuantity());
		addProviders(builder, itemId);

		builder.magicCape |= MagicItemTables.isMagicCape(itemId);
		builder.validGodStaff |= MagicItemTables.isGodStaff(itemId);
		builder.chargedWildySceptre |= MagicItemTables.isChargedWildySceptre(itemId);
		builder.unchargedWildySceptre |= MagicItemTables.isUnchargedWildySceptre(itemId);
		builder.runePouch |= MagicItemTables.isRunePouch(itemId);
	}

	private void addRuneItem(StateBuilder builder, int itemId, int quantity)
	{
		for (MagicRune rune : MagicItemTables.getRuneTypes(itemId))
		{
			builder.runeCounts.merge(rune, quantity, Integer::sum);
		}
	}

	private void addProviders(StateBuilder builder, int itemId)
	{
		for (MagicRune rune : MagicRune.values())
		{
			if (MagicItemTables.provides(rune, itemId) || chargedTomeProvides(rune, itemId))
			{
				builder.providedRunes.add(rune);
			}
		}
	}

	private boolean chargedTomeProvides(MagicRune rune, int itemId)
	{
		switch (rune)
		{
			case WATER:
				return isChargedTome(itemId, ItemID.TOME_OF_WATER, VarbitID.CHARGES_TOME_OF_WATER_QUANTITY);
			case FIRE:
				return isChargedTome(itemId, ItemID.TOME_OF_FIRE, VarbitID.CHARGES_TOME_OF_FIRE_QUANTITY)
					|| isChargedTome(itemId, ItemID.TOME_OF_FIRE_27358, VarbitID.CHARGES_TOME_OF_FIRE_QUANTITY);
			case EARTH:
				return isChargedTome(itemId, ItemID.TOME_OF_EARTH, VarbitID.CHARGES_TOME_OF_EARTH_QUANTITY);
			default:
				return false;
		}
	}

	private boolean isChargedTome(int itemId, int tomeItemId, int chargeVarbit)
	{
		return ItemVariationMapping.map(itemId) == ItemVariationMapping.map(tomeItemId)
			&& client.getVarbitValue(chargeVarbit) > 0;
	}

	private void collectRunePouch(StateBuilder builder)
	{
		for (int i = 0; i < RUNE_POUCH_TYPE_VARBITS.length; i++)
		{
			int quantity = client.getVarbitValue(RUNE_POUCH_QUANTITY_VARBITS[i]);
			if (quantity <= 0)
			{
				continue;
			}

			int itemId = runePouchItemId(client.getVarbitValue(RUNE_POUCH_TYPE_VARBITS[i]));
			if (itemId > 0)
			{
				addRuneItem(builder, itemId, quantity);
			}
		}
	}

	private int runePouchItemId(int runePouchType)
	{
		if (!MagicItemTables.getRuneTypes(runePouchType).isEmpty())
		{
			return runePouchType;
		}

		if (runePouchType >= 0 && runePouchType < RUNE_POUCH_ITEM_IDS.length)
		{
			return RUNE_POUCH_ITEM_IDS[runePouchType];
		}

		return 0;
	}

	private static boolean isRunePouchVarbit(int varbitId)
	{
		for (int runePouchVarbit : RUNE_POUCH_TYPE_VARBITS)
		{
			if (varbitId == runePouchVarbit)
			{
				return true;
			}
		}

		for (int runePouchVarbit : RUNE_POUCH_QUANTITY_VARBITS)
		{
			if (varbitId == runePouchVarbit)
			{
				return true;
			}
		}

		return false;
	}

	private MagicSpellbook currentSpellbook()
	{
		switch (client.getVarbitValue(VarbitID.SPELLBOOK))
		{
			case 0:
				return MagicSpellbook.STANDARD;
			case 1:
				return MagicSpellbook.ANCIENT;
			case 2:
				return MagicSpellbook.LUNAR;
			case 3:
				return MagicSpellbook.ARCEUUS;
			default:
				return MagicSpellbook.OTHER;
		}
	}

	private static class StateBuilder
	{
		private final ImmutableSet.Builder<Integer> itemIds = ImmutableSet.builder();
		private final EnumMap<MagicRune, Integer> runeCounts = new EnumMap<>(MagicRune.class);
		private final Map<Integer, Integer> itemCounts = new HashMap<>();
		private final EnumSet<MagicRune> providedRunes = EnumSet.noneOf(MagicRune.class);
		private boolean magicCape;
		private boolean validGodStaff;
		private boolean chargedWildySceptre;
		private boolean unchargedWildySceptre;
		private boolean runePouch;
	}
}
