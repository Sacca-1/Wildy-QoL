package com.wildyqol.warnings;

import com.wildyqol.WildyQoLConfig;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;

@Singleton
public class WarningEligibilityService
{
	public static final int RECENT_PVP_TICKS = 100;

	private static final int NO_PVP_TICK = Integer.MIN_VALUE;

	private final Client client;
	private final WildyQoLConfig config;
	private final BankProximityService bankProximityService;

	private int currentTick;
	private int lastPvpTick = NO_PVP_TICK;
	private int bankVisibleTick = NO_PVP_TICK;
	private boolean bankVisible;

	@Inject
	WarningEligibilityService(
		Client client,
		WildyQoLConfig config,
		BankProximityService bankProximityService)
	{
		this.client = client;
		this.config = config;
		this.bankProximityService = bankProximityService;
	}

	public void onGameTick(GameTick event)
	{
		currentTick++;
		if (PvpArea.isPvpArea(client))
		{
			lastPvpTick = currentTick;
		}
	}

	public WarningEligibility getEligibility()
	{
		boolean onlyWarnAtBank = config.onlyWarnAtBank();
		boolean inPvp = PvpArea.isPvpArea(client);

		if (inPvp)
		{
			lastPvpTick = currentTick;
		}

		if (!onlyWarnAtBank)
		{
			return new WarningEligibility(false, inPvp, true);
		}

		boolean recentlyLeftPvp = isRecentlyLeftPvp(inPvp, currentTick, lastPvpTick);
		boolean eligibleOutsidePvp = recentlyLeftPvp || isBankVisibleThisTick();
		return new WarningEligibility(true, inPvp, eligibleOutsidePvp);
	}

	private boolean isBankVisibleThisTick()
	{
		if (bankVisibleTick != currentTick)
		{
			bankVisible = bankProximityService.isBankVisible();
			bankVisibleTick = currentTick;
		}

		return bankVisible;
	}

	static boolean isRecentlyLeftPvp(boolean inPvp, int currentTick, int lastPvpTick)
	{
		return !inPvp
			&& lastPvpTick != NO_PVP_TICK
			&& currentTick - lastPvpTick <= RECENT_PVP_TICKS;
	}

	public void reset()
	{
		currentTick = 0;
		lastPvpTick = NO_PVP_TICK;
		bankVisibleTick = NO_PVP_TICK;
		bankVisible = false;
	}
}
