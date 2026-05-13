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
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;

@Singleton
public class ItemChargeWarningService extends WarningService<ItemChargeWarning>
{
	private final Client client;
	private final WildyQoLConfig config;
	private final ItemChargeEvaluator evaluator = new ItemChargeEvaluator();

	@Inject
	ItemChargeWarningService(
		Client client,
		ClientThread clientThread,
		WarningEligibilityService warningEligibilityService,
		WildyQoLConfig config)
	{
		super(client, clientThread, warningEligibilityService, ItemChargeWarning::getText);
		this.client = client;
		this.config = config;
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
		return config.enablePreviewWarnings() && config.itemChargeWarnings();
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
		charges.put(ItemChargeKind.TOME_OF_FIRE, chargeQuantity(VarbitID.CHARGES_TOME_OF_FIRE_QUANTITY));
		charges.put(ItemChargeKind.TOME_OF_WATER, chargeQuantity(VarbitID.CHARGES_TOME_OF_WATER_QUANTITY));
		charges.put(ItemChargeKind.TOME_OF_EARTH, chargeQuantity(VarbitID.CHARGES_TOME_OF_EARTH_QUANTITY));

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
			|| varbitId == VarbitID.CHARGES_TOME_OF_FIRE_QUANTITY
			|| varbitId == VarbitID.CHARGES_TOME_OF_WATER_QUANTITY
			|| varbitId == VarbitID.CHARGES_TOME_OF_EARTH_QUANTITY
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
		};
	}
}
