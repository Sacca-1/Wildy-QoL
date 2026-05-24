package com.wildyqol.warnings.charges;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GraphicID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.util.Text;

@Singleton
class ItemChargeTracker
{
	private static final String CONFIG_GROUP = "wildyqol";
	private static final int PAGE_CHARGES = 20;
	private static final int CRYSTAL_SHARD_CHARGES = 100;
	private static final int SCALE_COMBAT_CHARGE_TICKS = 90;
	private static final int COMBAT_RECENT_TICKS = 6;
	private static final int SCALE_CHECK_CONTEXT_TICKS = 3;
	private static final int SCALE_CHARGE_CONTEXT_TICKS = 300;
	private static final int BOWFA_CHARGE_CONTEXT_TICKS = 300;
	private static final int BOWFA_ATTACK_GRAPHIC = 1888;

	private static final Pattern BOWFA_CHECK = Pattern.compile(
		"Your bow of Faerdhinen has (?<charges>[\\d,.]+) charges? remaining.*",
		Pattern.CASE_INSENSITIVE);
	private static final Pattern BOWFA_AUTOCHARGE = Pattern.compile(
		"The banker charges your Bow of faerdhinen using (?<amount>[\\d,]+)x Crystal shards?.*",
		Pattern.CASE_INSENSITIVE);
	private static final Pattern TOME_OF_FIRE_CHECK = Pattern.compile(
		"Your tome has been charged with (?:Burnt|Searing) Pages\\. It currently holds (?<charges>.+) charges?\\.",
		Pattern.CASE_INSENSITIVE);
	private static final Pattern TOME_CHECK = Pattern.compile(
		"Your tome currently holds (?<charges>.+) charges?\\.",
		Pattern.CASE_INSENSITIVE);
	private static final Pattern TOME_AUTOCHARGE = Pattern.compile(
		"The banker charges your (?<item>Tome of fire|Tome of water|Tome of earth) using (?<amount>[\\d,]+)x (?<page>Burnt page|Searing page|Soaked page|Soiled page).*",
		Pattern.CASE_INSENSITIVE);
	private static final Pattern SERP_AUTOCHARGE = Pattern.compile(
		"The banker charges your (?:Serpentine helm|Tanzanite helm|Magma helm) using (?<amount>[\\d,]+)x Zulrah(?:'s)? scales.*",
		Pattern.CASE_INSENSITIVE);
	private static final Pattern TOXIC_STAFF_AUTOCHARGE = Pattern.compile(
		"The banker charges your Toxic staff of the dead using (?<amount>[\\d,]+)x Zulrah(?:'s)? scales.*",
		Pattern.CASE_INSENSITIVE);
	private static final Pattern SERP_CHECK = Pattern.compile(
		"Scales: (?<charges>[\\d,.]+) \\(.*\\)",
		Pattern.CASE_INSENSITIVE);
	private static final Pattern SCALE_CHECK = Pattern.compile(
		"Scales: (?<charges>[\\d,.]+)(?: \\(.*\\))?",
		Pattern.CASE_INSENSITIVE);

	private static final Map<ItemChargeKind, String> CONFIG_KEYS = new EnumMap<>(ItemChargeKind.class);

	static
	{
		CONFIG_KEYS.put(ItemChargeKind.BOWFA, "trackedBowfaCharges");
		CONFIG_KEYS.put(ItemChargeKind.TOME_OF_FIRE, "trackedTomeOfFireCharges");
		CONFIG_KEYS.put(ItemChargeKind.TOME_OF_WATER, "trackedTomeOfWaterCharges");
		CONFIG_KEYS.put(ItemChargeKind.TOME_OF_EARTH, "trackedTomeOfEarthCharges");
		CONFIG_KEYS.put(ItemChargeKind.TOXIC_STAFF, "trackedToxicStaffCharges");
		CONFIG_KEYS.put(ItemChargeKind.SERPENTINE_HELM, "trackedSerpentineHelmCharges");
	}

