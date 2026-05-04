package com.wildyqol.warnings.ammo;

import com.google.common.collect.ImmutableSet;
import com.wildyqol.WildyQoLConfig;
import com.wildyqol.warnings.PvpArea;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemVariationMapping;

@Singleton
public class RangedAmmoWarningService
{
	private final Client client;
	private final ClientThread clientThread;
	private final WildyQoLConfig config;
	private final RangedAmmoEvaluator evaluator = new RangedAmmoEvaluator();
	private final RangedAmmoWarningVisibility visibility = new RangedAmmoWarningVisibility();

	private List<RangedAmmoWarning> visibleWarnings = Collections.emptyList();

	@Inject
	RangedAmmoWarningService(
		Client client,
		ClientThread clientThread,
		WildyQoLConfig config)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.config = config;
	}

	public void startUp()
	{
		refreshOnClientThread();
	}

	public void shutDown()
	{
		visibleWarnings = Collections.emptyList();
		visibility.reset();
	}

	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.WORN || event.getContainerId() == InventoryID.INV)
		{
			refresh();
		}
	}

	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarpId() == VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO
			|| event.getVarpId() == VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO_AMOUNT
			|| event.getVarbitId() == VarbitID.CHARGES_BOW_OF_FAERDHINEN_QUANTITY
			|| event.getVarbitId() == VarbitID.INSIDE_WILDERNESS
			|| event.getVarbitId() == VarbitID.PVP_AREA_CLIENT)
		{
			refresh();
		}
	}

	public void onGameTick(GameTick event)
	{
		visibleWarnings = visibility.update(evaluateAll(), config.rangedAmmoWarnings(), PvpArea.isPvpArea(client), true);
	}

	List<String> getOverlayTexts()
	{
		if (!config.rangedAmmoWarnings() || visibleWarnings.isEmpty())
		{
			return Collections.emptyList();
		}

		return visibleWarnings.stream()
			.map(RangedAmmoWarning::getText)
			.collect(Collectors.toList());
	}

	public void refresh()
	{
		visibleWarnings = visibility.update(evaluateAll(), config.rangedAmmoWarnings(), PvpArea.isPvpArea(client), false);
	}

	public void refreshOnClientThread()
	{
		clientThread.invokeLater(this::refresh);
	}

	private List<RangedAmmoWarning> evaluateAll()
	{
		return evaluator.evaluateAll(buildState(), thresholds());
	}

	private RangedAmmoState buildState()
	{
		Set<RangedAmmoRequirement> requirements = collectRequirements();
		Map<Integer, Integer> ammoCounts = collectAmmoCounts();
		return RangedAmmoEvaluator.state(
			requirements,
			ammoCounts,
			hasChargedBowfa(),
			hasInactiveBowfa(),
			client.getVarbitValue(VarbitID.CHARGES_BOW_OF_FAERDHINEN_QUANTITY));
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

	private boolean hasChargedBowfa()
	{
		return hasMatchingItem(RangedAmmoTables::isBowfaWithCharges);
	}

	private boolean hasInactiveBowfa()
	{
		return hasMatchingItem(RangedAmmoTables::isInactiveBowfa);
	}

	private boolean hasMatchingItem(ItemMatcher matcher)
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
		if (equipment != null)
		{
			Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
			if (weapon != null && matcher.matches(weapon.getId()))
			{
				return true;
			}
		}

		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory != null)
		{
			for (Item item : inventory.getItems())
			{
				if (item != null && matcher.matches(item.getId()))
				{
					return true;
				}
			}
		}

		return false;
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

			@Override
			public int bowfaCharges()
			{
				return config.bowfaChargeMinimum();
			}
		};
	}

	private interface ItemMatcher
	{
		boolean matches(int itemId);
	}
}
