package com.wildyqol.warnings.teleport;

import com.wildyqol.WildyQoLConfig;
import com.wildyqol.WildyQoLConfig.TeleportOutWarningMode;
import com.wildyqol.warnings.WarningService;
import com.wildyqol.warnings.magic.MagicInventoryStateBuilder;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;

@Singleton
public class TeleportOutWarningService extends WarningService<TeleportOutWarning>
{
	private final WildyQoLConfig config;
	private final TeleportOutEvaluator evaluator = new TeleportOutEvaluator();
	private final MagicInventoryStateBuilder inventoryStateBuilder;

	@Inject
	TeleportOutWarningService(
		Client client,
		ClientThread clientThread,
		WildyQoLConfig config)
	{
		super(client, clientThread, TeleportOutWarning::getText);
		this.config = config;
		this.inventoryStateBuilder = new MagicInventoryStateBuilder(client);
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
		if (MagicInventoryStateBuilder.isTrackedVarbit(event.getVarbitId())
			|| event.getVarbitId() == VarbitID.INSIDE_WILDERNESS
			|| event.getVarbitId() == VarbitID.PVP_AREA_CLIENT)
		{
			refresh();
		}
	}

	@Override
	protected boolean isEnabled()
	{
		return config.teleportOutWarningMode() != TeleportOutWarningMode.NEVER;
	}

	@Override
	protected List<TeleportOutWarning> evaluateAll()
	{
		return evaluator.evaluateAll(TeleportOutEvaluator.state(inventoryStateBuilder.build()), config.teleportOutWarningMode());
	}
}
