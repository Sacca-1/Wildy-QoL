package com.wildyqol.warnings;

import com.google.common.collect.ImmutableList;
import com.wildyqol.warnings.ammo.RangedAmmoWarningService;
import com.wildyqol.warnings.charges.ItemChargeWarningService;
import com.wildyqol.warnings.magic.MagicSpellbookWarningService;
import com.wildyqol.warnings.teleport.TeleportOutWarningService;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;

@Singleton
public class WarningServiceManager
{
	private final WarningEligibilityService warningEligibilityService;
	private final ItemChargeWarningService itemChargeWarningService;
	private final List<WarningService<?>> services;

	@Inject
	WarningServiceManager(
		WarningEligibilityService warningEligibilityService,
		RangedAmmoWarningService rangedAmmoWarningService,
		ItemChargeWarningService itemChargeWarningService,
		MagicSpellbookWarningService magicSpellbookWarningService,
		TeleportOutWarningService teleportOutWarningService)
	{
		this.warningEligibilityService = warningEligibilityService;
		this.itemChargeWarningService = itemChargeWarningService;
		services = ImmutableList.of(
			rangedAmmoWarningService,
			itemChargeWarningService,
			magicSpellbookWarningService,
			teleportOutWarningService);
	}

	public void startUp()
	{
		warningEligibilityService.reset();
		services.forEach(WarningService::startUp);
	}

	public void shutDown()
	{
		warningEligibilityService.reset();
		services.forEach(WarningService::shutDown);
	}

	public void onItemContainerChanged(ItemContainerChanged event)
	{
		services.forEach(service -> service.onItemContainerChanged(event));
	}

	public void onVarbitChanged(VarbitChanged event)
	{
		services.forEach(service -> service.onVarbitChanged(event));
	}

	public void onGameTick(GameTick event)
	{
		warningEligibilityService.onGameTick(event);
		services.forEach(service -> service.onGameTick(event));
	}

	public void onChatMessage(ChatMessage event)
	{
		services.forEach(service -> service.onChatMessage(event));
		if (warningEligibilityService.onChatMessage(event))
		{
			services.forEach(WarningService::refreshOnClientThread);
		}
	}

	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		services.forEach(service -> service.onMenuOptionClicked(event));
	}

	public void onGraphicChanged(GraphicChanged event)
	{
		services.forEach(service -> service.onGraphicChanged(event));
	}

	public void onHitsplatApplied(HitsplatApplied event)
	{
		services.forEach(service -> service.onHitsplatApplied(event));
	}

	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (warningEligibilityService.onWidgetLoaded(event))
		{
			services.forEach(WarningService::refreshOnClientThread);
		}
	}

	public void refreshOnClientThread()
	{
		services.forEach(WarningService::refreshOnClientThread);
	}

	public void onRuneScapeProfileChanged()
	{
		itemChargeWarningService.onRuneScapeProfileChanged();
	}

	public List<String> getOverlayTexts()
	{
		List<String> texts = new ArrayList<>();
		for (WarningService<?> service : services)
		{
			for (String text : service.getOverlayTexts())
			{
				if (!texts.contains(text))
				{
					texts.add(text);
				}
			}
		}
		return texts;
	}
}
