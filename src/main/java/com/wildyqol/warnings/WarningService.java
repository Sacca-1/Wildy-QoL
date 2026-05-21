package com.wildyqol.warnings;

import com.wildyqol.WildyQoLConfig.WarningDisplayMode;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;

public abstract class WarningService<T>
{
	private final Client client;
	private final ClientThread clientThread;
	private final WarningEligibilityService warningEligibilityService;
	private final Function<T, String> textProvider;
	private final WarningVisibility<T> visibility;

	private List<T> visibleWarnings = Collections.emptyList();

	protected WarningService(
		Client client,
		ClientThread clientThread,
		WarningEligibilityService warningEligibilityService,
		Function<T, String> textProvider)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.warningEligibilityService = warningEligibilityService;
		this.textProvider = textProvider;
		this.visibility = new WarningVisibility<>(textProvider);
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
	}

	public void onVarbitChanged(VarbitChanged event)
	{
	}

	public void onGameTick(GameTick event)
	{
		update(true);
	}

	public List<String> getOverlayTexts()
	{
		if (!isEnabled() || visibleWarnings.isEmpty())
		{
			return Collections.emptyList();
		}

		return visibleWarnings.stream()
			.map(textProvider)
			.collect(Collectors.toList());
	}

	public void refresh()
	{
		update(false);
	}

	public void refreshOnClientThread()
	{
		clientThread.invokeLater(this::refresh);
	}

	protected abstract boolean isEnabled();

	protected abstract List<T> evaluateAll();

	private void update(boolean gameTick)
	{
		boolean enabled = isEnabled();
		if (!enabled || client.getGameState() != GameState.LOGGED_IN)
		{
			visibleWarnings = visibility.update(
				Collections.emptyList(),
				enabled,
				WarningDisplayMode.ALWAYS,
				false,
				false,
				gameTick);
			return;
		}

		WarningEligibility eligibility = warningEligibilityService.getEligibility();
		if (!eligibility.isEquipmentWarningsVisible())
		{
			visibleWarnings = visibility.update(
				Collections.emptyList(),
				enabled,
				WarningDisplayMode.ALWAYS,
				false,
				false,
				gameTick);
			return;
		}

		List<T> warnings = enabled ? evaluateAll() : Collections.emptyList();
		if (warnings.isEmpty())
		{
			visibleWarnings = visibility.update(warnings, enabled, WarningDisplayMode.ALWAYS, false, false, gameTick);
			return;
		}

		visibleWarnings = visibility.update(
			warnings,
			enabled,
			eligibility.getWarningDisplayMode(),
			eligibility.isInPvp(),
			eligibility.isEligibleOutsidePvp(),
			gameTick);
	}
}
