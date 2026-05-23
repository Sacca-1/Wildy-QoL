package com.wildyqol.warnings;

import com.wildyqol.AreaDetection;
import com.wildyqol.WildyQoLConfig;
import com.wildyqol.WildyQoLConfig.WarningDisplayMode;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.vars.AccountType;
import net.runelite.client.util.Text;

@Singleton
public class WarningEligibilityService
{
	public static final int RECENT_PVP_TICKS = 100;

	private static final int NO_PVP_TICK = Integer.MIN_VALUE;
	private static final String DEATH_MESSAGE = "Oh dear, you are dead!";

	private final Client client;
	private final WildyQoLConfig config;
	private final BankProximityService bankProximityService;

	private int currentTick;
	private int lastPvpTick = NO_PVP_TICK;
	private int bankVisibleTick = NO_PVP_TICK;
	private int pvpRelevantBankVisibleTick = NO_PVP_TICK;
	private boolean bankVisible;
	private boolean pvpRelevantBankVisible;
	private boolean warningsSuppressedAfterDeath;

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

	public boolean onChatMessage(ChatMessage event)
	{
		if (isDeathMessage(event.getType(), event.getMessage()))
		{
			warningsSuppressedAfterDeath = true;
			return true;
		}
		return false;
	}

	public boolean onWidgetLoaded(WidgetLoaded event)
	{
		if (isBankWidgetGroup(event.getGroupId()) && warningsSuppressedAfterDeath)
		{
			warningsSuppressedAfterDeath = false;
			return true;
		}
		return false;
	}

	public WarningEligibility getEligibility()
	{
		WarningDisplayMode warningDisplayMode = config.warningDisplayMode();
		boolean inPvp = PvpArea.isPvpArea(client);
		boolean equipmentWarningsVisible = isAccountEligibleForEquipmentWarnings(client, config)
			&& !AreaDetection.isPvpArenaWorld(client)
			&& (!config.onlyShowEquipmentWarningsWhenSkulled() || isSkulled(client));

		if (inPvp)
		{
			lastPvpTick = currentTick;
		}

		if (warningDisplayMode != WarningDisplayMode.BANK && warningDisplayMode != WarningDisplayMode.PVP_BANKS)
		{
			return new WarningEligibility(warningDisplayMode, inPvp, true, equipmentWarningsVisible);
		}

		if (warningsSuppressedAfterDeath)
		{
			return new WarningEligibility(warningDisplayMode, false, false, equipmentWarningsVisible);
		}

		boolean eligibleOutsidePvp = isEligibleOutsidePvp(warningDisplayMode, inPvp);
		return new WarningEligibility(warningDisplayMode, inPvp, eligibleOutsidePvp, equipmentWarningsVisible);
	}

	private boolean isEligibleOutsidePvp(WarningDisplayMode warningDisplayMode, boolean inPvp)
	{
		boolean recentlyLeftPvp = isRecentlyLeftPvp(inPvp, currentTick, lastPvpTick);
		if (warningDisplayMode == WarningDisplayMode.PVP_BANKS)
		{
			return recentlyLeftPvp || isPvpRelevantBankVisibleThisTick();
		}

		return recentlyLeftPvp || isBankVisibleThisTick();
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

	private boolean isPvpRelevantBankVisibleThisTick()
	{
		if (pvpRelevantBankVisibleTick != currentTick)
		{
			pvpRelevantBankVisible = bankProximityService.isPvpRelevantBankVisible();
			pvpRelevantBankVisibleTick = currentTick;
		}

		return pvpRelevantBankVisible;
	}

	static boolean isRecentlyLeftPvp(boolean inPvp, int currentTick, int lastPvpTick)
	{
		return !inPvp
			&& lastPvpTick != NO_PVP_TICK
			&& currentTick - lastPvpTick <= RECENT_PVP_TICKS;
	}

	static boolean isDeathMessage(ChatMessageType type, String message)
	{
		return type == ChatMessageType.GAMEMESSAGE
			&& message != null
			&& DEATH_MESSAGE.equals(Text.removeTags(message));
	}

	static boolean isBankWidgetGroup(int groupId)
	{
		return groupId == InterfaceID.BANKMAIN;
	}

	static boolean isSkulled(Client client)
	{
		Player localPlayer = client.getLocalPlayer();
		return localPlayer != null && localPlayer.getSkullIcon() != SkullIcon.NONE;
	}

	static boolean isAccountEligibleForEquipmentWarnings(Client client, WildyQoLConfig config)
	{
		if (config.showEquipmentWarningsOnRestrictedAccounts())
		{
			return true;
		}

		return client.getAccountType() == AccountType.NORMAL;
	}

	boolean isWarningsSuppressedAfterDeath()
	{
		return warningsSuppressedAfterDeath;
	}

	public void reset()
	{
		currentTick = 0;
		lastPvpTick = NO_PVP_TICK;
		bankVisibleTick = NO_PVP_TICK;
		pvpRelevantBankVisibleTick = NO_PVP_TICK;
		bankVisible = false;
		pvpRelevantBankVisible = false;
		warningsSuppressedAfterDeath = false;
	}
}
