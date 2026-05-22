package com.wildyqol.warnings.charges;

import com.google.common.collect.ImmutableSet;
import com.wildyqol.WildyQoLConfig;
import com.wildyqol.warnings.WarningEligibilityService;
import com.wildyqol.warnings.WarningService;
import java.util.EnumMap;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;

@Singleton
public class ItemChargeWarningService extends WarningService<ItemChargeWarning>
{
	private final Client client;
	private final WildyQoLConfig config;
	private final ItemChargeTracker itemChargeTracker;
	private final ItemChargeEvaluator evaluator = new ItemChargeEvaluator();

	@Inject
	ItemChargeWarningService(
		Client client,
		ClientThread clientThread,
		WarningEligibilityService warningEligibilityService,
		WildyQoLConfig config,
		ItemChargeTracker itemChargeTracker)
	{
		super(client, clientThread, warningEligibilityService, ItemChargeWarning::getText);
		this.client = client;
		this.config = config;
		this.itemChargeTracker = itemChargeTracker;
	}

	@Override
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.WORN || event.getContainerId() == InventoryID.INV)
		{
			refresh();
		}
	}

	@Override
	public void onGameTick(GameTick event)
	{
		itemChargeTracker.onGameTick(event);
		super.onGameTick(event);
	}

	@Override
	public void onChatMessage(ChatMessage event)
	{
		itemChargeTracker.onChatMessage(event);
		refreshOnClientThread();
	}

	@Override
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		itemChargeTracker.onMenuOptionClicked(event);
	}

	@Override
	public void onGraphicChanged(GraphicChanged event)
	{
		itemChargeTracker.onGraphicChanged(event);
		refreshOnClientThread();
	}

	@Override
	public void onHitsplatApplied(HitsplatApplied event)
	{
		itemChargeTracker.onHitsplatApplied(event);
	}

	@Override
	public void onVarbitChanged(VarbitChanged event)
	{
		if (isTrackedVarbit(event.getVarbitId()))
		{
			refresh();
		}
	}

	@Override
	protected boolean isEnabled()
	{
		return config.itemChargeWarnings();
	}

	@Override
	protected List<ItemChargeWarning> evaluateAll()
	{
		return evaluator.evaluateAll(buildState(), thresholds());
	}

	private ItemChargeState buildState()
	{
		ImmutableSet.Builder<ItemChargeKind> chargedItems = ImmutableSet.builder();
		ImmutableSet.Builder<ItemChargeKind> unchargedItems = ImmutableSet.builder();
		collectItems(chargedItems, unchargedItems);

		EnumMap<ItemChargeKind, Integer> charges = new EnumMap<>(ItemChargeKind.class);
		charges.put(ItemChargeKind.BOWFA, chargeQuantity(VarbitID.CHARGES_BOW_OF_FAERDHINEN_QUANTITY));
		itemChargeTracker.addKnownCharges(charges);

		return ItemChargeEvaluator.state(chargedItems.build(), unchargedItems.build(), charges);
	}

	private int chargeQuantity(int varbitId)
	{
		return Math.max(client.getVarbitValue(varbitId), client.getServerVarbitValue(varbitId));
	}

	private void collectItems(
		ImmutableSet.Builder<ItemChargeKind> chargedItems,
		ImmutableSet.Builder<ItemChargeKind> unchargedItems)
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
		if (equipment != null)
		{
			for (Item item : equipment.getItems())
			{
				addItem(chargedItems, unchargedItems, item);
			}
		}

		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory != null)
		{
			for (Item item : inventory.getItems())
			{
				addItem(chargedItems, unchargedItems, item);
			}
		}
	}

	private void addItem(
		ImmutableSet.Builder<ItemChargeKind> chargedItems,
		ImmutableSet.Builder<ItemChargeKind> unchargedItems,
		@Nullable Item item)
	{
		if (item == null || item.getId() <= 0 || ItemChargeTables.isIgnoredBowfa(item.getId()))
		{
			return;
		}

		ItemChargeKind unchargedKind = ItemChargeTables.getUnchargedKind(item.getId());
		if (unchargedKind != null)
		{
			unchargedItems.add(unchargedKind);
			itemChargeTracker.markUncharged(unchargedKind);
			return;
		}

		ItemChargeKind chargedKind = ItemChargeTables.getChargedKind(item.getId());
		if (chargedKind != null)
		{
			chargedItems.add(chargedKind);
		}
	}

	private boolean isTrackedVarbit(int varbitId)
	{
		return varbitId == VarbitID.CHARGES_BOW_OF_FAERDHINEN_QUANTITY
			|| varbitId == VarbitID.INSIDE_WILDERNESS
			|| varbitId == VarbitID.PVP_AREA_CLIENT;
	}

	private ItemChargeThresholds thresholds()
	{
		return new ItemChargeThresholds()
		{
			@Override
			public int bowfaCharges()
			{
				return config.bowfaChargeMinimum();
			}

			@Override
			public int tomeCharges()
			{
				return config.tomeChargeMinimum();
			}

			@Override
			public int toxicStaffCharges()
			{
				return config.toxicStaffChargeMinimum();
			}

			@Override
			public int serpentineHelmCharges()
			{
				return config.serpentineHelmChargeMinimum();
			}
		};
	}
}