	private final Client client;
	private final ConfigManager configManager;

	private int outgoingCombatTicks;
	private int serpCombatTicks;
	private int toxicStaffCombatTicks;
	private ItemChargeKind lastScaleCheckKind;
	private int lastScaleCheckExpiryTick = -1;
	private int bowfaChargeShardCount = -1;
	private int bowfaChargeExpiryTick = -1;

	@Inject
	ItemChargeTracker(Client client, ConfigManager configManager)
	{
		this.client = client;
		this.configManager = configManager;
	}

	void onChatMessage(ChatMessage event)
	{
		if (!isTrackedMessageType(event.getType()))
		{
			return;
		}

		String message = Text.removeTags(event.getMessage());
		if (matchSet(BOWFA_CHECK, message, ItemChargeKind.BOWFA)
			|| matchIncrease(BOWFA_AUTOCHARGE, message, ItemChargeKind.BOWFA, CRYSTAL_SHARD_CHARGES)
			|| matchSet(TOME_OF_FIRE_CHECK, message, ItemChargeKind.TOME_OF_FIRE)
			|| matchSet(TOME_CHECK, message, likelyNonFireTomeKind())
			|| matchAutocharge(message)
			|| matchIncrease(SERP_AUTOCHARGE, message, ItemChargeKind.SERPENTINE_HELM)
			|| matchIncrease(TOXIC_STAFF_AUTOCHARGE, message, ItemChargeKind.TOXIC_STAFF)
			|| matchScaleCheck(message))
		{
			return;
		}

		if (message.equals("Your serpentine helm has run out of Zulrah's scales."))
		{
			setCharges(ItemChargeKind.SERPENTINE_HELM, 0);
		}
	}

	void onMenuOptionClicked(MenuOptionClicked event)
	{
		String option = Text.removeTags(event.getMenuOption());
		String target = Text.removeTags(event.getMenuTarget());
		if ("Check".equals(option))
		{
			ItemChargeKind kind = scaleItemKind(event.getItemId(), target);
			if (kind != null)
			{
				setScaleCheckContext(kind, SCALE_CHECK_CONTEXT_TICKS);
			}
		}

		if ("Use".equals(option))
		{
			if (isBowfaCrystalShardUse(target))
			{
				setBowfaChargeContext();
			}

			ItemChargeKind kind = scaleUseKind(target);
			if (kind != null)
			{
				setScaleCheckContext(kind, SCALE_CHARGE_CONTEXT_TICKS);
			}
		}
	}

