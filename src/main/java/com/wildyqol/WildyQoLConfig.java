package com.wildyqol;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("wildyqol")
public interface WildyQoLConfig extends Config
{
	enum TeleportOutWarningMode
	{
		NEVER("Never", 0),
		LEVEL_20("20", 20),
		LEVEL_30("30", 30);

		private final String label;
		private final int wildernessLevel;

		TeleportOutWarningMode(String label, int wildernessLevel)
		{
			this.label = label;
			this.wildernessLevel = wildernessLevel;
		}

		public int getWildernessLevel()
		{
			return wildernessLevel;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	enum WarningDisplayMode
	{
		BANK("Bank"),
		PVP_BANKS("PvP banks"),
		PVP_AREA("PvP area"),
		ALWAYS("Always");

		private final String label;

		WarningDisplayMode(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	@ConfigSection(
		name = "Misclick prevention",
		description = "Settings to help avoid dangerous misclicks",
		position = 0,
		closedByDefault = false
	)
	String MISCLICK_PREVENTION_SECTION = "misclickPreventionSection";

	@ConfigSection(
		name = "Items Kept on Death",
		description = "Settings for the Items Kept on Death widget",
		position = 1,
		closedByDefault = false
	)
	String ITEMS_KEPT_ON_DEATH_SECTION = "itemsKeptOnDeathSection";

	@ConfigSection(
		name = "Menaphite proc timer",
		description = "Settings for tracking the next menaphite remedy proc",
		position = 2,
		closedByDefault = false
	)
	String MENAPHITE_PROC_TIMER_SECTION = "menaphiteProcTimerSection";

	@ConfigSection(
		name = "DMM overload proc timer",
		description = "Settings for tracking the next DMM overload proc",
		position = 3,
		closedByDefault = false
	)
	String DMM_OVERLOAD_PROC_TIMER_SECTION = "dmmOverloadProcTimerSection";

	@ConfigSection(
		name = "Freeze timers",
		description = "Extended freeze timer settings",
		position = 4,
		closedByDefault = false
	)
	String FREEZE_TIMERS_SECTION = "freezeTimersSection";

	@ConfigSection(
		name = "Warnings",
		description = "Warnings for risky PvP states",
		position = 5,
		closedByDefault = false
	)
	String WARNINGS_SECTION = "warningsSection";

	@ConfigSection(
		name = "Warnings (advanced)",
		description = "Advanced warning thresholds",
		position = 6,
		closedByDefault = true
	)
	String WARNINGS_ADVANCED_SECTION = "warningsAdvancedSection";

	@ConfigItem(
		keyName = "runePouchBlocker",
		name = "Rune Pouch Blocker",
		description = "Block left-click on rune pouches (PvP areas only)",
		position = 0,
		section = MISCLICK_PREVENTION_SECTION
	)
	default boolean runePouchBlocker()
	{
		return true;
	}

	@ConfigItem(
		keyName = "petSpellBlocker",
		name = "Pet Spell Blocker",
		description = "Removes spell-cast menu entries on pets and suppresses pet examine while a spell is selected",
		position = 1,
		section = MISCLICK_PREVENTION_SECTION
	)
	default boolean petSpellBlocker()
	{
		return true;
	}

	@ConfigItem(
		keyName = "emptyVialBlocker",
		name = "Empty Vial Blocker",
		description = "Prevents left-clicking 'Use' on empty vials",
		position = 2,
		section = MISCLICK_PREVENTION_SECTION
	)
	default boolean emptyVialBlocker()
	{
		return false;
	}

	@ConfigItem(
		keyName = "marlinEqualsAnglerfish",
		name = "Marlin = Anglerfish",
		description = "Replace marlin inventory icon with anglerfish (PvP areas only)",
		position = 3,
		section = MISCLICK_PREVENTION_SECTION
	)
	default boolean marlinEqualsAnglerfish()
	{
		return false;
	}

	@ConfigItem(
		keyName = "halibutEqualsKarambwan",
		name = "Halibut = Karambwan",
		description = "Replace halibut inventory icon with karambwan (PvP areas only)",
		position = 4,
		section = MISCLICK_PREVENTION_SECTION
	)
	default boolean halibutEqualsKarambwan()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showIkodTrouverOverlay",
		name = "Trouver Cost Overlay",
		description = "Show Trouver parchment cost + 500k gp in the Items Kept on Death interface",
		position = 0,
		section = ITEMS_KEPT_ON_DEATH_SECTION
	)
	default boolean showIkodTrouverOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "updateMessageShown130",
		name = "Update Message Shown v1.3.0",
		description = "Internal flag to track if the v1.3.0 update message has been shown",
		hidden = true,
		position = 3
	)
	default boolean updateMessageShown130()
	{
		return false;
	}

	@ConfigItem(
		keyName = "menaphiteProcTimerDisplayTicks",
		name = "Show in ticks",
		description = "Display the menaphite proc countdown using game ticks instead of seconds",
		position = 1,
		section = MENAPHITE_PROC_TIMER_SECTION
	)
	default boolean menaphiteProcTimerDisplayTicks()
	{
		return false;
	}

	enum MenaphiteProcStatusBarMode
	{
		OFF,
		LEFT,
		RIGHT
	}

	@ConfigItem(
		keyName = "menaphiteProcTimerShowInfoBox",
		name = "Show infobox",
		description = "Display the menaphite proc timer as an info box",
		position = 0,
		section = MENAPHITE_PROC_TIMER_SECTION
	)
	default boolean menaphiteProcTimerShowInfoBox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "menaphiteProcTimerStatusBarMode",
		name = "Status bar position",
		description = "Display the menaphite proc timer as a status bar next to the inventory",
		position = 2,
		section = MENAPHITE_PROC_TIMER_SECTION
	)
	default MenaphiteProcStatusBarMode menaphiteProcTimerStatusBarMode()
	{
		return MenaphiteProcStatusBarMode.OFF;
	}

