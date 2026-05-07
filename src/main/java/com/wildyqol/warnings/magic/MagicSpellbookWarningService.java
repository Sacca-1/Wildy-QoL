package com.wildyqol.warnings.magic;

import com.wildyqol.WildyQoLConfig;
import com.wildyqol.warnings.WarningEligibilityService;
import com.wildyqol.warnings.WarningService;
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
public class MagicSpellbookWarningService extends WarningService<MagicSpellbookWarning>
{
	private final WildyQoLConfig config;
	private final MagicSpellbookEvaluator evaluator = new MagicSpellbookEvaluator();
	private final MagicInventoryStateBuilder inventoryStateBuilder;

	@Inject
	MagicSpellbookWarningService(
		Client client,
		ClientThread clientThread,
		WarningEligibilityService warningEligibilityService,
		WildyQoLConfig config)
	{
		super(clientThread, warningEligibilityService, MagicSpellbookWarning::getText);
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
		return config.spellbookRuneWarnings();
	}

	@Override
	protected List<MagicSpellbookWarning> evaluateAll()
	{
		return evaluator.evaluateAll(MagicSpellbookEvaluator.state(inventoryStateBuilder.build()), thresholds());
	}

	private MagicThresholds thresholds()
	{
		return new MagicThresholds()
		{
			@Override
			public int teleBlock()
			{
				return config.teleBlockMinimum();
			}

			@Override
			public int entangle()
			{
				return config.entangleMinimum();
			}

			@Override
			public int surge()
			{
				return config.surgeMinimum();
			}

			@Override
			public int ice()
			{
				return config.iceSpellMinimum();
			}

			@Override
			public int blood()
			{
				return config.bloodSpellMinimum();
			}

			@Override
			public int vengeance()
			{
				return config.vengeanceMinimum();
			}
		};
	}
}
