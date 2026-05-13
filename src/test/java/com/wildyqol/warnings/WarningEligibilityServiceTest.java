package com.wildyqol.warnings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.wildyqol.WildyQoLConfig;
import java.lang.reflect.Proxy;
import java.util.EnumSet;
import net.runelite.api.Client;
import net.runelite.api.ChatMessageType;
import net.runelite.api.WorldType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import org.junit.Test;

public class WarningEligibilityServiceTest
{
	@Test
	public void recentlyLeftPvpLastsOneHundredTicks()
	{
		assertTrue(WarningEligibilityService.isRecentlyLeftPvp(false, 100, 0));
		assertFalse(WarningEligibilityService.isRecentlyLeftPvp(false, 101, 0));
	}

	@Test
	public void currentPvpAndNoPriorPvpDoNotCountAsRecentlyLeft()
	{
		assertFalse(WarningEligibilityService.isRecentlyLeftPvp(true, 100, 100));
		assertFalse(WarningEligibilityService.isRecentlyLeftPvp(false, 100, Integer.MIN_VALUE));
	}

	@Test
	public void detectsDeathGameMessage()
	{
		assertTrue(WarningEligibilityService.isDeathMessage(
			ChatMessageType.GAMEMESSAGE,
			"<col=ff0000>Oh dear, you are dead!</col>"));
		assertFalse(WarningEligibilityService.isDeathMessage(ChatMessageType.SPAM, "Oh dear, you are dead!"));
		assertFalse(WarningEligibilityService.isDeathMessage(ChatMessageType.GAMEMESSAGE, "Welcome to Old School RuneScape."));
		assertFalse(WarningEligibilityService.isDeathMessage(ChatMessageType.GAMEMESSAGE, null));
	}

	@Test
	public void deathSuppressionClearsWhenBankOpens()
	{
		WarningEligibilityService service = new WarningEligibilityService(null, null, null);

		assertTrue(service.onChatMessage(new ChatMessage(
			null,
			ChatMessageType.GAMEMESSAGE,
			"",
			"Oh dear, you are dead!",
			"",
			0)));
		assertTrue(service.isWarningsSuppressedAfterDeath());

		WidgetLoaded nonBank = new WidgetLoaded();
		nonBank.setGroupId(InterfaceID.DEATHKEEP);
		assertFalse(service.onWidgetLoaded(nonBank));
		assertTrue(service.isWarningsSuppressedAfterDeath());

		WidgetLoaded bank = new WidgetLoaded();
		bank.setGroupId(InterfaceID.BANKMAIN);
		assertTrue(service.onWidgetLoaded(bank));
		assertFalse(service.isWarningsSuppressedAfterDeath());
	}

	@Test
	public void bankWidgetGroupsAreRecognized()
	{
		assertTrue(WarningEligibilityService.isBankWidgetGroup(InterfaceID.BANKMAIN));
		assertFalse(WarningEligibilityService.isBankWidgetGroup(InterfaceID.SHARED_BANK));
		assertFalse(WarningEligibilityService.isBankWidgetGroup(InterfaceID.BANKSIDE));
	}

	@Test
	public void deathSuppressionOnlyAppliesToBankGatedWarnings()
	{
		WarningEligibilityService bankGatedService = new WarningEligibilityService(clientOutsidePvp(), config(true), null);
		bankGatedService.onChatMessage(deathMessage());

		WarningEligibility bankGatedEligibility = bankGatedService.getEligibility();
		assertTrue(bankGatedEligibility.isOnlyWarnAtBank());
		assertFalse(bankGatedEligibility.isInPvp());
		assertFalse(bankGatedEligibility.isEligibleOutsidePvp());

		WarningEligibilityService alwaysShowService = new WarningEligibilityService(clientOutsidePvp(), config(false), null);
		alwaysShowService.onChatMessage(deathMessage());

		WarningEligibility alwaysShowEligibility = alwaysShowService.getEligibility();
		assertFalse(alwaysShowEligibility.isOnlyWarnAtBank());
		assertFalse(alwaysShowEligibility.isInPvp());
		assertTrue(alwaysShowEligibility.isEligibleOutsidePvp());
	}

	private static ChatMessage deathMessage()
	{
		return new ChatMessage(
			null,
			ChatMessageType.GAMEMESSAGE,
			"",
			"Oh dear, you are dead!",
			"",
			0);
	}

	private static WildyQoLConfig config(boolean onlyWarnAtBank)
	{
		return new WildyQoLConfig()
		{
			@Override
			public boolean onlyWarnAtBank()
			{
				return onlyWarnAtBank;
			}
		};
	}

	private static Client clientOutsidePvp()
	{
		return (Client) Proxy.newProxyInstance(
			Client.class.getClassLoader(),
			new Class<?>[] {Client.class},
			(proxy, method, args) ->
			{
				if ("getWorldType".equals(method.getName()))
				{
					return EnumSet.noneOf(WorldType.class);
				}

				if ("getVarbitValue".equals(method.getName()))
				{
					return 0;
				}

				if ("toString".equals(method.getName()))
				{
					return "clientOutsidePvp";
				}

				return null;
			});
	}
}
