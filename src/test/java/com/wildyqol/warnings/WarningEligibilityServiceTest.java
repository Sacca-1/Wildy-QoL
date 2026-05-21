package com.wildyqol.warnings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.wildyqol.WildyQoLConfig;
import com.wildyqol.WildyQoLConfig.WarningDisplayMode;
import java.lang.reflect.Proxy;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import net.runelite.api.Client;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;
import net.runelite.api.WorldType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.vars.AccountType;
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
		WarningEligibilityService bankGatedService = new WarningEligibilityService(
			clientOutsidePvp(),
			config(WarningDisplayMode.BANK),
			null);
		bankGatedService.onChatMessage(deathMessage());

		WarningEligibility bankGatedEligibility = bankGatedService.getEligibility();
		assertSame(WarningDisplayMode.BANK, bankGatedEligibility.getWarningDisplayMode());
		assertFalse(bankGatedEligibility.isInPvp());
		assertFalse(bankGatedEligibility.isEligibleOutsidePvp());
		assertTrue(bankGatedEligibility.isEquipmentWarningsVisible());

		WarningEligibilityService alwaysShowService = new WarningEligibilityService(
			clientOutsidePvp(),
			config(WarningDisplayMode.ALWAYS),
			null);
		alwaysShowService.onChatMessage(deathMessage());

		WarningEligibility alwaysShowEligibility = alwaysShowService.getEligibility();
		assertSame(WarningDisplayMode.ALWAYS, alwaysShowEligibility.getWarningDisplayMode());
		assertFalse(alwaysShowEligibility.isInPvp());
		assertTrue(alwaysShowEligibility.isEligibleOutsidePvp());
		assertTrue(alwaysShowEligibility.isEquipmentWarningsVisible());
	}

	@Test
	public void pvpAreaModeKeepsPvpStateWithoutBankGate()
	{
		WarningEligibilityService service = new WarningEligibilityService(
			clientInPvp(),
			config(WarningDisplayMode.PVP_AREA),
			null);

		WarningEligibility eligibility = service.getEligibility();

		assertSame(WarningDisplayMode.PVP_AREA, eligibility.getWarningDisplayMode());
		assertTrue(eligibility.isInPvp());
		assertTrue(eligibility.isEligibleOutsidePvp());
		assertTrue(eligibility.isEquipmentWarningsVisible());
	}

	@Test
	public void pvpBanksModeOnlyUsesPvpRelevantBanks()
	{
		WarningEligibilityService pvpBankService = new WarningEligibilityService(
			clientOutsidePvp(),
			config(WarningDisplayMode.PVP_BANKS),
			bankProximityService(true, true));

		WarningEligibility pvpBankEligibility = pvpBankService.getEligibility();
		assertSame(WarningDisplayMode.PVP_BANKS, pvpBankEligibility.getWarningDisplayMode());
		assertTrue(pvpBankEligibility.isEligibleOutsidePvp());

		WarningEligibilityService otherBankService = new WarningEligibilityService(
			clientOutsidePvp(),
			config(WarningDisplayMode.PVP_BANKS),
			bankProximityService(true, false));

		WarningEligibility otherBankEligibility = otherBankService.getEligibility();
		assertSame(WarningDisplayMode.PVP_BANKS, otherBankEligibility.getWarningDisplayMode());
		assertFalse(otherBankEligibility.isEligibleOutsidePvp());
	}

	@Test
	public void pvpBanksModeKeepsRecentPvpFallback()
	{
		AtomicBoolean inPvp = new AtomicBoolean(true);
		WarningEligibilityService service = new WarningEligibilityService(
			clientWithPvpState(inPvp),
			config(WarningDisplayMode.PVP_BANKS),
			bankProximityService(false, false));

		service.onGameTick(new GameTick());
		inPvp.set(false);

		service.onGameTick(new GameTick());
		WarningEligibility eligibility = service.getEligibility();
		assertSame(WarningDisplayMode.PVP_BANKS, eligibility.getWarningDisplayMode());
		assertFalse(eligibility.isInPvp());
		assertTrue(eligibility.isEligibleOutsidePvp());
	}

	@Test
	public void equipmentWarningsCanRequireSkull()
	{
		WarningEligibilityService unskulledService = new WarningEligibilityService(
			clientOutsidePvp(SkullIcon.NONE),
			config(WarningDisplayMode.ALWAYS, true),
			null);
		assertFalse(unskulledService.getEligibility().isEquipmentWarningsVisible());

		WarningEligibilityService skulledService = new WarningEligibilityService(
			clientOutsidePvp(SkullIcon.SKULL),
			config(WarningDisplayMode.ALWAYS, true),
			null);
		assertTrue(skulledService.getEligibility().isEquipmentWarningsVisible());

		WarningEligibilityService disabledGateService = new WarningEligibilityService(
			clientOutsidePvp(SkullIcon.NONE),
			config(WarningDisplayMode.ALWAYS, false),
			null);
		assertTrue(disabledGateService.getEligibility().isEquipmentWarningsVisible());
	}

	@Test
	public void equipmentWarningsShowOnAllAccountTypesByDefault()
	{
		WarningEligibilityService normalService = new WarningEligibilityService(
			clientOutsidePvp(AccountType.NORMAL, EnumSet.noneOf(WorldType.class), SkullIcon.NONE),
			config(WarningDisplayMode.ALWAYS, false),
			null);
		assertTrue(normalService.getEligibility().isEquipmentWarningsVisible());

		WarningEligibilityService ironmanService = new WarningEligibilityService(
			clientOutsidePvp(AccountType.IRONMAN, EnumSet.noneOf(WorldType.class), SkullIcon.NONE),
			config(WarningDisplayMode.ALWAYS, false),
			null);
		assertTrue(ironmanService.getEligibility().isEquipmentWarningsVisible());
	}

	@Test
	public void equipmentWarningsCanBeLimitedToNormalAccounts()
	{
		WarningEligibilityService normalService = new WarningEligibilityService(
			clientOutsidePvp(AccountType.NORMAL, EnumSet.noneOf(WorldType.class), SkullIcon.NONE),
			config(WarningDisplayMode.ALWAYS, false, false),
			null);
		assertTrue(normalService.getEligibility().isEquipmentWarningsVisible());

		WarningEligibilityService ironmanService = new WarningEligibilityService(
			clientOutsidePvp(AccountType.IRONMAN, EnumSet.noneOf(WorldType.class), SkullIcon.NONE),
			config(WarningDisplayMode.ALWAYS, false, false),
			null);
		assertFalse(ironmanService.getEligibility().isEquipmentWarningsVisible());
	}

	@Test
	public void deadmanNormalAccountsStillShowEquipmentWarningsByDefault()
	{
		WarningEligibilityService service = new WarningEligibilityService(
			clientOutsidePvp(AccountType.NORMAL, EnumSet.of(WorldType.DEADMAN), SkullIcon.NONE),
			config(WarningDisplayMode.ALWAYS, false, false),
			null);

		assertTrue(service.getEligibility().isEquipmentWarningsVisible());
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

	private static WildyQoLConfig config(WarningDisplayMode warningDisplayMode)
	{
		return config(warningDisplayMode, false);
	}

	private static WildyQoLConfig config(WarningDisplayMode warningDisplayMode, boolean onlyWhenSkulled)
	{
		return config(warningDisplayMode, onlyWhenSkulled, true);
	}

	private static WildyQoLConfig config(
		WarningDisplayMode warningDisplayMode,
		boolean onlyWhenSkulled,
		boolean showOnRestrictedAccounts)
	{
		return new WildyQoLConfig()
		{
			@Override
			public WarningDisplayMode warningDisplayMode()
			{
				return warningDisplayMode;
			}

			@Override
			public boolean onlyShowEquipmentWarningsWhenSkulled()
			{
				return onlyWhenSkulled;
			}

			@Override
			public boolean showEquipmentWarningsOnRestrictedAccounts()
			{
				return showOnRestrictedAccounts;
			}
		};
	}

	private static Client clientOutsidePvp()
	{
		return clientOutsidePvp(SkullIcon.NONE);
	}

	private static Client clientInPvp()
	{
		return client(
			AccountType.NORMAL,
			EnumSet.noneOf(WorldType.class),
			SkullIcon.NONE,
			true);
	}

	private static Client clientWithPvpState(AtomicBoolean inPvp)
	{
		return client(
			AccountType.NORMAL,
			EnumSet.noneOf(WorldType.class),
			SkullIcon.NONE,
			inPvp);
	}

	private static Client clientOutsidePvp(int skullIcon)
	{
		return clientOutsidePvp(AccountType.NORMAL, EnumSet.noneOf(WorldType.class), skullIcon);
	}

	private static Client clientOutsidePvp(
		AccountType accountType,
		EnumSet<WorldType> worldTypes,
		int skullIcon)
	{
		return client(accountType, worldTypes, skullIcon, false);
	}

	private static Client client(
		AccountType accountType,
		EnumSet<WorldType> worldTypes,
		int skullIcon,
		boolean inPvp)
	{
		return client(accountType, worldTypes, skullIcon, new AtomicBoolean(inPvp));
	}

	private static Client client(
		AccountType accountType,
		EnumSet<WorldType> worldTypes,
		int skullIcon,
		AtomicBoolean inPvp)
	{
		return (Client) Proxy.newProxyInstance(
			Client.class.getClassLoader(),
			new Class<?>[] {Client.class},
			(proxy, method, args) ->
			{
				if ("getWorldType".equals(method.getName()))
				{
					return worldTypes;
				}

				if ("getVarbitValue".equals(method.getName()))
				{
					return inPvp.get() ? 1 : 0;
				}

				if ("getLocalPlayer".equals(method.getName()))
				{
					return player(skullIcon);
				}

				if ("getAccountType".equals(method.getName()))
				{
					return accountType;
				}

				if ("toString".equals(method.getName()))
				{
					return "clientOutsidePvp";
				}

				return null;
			});
	}

	private static Player player(int skullIcon)
	{
		return (Player) Proxy.newProxyInstance(
			Player.class.getClassLoader(),
			new Class<?>[] {Player.class},
			(proxy, method, args) ->
			{
				if ("getSkullIcon".equals(method.getName()))
				{
					return skullIcon;
				}

				if ("toString".equals(method.getName()))
				{
					return "player";
				}

				return null;
			});
	}

	private static BankProximityService bankProximityService(boolean bankVisible, boolean pvpRelevantBankVisible)
	{
		return new BankProximityService(null)
		{
			@Override
			public boolean isBankVisible()
			{
				return bankVisible;
			}

			@Override
			public boolean isPvpRelevantBankVisible()
			{
				return pvpRelevantBankVisible;
			}
		};
	}
}
