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
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;

@Singleton
public class WarningServiceManager
{
	private final WarningEligibilityService warningEligibilityService;
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

	public void refreshOnClientThread()
	{
		services.forEach(WarningService::refreshOnClientThread);
	}

	public List<String> getOverlayTexts()
	{
		List<String> texts = new ArrayList<>();
		for (WarningService<?> service : services)
		{
			texts.addAll(service.getOverlayTexts());
		}
		return texts;
	}
}