	void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INV || bowfaChargeShardCount < 0)
		{
			return;
		}

		if (client.getTickCount() > bowfaChargeExpiryTick)
		{
			clearBowfaChargeContext();
			return;
		}

		int currentShardCount = countCrystalShards(event.getItemContainer());
		if (currentShardCount >= bowfaChargeShardCount)
		{
			return;
		}

		increase(ItemChargeKind.BOWFA, (bowfaChargeShardCount - currentShardCount) * CRYSTAL_SHARD_CHARGES);
		clearBowfaChargeContext();
	}

	void onGraphicChanged(GraphicChanged event)
	{
		if (event.getActor() == client.getLocalPlayer())
		{
			int graphic = event.getActor().getGraphic();
			if (isFireSpellGraphic(graphic))
			{
				decreaseIfEquipped(ItemChargeKind.TOME_OF_FIRE, EquipmentInventorySlot.SHIELD);
			}
			else if (isWaterSpellGraphic(graphic))
			{
				decreaseIfEquipped(ItemChargeKind.TOME_OF_WATER, EquipmentInventorySlot.SHIELD);
			}
			else if (isEarthSpellGraphic(graphic))
			{
				decreaseIfEquipped(ItemChargeKind.TOME_OF_EARTH, EquipmentInventorySlot.SHIELD);
			}
			else if (graphic == BOWFA_ATTACK_GRAPHIC)
			{
				decreaseIfEquipped(ItemChargeKind.BOWFA, EquipmentInventorySlot.WEAPON);
			}
			return;
		}

		if (event.getActor() != null && event.getActor().getGraphic() == GraphicID.SPLASH)
		{
			outgoingCombatTicks = COMBAT_RECENT_TICKS;
		}
	}

	void onHitsplatApplied(HitsplatApplied event)
	{
		if (event.getHitsplat().isMine())
		{
			outgoingCombatTicks = COMBAT_RECENT_TICKS;
		}
	}

	void onGameTick(GameTick event)
	{
		if (outgoingCombatTicks > 0 && isEquipped(ItemChargeKind.SERPENTINE_HELM, EquipmentInventorySlot.HEAD))
		{
			serpCombatTicks = incrementCombatDegrade(ItemChargeKind.SERPENTINE_HELM, serpCombatTicks);
		}
		else
		{
			serpCombatTicks = 0;
		}

		if (outgoingCombatTicks > 0 && isEquipped(ItemChargeKind.TOXIC_STAFF, EquipmentInventorySlot.WEAPON))
		{
			toxicStaffCombatTicks = incrementCombatDegrade(ItemChargeKind.TOXIC_STAFF, toxicStaffCombatTicks);
		}
		else
		{
			toxicStaffCombatTicks = 0;
		}

		outgoingCombatTicks = Math.max(0, outgoingCombatTicks - 1);
	}

	void markUncharged(ItemChargeKind kind)
	{
		if (CONFIG_KEYS.containsKey(kind))
		{
			setCharges(kind, 0);
		}
	}

	void addKnownCharges(EnumMap<ItemChargeKind, Integer> charges)
	{
		for (ItemChargeKind kind : CONFIG_KEYS.keySet())
		{
			Integer quantity = getCharges(kind);
			if (quantity != null)
			{
				charges.put(kind, quantity);
			}
		}
	}

	private boolean matchSet(Pattern pattern, String message, ItemChargeKind kind)
	{
		if (kind == null)
		{
			return false;
		}

		Matcher matcher = pattern.matcher(message);
		if (!matcher.matches())
		{
			return false;
		}

		setCharges(kind, parseQuantity(matcher.group("charges")));
		return true;
	}

	private boolean matchScaleCheck(String message)
	{
		Matcher matcher = SCALE_CHECK.matcher(message);
		if (!matcher.matches())
		{
			return false;
		}

		ItemChargeKind kind = likelyScaleCheckKind(message);
		if (kind == null)
		{
			return false;
		}

		setCharges(kind, parseQuantity(matcher.group("charges")));
		clearScaleCheckContext();
		return true;
	}

	private boolean matchIncrease(Pattern pattern, String message, ItemChargeKind kind)
	{
		return matchIncrease(pattern, message, kind, 1);
	}

	private boolean matchIncrease(Pattern pattern, String message, ItemChargeKind kind, int multiplier)
	{
		Matcher matcher = pattern.matcher(message);
		if (!matcher.matches())
		{
			return false;
		}

		increase(kind, parseQuantity(matcher.group("amount")) * multiplier);
		return true;
	}

	private boolean matchAutocharge(String message)
	{
		Matcher matcher = TOME_AUTOCHARGE.matcher(message);
		if (!matcher.matches())
		{
			return false;
		}

		ItemChargeKind kind = tomeKind(matcher.group("item"));
		if (kind == null)
		{
			return false;
		}

		increase(kind, parseQuantity(matcher.group("amount")) * PAGE_CHARGES);
		return true;
	}

	private ItemChargeKind likelyNonFireTomeKind()
	{
		if (hasItem(ItemChargeKind.TOME_OF_WATER))
		{
			return ItemChargeKind.TOME_OF_WATER;
		}

		if (hasItem(ItemChargeKind.TOME_OF_EARTH))
		{
			return ItemChargeKind.TOME_OF_EARTH;
		}

		return null;
	}

	private ItemChargeKind tomeKind(String itemName)
	{
		String normalized = itemName.toLowerCase();
		if (normalized.equals("tome of fire"))
		{
			return ItemChargeKind.TOME_OF_FIRE;
		}
		if (normalized.equals("tome of water"))
		{
			return ItemChargeKind.TOME_OF_WATER;
		}
		if (normalized.equals("tome of earth"))
		{
			return ItemChargeKind.TOME_OF_EARTH;
		}
		return null;
	}

	private ItemChargeKind likelyScaleCheckKind(String message)
	{
		ItemChargeKind recentKind = recentScaleCheckKind();
		if (recentKind != null)
		{
			return recentKind;
		}

		if (SERP_CHECK.matcher(message).matches())
		{
			return ItemChargeKind.SERPENTINE_HELM;
		}

		boolean hasSerp = hasItem(ItemChargeKind.SERPENTINE_HELM);
		boolean hasToxicStaff = hasItem(ItemChargeKind.TOXIC_STAFF);
		if (hasSerp == hasToxicStaff)
		{
			return null;
		}

		return hasSerp ? ItemChargeKind.SERPENTINE_HELM : ItemChargeKind.TOXIC_STAFF;
	}

	private ItemChargeKind recentScaleCheckKind()
	{
		if (lastScaleCheckKind == null || client.getTickCount() > lastScaleCheckExpiryTick)
		{
			clearScaleCheckContext();
			return null;
		}
		return lastScaleCheckKind;
	}

	private void setScaleCheckContext(ItemChargeKind kind, int ticks)
	{
		lastScaleCheckKind = kind;
		lastScaleCheckExpiryTick = client.getTickCount() + ticks;
	}

	private void clearScaleCheckContext()
	{
		lastScaleCheckKind = null;
		lastScaleCheckExpiryTick = -1;
	}

	private void setBowfaChargeContext()
	{
		bowfaChargeShardCount = countCrystalShards(client.getItemContainer(InventoryID.INV));
		bowfaChargeExpiryTick = client.getTickCount() + BOWFA_CHARGE_CONTEXT_TICKS;
	}

	private void clearBowfaChargeContext()
	{
		bowfaChargeShardCount = -1;
		bowfaChargeExpiryTick = -1;
	}

	private boolean isBowfaCrystalShardUse(String menuTarget)
	{
		String normalized = menuTarget.toLowerCase();
		return normalized.contains("crystal shard") && normalized.contains("bow of faerdhinen");
	}

	private ItemChargeKind scaleItemKind(int itemId, String menuTarget)
	{
		ItemChargeKind kind = ItemChargeTables.getChargedKind(itemId);
		if (kind == ItemChargeKind.SERPENTINE_HELM || kind == ItemChargeKind.TOXIC_STAFF)
		{
			return kind;
		}
		return scaleItemKind(menuTarget);
	}

	private ItemChargeKind scaleUseKind(String menuTarget)
	{
		String normalized = menuTarget.toLowerCase();
		if (!normalized.contains("zulrah") || !normalized.contains("scale"))
		{
			return null;
		}
		return scaleItemKind(normalized);
	}

	private ItemChargeKind scaleItemKind(String menuTarget)
	{
		String normalized = menuTarget.toLowerCase();
		if (normalized.contains("toxic staff"))
		{
			return ItemChargeKind.TOXIC_STAFF;
		}

		if (normalized.contains("serpentine helm")
			|| normalized.contains("tanzanite helm")
			|| normalized.contains("magma helm"))
		{
			return ItemChargeKind.SERPENTINE_HELM;
		}

		return null;
	}

	private int countCrystalShards(ItemContainer container)
	{
		if (container == null)
		{
			return 0;
		}

		int count = 0;
		for (Item item : container.getItems())
		{
			if (item != null && (item.getId() == ItemID.CRYSTAL_SHARD || item.getId() == ItemID.CRYSTAL_SHARDS))
			{
				count += item.getQuantity();
			}
		}
		return count;
	}

	private void decreaseIfEquipped(ItemChargeKind kind, EquipmentInventorySlot slot)
	{
		if (isEquipped(kind, slot))
		{
			decrease(kind, 1);
		}
	}

	private int incrementCombatDegrade(ItemChargeKind kind, int combatTicks)
	{
		int nextCombatTicks = combatTicks + 1;
		if (nextCombatTicks >= SCALE_COMBAT_CHARGE_TICKS)
		{
			decrease(kind, 10);
			return 0;
		}
		return nextCombatTicks;
	}

	private void increase(ItemChargeKind kind, int amount)
	{
		Integer charges = getCharges(kind);
		setCharges(kind, Math.max(0, (charges == null ? 0 : charges) + amount));
	}

	private void decrease(ItemChargeKind kind, int amount)
	{
		Integer charges = getCharges(kind);
		if (charges == null)
		{
			return;
		}
		setCharges(kind, Math.max(0, charges - amount));
	}

	private Integer getCharges(ItemChargeKind kind)
	{
		String key = CONFIG_KEYS.get(kind);
		if (key == null)
		{
			return null;
		}
		Integer charges = configManager.getRSProfileConfiguration(CONFIG_GROUP, key, Integer.class);
		return charges == null ? null : Math.max(0, charges);
	}

	private void setCharges(ItemChargeKind kind, int charges)
	{
		String key = CONFIG_KEYS.get(kind);
		if (key != null)
		{
			int normalizedCharges = Math.max(0, charges);
			Integer currentCharges = getCharges(kind);
			if (currentCharges == null || currentCharges != normalizedCharges)
			{
				configManager.setRSProfileConfiguration(CONFIG_GROUP, key, normalizedCharges);
			}
		}
	}

	private boolean hasItem(ItemChargeKind kind)
	{
		return containsKind(client.getItemContainer(InventoryID.WORN), kind)
			|| containsKind(client.getItemContainer(InventoryID.INV), kind);
	}

	private boolean isEquipped(ItemChargeKind kind, EquipmentInventorySlot slot)
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
		if (equipment == null)
		{
			return false;
		}

		Item item = equipment.getItem(slot.getSlotIdx());
		return item != null && ItemChargeTables.getChargedKind(item.getId()) == kind;
	}

	private boolean containsKind(ItemContainer container, ItemChargeKind kind)
	{
		if (container == null)
		{
			return false;
		}

		for (Item item : container.getItems())
		{
			if (item != null && (ItemChargeTables.getChargedKind(item.getId()) == kind
				|| ItemChargeTables.getUnchargedKind(item.getId()) == kind))
			{
				return true;
			}
		}
		return false;
	}

	private boolean isTrackedMessageType(ChatMessageType type)
	{
		return type == ChatMessageType.GAMEMESSAGE
			|| type == ChatMessageType.SPAM
			|| type == ChatMessageType.DIALOG
			|| type == ChatMessageType.MESBOX;
	}

	private boolean isFireSpellGraphic(int graphic)
	{
		return graphic == 99 || graphic == 126 || graphic == 129 || graphic == 155 || graphic == 1464;
	}

	private boolean isWaterSpellGraphic(int graphic)
	{
		return graphic == 93 || graphic == 120 || graphic == 135 || graphic == 161 || graphic == 1458;
	}

	private boolean isEarthSpellGraphic(int graphic)
	{
		return graphic == 96 || graphic == 123 || graphic == 138 || graphic == 164 || graphic == 1461;
	}

	static int parseQuantity(String value)
	{
		return Integer.parseInt(value.replace(",", "").replace(".", "").trim());
	}
}
