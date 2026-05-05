package com.wildyqol.warnings.ammo;

import com.google.common.collect.ImmutableSet;
import com.wildyqol.WildyQoLConfig;
import com.wildyqol.warnings.WarningService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemVariationMapping;

@Singleton
public class RangedAmmoWarningService extends WarningService<RangedAmmoWarning>
{
	private final Client client;
	private final WildyQoLConfig config;
	private final RangedAmmoEvaluator evaluator = new RangedAmmoEvaluator();

	@Inject
	RangedAmmoWarningService(
		Client client,
		ClientThread clientThread,
		WildyQoLConfig config)
	{
		super(client, clientThread, RangedAmmoWarning::getText);
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
		if (event.getVarpId() == VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO
			|| event.getVarpId() == VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO_AMOUNT
			|| event.getVarbitId() == VarbitID.INSIDE_WILDERNESS
			|| event.getVarbitId() == VarbitID.PVP_AREA_CLIENT)
		{
			refresh();
		}
	}

	@Override
	protected boolean isEnabled()
	{
		return config.rangedAmmoWarnings();
	}

	@Override
	protected List<RangedAmmoWarning> evaluateAll()
	{
		return evaluator.evaluateAll(buildState(), thresholds());
	}

	private RangedAmmoState buildState()
	{
		Set<RangedAmmoRequirement> requirements = collectRequirements();
		Map<Integer, Integer> ammoCounts = collectAmmoCounts();
		return RangedAmmoEvaluator.state(
			requirements,
			ammoCounts);
	}

	private Set<RangedAmmoRequirement> collectRequirements()
	{
		ImmutableSet.Builder<RangedAmmoRequirement> requirements = ImmutableSet.builder();
		ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
		if (equipment != null)
		{
			Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
			addRequirement(requirements, weapon);
		}

		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory != null)
		{
			for (Item item : inventory.getItems())
			{
				addRequirement(requirements, item);
			}
		}

		return requirements.build();
	}

	private void addRequirement(ImmutableSet.Builder<RangedAmmoRequirement> requirements, @Nullable Item item)
	{
		if (item == null || item.getId() <= 0)
		{
			return;
		}

		RangedAmmoRequirement requirement = RangedAmmoTables.getRequirement(item.getId());
		if (requirement != null)
		{
			requirements.add(requirement);
		}
	}

	private Map<Integer, Integer> collectAmmoCounts()
	{
		Map<Integer, Integer> ammoCounts = new HashMap<>();
		ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
		if (equipment != null)
		{
			addAmmo(ammoCounts, equipment.getItem(EquipmentInventorySlot.AMMO.getSlotIdx()));
		}

		addAmmo(ammoCounts,
			client.getVarpValue(VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO),
			client.getVarpValue(VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO_AMOUNT));

		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory != null)
		{
			for (Item item : inventory.getItems())
			{
				addAmmo(ammoCounts, item);
			}
		}

		return ammoCounts;
	}

	private void addAmmo(Map<Integer, Integer> ammoCounts, @Nullable Item item)
	{
		if (item != null)
		{
			addAmmo(ammoCounts, item.getId(), item.getQuantity());
		}
	}

	private void addAmmo(Map<Integer, Integer> ammoCounts, int itemId, int quantity)
	{
		if (itemId <= 0 || quantity <= 0 || !RangedAmmoTables.isSupportedAmmo(itemId))
		{
			return;
		}

		ammoCounts.merge(ItemVariationMapping.map(itemId), quantity, Integer::sum);
	}

	private AmmoThresholds thresholds()
	{
		return new AmmoThresholds()
		{
			@Override
			public int atlatlDarts()
			{
				return config.atlatlDartMinimum();
			}

			@Override
			public int bolts()
			{
				return config.boltMinimum();
			}

			@Override
			public int javelins()
			{
				return config.javelinMinimum();
			}

			@Override
			public int arrows()
			{
				return config.arrowMinimum();
			}
		};
	}
}