	@ConfigItem(
		keyName = "dmmOverloadProcTimerShowInfoBox",
		name = "Show infobox",
		description = "Display the DMM overload proc timer as an info box",
		position = 0,
		section = DMM_OVERLOAD_PROC_TIMER_SECTION
	)
	default boolean dmmOverloadProcTimerShowInfoBox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "dmmOverloadProcTimerDisplayTicks",
		name = "Show in ticks",
		description = "Display the DMM overload proc countdown using game ticks instead of seconds",
		position = 1,
		section = DMM_OVERLOAD_PROC_TIMER_SECTION
	)
	default boolean dmmOverloadProcTimerDisplayTicks()
	{
		return false;
	}

	enum DmmOverloadProcStatusBarMode
	{
		OFF,
		LEFT,
		RIGHT
	}

	@ConfigItem(
		keyName = "dmmOverloadProcTimerStatusBarMode",
		name = "Status bar position",
		description = "Display the DMM overload proc timer as a status bar next to the inventory",
		position = 2,
		section = DMM_OVERLOAD_PROC_TIMER_SECTION
	)
	default DmmOverloadProcStatusBarMode dmmOverloadProcTimerStatusBarMode()
	{
		return DmmOverloadProcStatusBarMode.OFF;
	}

	@ConfigItem(
		keyName = "protectItemInfoBox",
		name = "Protect Item Infobox",
		description = "Show an infobox when in PvP area and Protect Item is not active",
		position = 0,
		section = WARNINGS_SECTION
	)
	default boolean protectItemInfoBox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "rangedAmmoWarnings",
		name = "Ranged ammo warnings",
		description = "Show a text overlay when recognized ranged weapons are missing compatible ammo",
		position = 1,
		section = WARNINGS_SECTION
	)
	default boolean rangedAmmoWarnings()
	{
		return true;
	}

	@ConfigItem(
		keyName = "itemChargeWarnings",
		name = "Item charges warnings",
		description = "Show a text overlay when recognized charged items are empty or low",
		position = 2,
		section = WARNINGS_SECTION
	)
	default boolean itemChargeWarnings()
	{
		return true;
	}

	@ConfigItem(
		keyName = "spellbookRuneWarnings",
		name = "Spellbook/rune warnings",
		description = "Show a text overlay when recognized runes and spellbook do not match or are insufficient",
		position = 3,
		section = WARNINGS_SECTION
	)
	default boolean spellbookRuneWarnings()
	{
		return true;
	}

	@ConfigItem(
		keyName = "teleportOutWarningMode",
		name = "Teleport out warning",
		description = "Show a text overlay when no recognized teleport out of the wilderness is carried or worn.<br>"
			+ "Uses the selected Wilderness level.",
		position = 4,
		section = WARNINGS_SECTION
	)
	default TeleportOutWarningMode teleportOutWarningMode()
	{
		return TeleportOutWarningMode.LEVEL_20;
	}

	@ConfigItem(
		keyName = "warningDisplayMode",
		name = "Warn at",
		description = "Choose where ranged ammo, item charge, spellbook/rune, and teleport-out text warnings appear.<br>"
			+ "Bank also shows for 100 ticks after leaving a PvP area, and existing warnings stay briefly after entering PvP.<br>"
			+ "PvP banks limits bank warnings to configured PvP-relevant banks.",
		position = 0,
		section = WARNINGS_ADVANCED_SECTION
	)
	default WarningDisplayMode warningDisplayMode()
	{
		return WarningDisplayMode.BANK;
	}

	@ConfigItem(
		keyName = "onlyShowEquipmentWarningsWhenSkulled",
		name = "Only when skulled",
		description = "Only show ranged ammo, item charge, spellbook/rune, and teleport-out text warnings while skulled",
		position = 1,
		section = WARNINGS_ADVANCED_SECTION
	)
	default boolean onlyShowEquipmentWarningsWhenSkulled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showEquipmentWarningsOnRestrictedAccounts",
		name = "Show on ironman/leagues",
		description = "Show ranged ammo, item charge, spellbook/rune, and teleport-out text warnings on ironman and leagues accounts",
		position = 2,
		section = WARNINGS_ADVANCED_SECTION
	)
	default boolean showEquipmentWarningsOnRestrictedAccounts()
	{
		return true;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "atlatlDartMinimum",
		name = "Atlatl dart minimum",
		description = "Minimum atlatl darts required before warning",
		position = 3,
		section = WARNINGS_ADVANCED_SECTION
	)
	default int atlatlDartMinimum()
	{
		return 250;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "boltMinimum",
		name = "Bolt minimum",
		description = "Minimum bolts required before warning",
		position = 4,
		section = WARNINGS_ADVANCED_SECTION
	)
	default int boltMinimum()
	{
		return 100;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "javelinMinimum",
		name = "Javelin minimum",
		description = "Minimum javelins required before warning",
		position = 5,
		section = WARNINGS_ADVANCED_SECTION
	)
	default int javelinMinimum()
	{
		return 100;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "arrowMinimum",
		name = "Arrow minimum",
		description = "Minimum arrows required before warning",
		position = 6,
		section = WARNINGS_ADVANCED_SECTION
	)
	default int arrowMinimum()
	{
		return 100;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "bowfaChargeMinimum",
		name = "Bowfa charges minimum",
		description = "Minimum Bow of faerdhinen charges required before warning",
		position = 7,
		section = WARNINGS_ADVANCED_SECTION
	)
	default int bowfaChargeMinimum()
	{
		return 250;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "tomeChargeMinimum",
		name = "Tome charges minimum",
		description = "Minimum tome charges required before warning",
		position = 8,
		section = WARNINGS_ADVANCED_SECTION
	)
	default int tomeChargeMinimum()
	{
		return 50;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "teleBlockMinimum",
		name = "TB minimum",
		description = "Minimum Tele Block casts or sacks required before warning",
		position = 9,
		section = WARNINGS_ADVANCED_SECTION
	)
	default int teleBlockMinimum()
	{
		return 10;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "entangleMinimum",
		name = "Entangle minimum",
		description = "Minimum freeze casts or sacks required before warning",
		position = 10,
		section = WARNINGS_ADVANCED_SECTION
	)
	default int entangleMinimum()
	{
		return 50;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "surgeMinimum",
		name = "Surge minimum",
		description = "Minimum standard damage casts or sacks required before warning",
		position = 15,
		section = WARNINGS_ADVANCED_SECTION
	)
	default int surgeMinimum()
	{
		return 100;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "iceSpellMinimum",
		name = "Ice spell minimum",
		description = "Minimum ancient ice casts or sacks required before warning",
		position = 16,
		section = WARNINGS_ADVANCED_SECTION
	)
	default int iceSpellMinimum()
	{
		return 100;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "bloodSpellMinimum",
		name = "Blood spell minimum",
		description = "Minimum ancient blood casts required before warning when blood spell runes are present",
		position = 17,
		section = WARNINGS_ADVANCED_SECTION
	)
	default int bloodSpellMinimum()
	{
		return 50;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "vengeanceMinimum",
		name = "Vengeance minimum",
		description = "Minimum vengeance casts or sacks required before warning",
		position = 18,
		section = WARNINGS_ADVANCED_SECTION
	)
	default int vengeanceMinimum()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "suboptimalRangedAmmoWarnings",
		name = "Suboptimal ammo warning",
		description = "Warn when recognized ranged weapons have only lower-tier compatible ammo",
		position = 19,
		section = WARNINGS_ADVANCED_SECTION
	)
	default boolean suboptimalRangedAmmoWarnings()
	{
		return true;
	}

	@ConfigItem(
		keyName = "enableExtendedFreezeTimersV2",
		name = "Extended freeze timers",
		description = "Use extended freeze timers that account for opponent gear.<br>"
			+ "Includes Ancient sceptres, Zuriel's staves, and Swampbark.",
		position = 0,
		section = FREEZE_TIMERS_SECTION
	)
	default boolean enableExtendedFreezeTimers()
	{
		return true;
	}

	@ConfigItem(
		keyName = "warnDuplicateFreezeTimers",
		name = "Warn about duplicates",
		description = "Warn in chat if the core Timers & Buffs freeze timer stays enabled",
		position = 1,
		section = FREEZE_TIMERS_SECTION
	)
	default boolean warnDuplicateFreezeTimers()
	{
		return true;
	}

	@ConfigItem(
		keyName = "preserveFreezeTimerOnForcedMovement",
		name = "Ignore forced movement",
		description = "Keep the freeze timer running when mithril/adamant seeds move you.<br>"
			+ "Also applies to dragon spear/hasta special attacks.",
		position = 2,
		section = FREEZE_TIMERS_SECTION
	)
	default boolean preserveFreezeTimerOnForcedMovement()
	{
		return true;
	}
}
