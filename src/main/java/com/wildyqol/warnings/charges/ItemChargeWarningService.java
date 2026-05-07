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
		super(clientThread, warningEligibilityService, ItemChargeWarning::getText);
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
		charges.put(ItemChargeKind.BOWFA, client.getVarbitValue(VarbitID.CHARGES_BOW_OF_FAERDHINEN_QUANTITY));
		charges.put(ItemChargeKind.SERPENTINE_HELM, client.getVarbitValue(VarbitID.CHARGES_SERPENTINE_HELM_QUANTITY));
		charges.put(ItemChargeKind.TOXIC_STAFF, client.getVarbitValue(VarbitID.CHARGES_TOXIC_STAFF_OF_THE_DEAD_QUANTITY));
		charges.put(ItemChargeKind.ACCURSED_THAMMARONS, client.getVarbitValue(VarbitID.CHARGES_WILDERNESS_WEAPON_QUANTITY));
		charges.put(ItemChargeKind.CRAWS_WEBWEAVER, client.getVarbitValue(VarbitID.CHARGES_WILDERNESS_WEAPON_QUANTITY));
		charges.put(ItemChargeKind.URSINE_VIGGORAS, client.getVarbitValue(VarbitID.CHARGES_WILDERNESS_WEAPON_QUANTITY));
		charges.put(ItemChargeKind.TOME_OF_FIRE, client.getVarbitValue(VarbitID.CHARGES_TOME_OF_FIRE_QUANTITY));
		charges.put(ItemChargeKind.TOME_OF_WATER, client.getVarbitValue(VarbitID.CHARGES_TOME_OF_WATER_QUANTITY));
		charges.put(ItemChargeKind.TOME_OF_EARTH, client.getVarbitValue(VarbitID.CHARGES_TOME_OF_EARTH_QUANTITY));

		return ItemChargeEvaluator.state(chargedItems.build(), unchargedItems.build(), charges);
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
			|| varbitId == VarbitID.CHARGES_SERPENTINE_HELM_QUANTITY
			|| varbitId == VarbitID.CHARGES_TOXIC_STAFF_OF_THE_DEAD_QUANTITY
			|| varbitId == VarbitID.CHARGES_WILDERNESS_WEAPON_QUANTITY
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
			public int serpentineHelmCharges()
			{
				return config.serpentineHelmChargeMinimum();
			}

			@Override
			public int toxicStaffCharges()
			{
				return config.toxicStaffChargeMinimum();
			}

			@Override
			public int accursedThammaronsCharges()
			{
				return config.accursedThammaronsChargeMinimum();
			}

			@Override
			public int crawsWebweaverCharges()
			{
				return config.crawsWebweaverChargeMinimum();
			}

			@Override
			public int ursineViggorasCharges()
			{
				return config.ursineViggorasChargeMinimum();
			}

			@Override
			public int tomeCharges()
			{
				return config.tomeChargeMinimum();
			}
		};
	}
}
