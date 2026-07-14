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
		NEVER("Off", 0),
		LEVEL_20("Level 20", 20),
		LEVEL_30("Level 30", 30);

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
		BANK("Banks"),
		PVP_BANKS("PvP banks"),
		PVP_AREA("PvP areas"),
		ALWAYS("Everywhere");

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

	enum SpecialAttackOrbBlockMode
	{
		NEVER("Off"),
		PVP("PvP areas"),
		ALWAYS("Everywhere");

		private final String label;

		SpecialAttackOrbBlockMode(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	enum PvpArenaSpellbookWarningMode
	{
		ANCIENT("Require Ancient"),
		STANDARD("Require Standard"),
		LUNAR("Require Lunar"),
		PLAYERS_MATCH("Players match"),
		NEVER("Off");

		private final String label;

		PvpArenaSpellbookWarningMode(String label)
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
		description = "Block common misclicks that can be costly in PvP.",
		position = 0,
		closedByDefault = false
	)
	String MISCLICK_PREVENTION_SECTION = "misclickPreventionSection";

	@ConfigSection(
		name = "PvP setup warnings",
		description = "Check your PvP setup for missing or low supplies.",
		position = 1,
		closedByDefault = false
	)
	String WARNINGS_SECTION = "warningsSection";

	@ConfigSection(
		name = "Where warnings appear",
		description = "Choose when PvP setup warnings are visible.",
		position = 2,
		closedByDefault = true
	)
	String WARNINGS_DISPLAY_SECTION = "warningsDisplaySection";

	@ConfigSection(
		name = "Warning amounts",
		description = "Choose when low-supply warnings start.",
		position = 3,
		closedByDefault = true
	)
	String WARNING_AMOUNTS_SECTION = "warningAmountsSection";

	@ConfigSection(
		name = "Extended freeze timers",
		description = "Improve freeze timers for PvP gear and forced movement.",
		position = 4,
		closedByDefault = true
	)
	String FREEZE_TIMERS_SECTION = "freezeTimersSection";

	@ConfigSection(
		name = "Prayer layouts",
		description = "Save reordered and hidden prayers for each character and PvP build.",
		position = 5,
		closedByDefault = true
	)
	String PRAYER_SECTION = "prayerSection";

	@ConfigSection(
		name = "Menaphite proc timer",
		description = "Track when your next menaphite remedy proc can trigger.",
		position = 6,
		closedByDefault = true
	)
	String MENAPHITE_PROC_TIMER_SECTION = "menaphiteProcTimerSection";

	@ConfigSection(
		name = "DMM overload proc timer",
		description = "Track when your next DMM overload proc can trigger.",
		position = 7,
		closedByDefault = true
	)
	String DMM_OVERLOAD_PROC_TIMER_SECTION = "dmmOverloadProcTimerSection";

	@ConfigSection(
		name = "PvP Arena",
		description = "Improve PvP Arena spellbook checks and fight visibility.",
		position = 8,
		closedByDefault = true
	)
	String PVP_ARENA_SECTION = "pvpArenaSection";

	@ConfigSection(
		name = "Items Kept on Death",
		description = "Show a more complete risk total in Items Kept on Death.",
		position = 9,
		closedByDefault = true
	)
	String ITEMS_KEPT_ON_DEATH_SECTION = "itemsKeptOnDeathSection";

	@ConfigItem(
		keyName = "runePouchBlocker",
		name = "Block rune pouch left-clicks",
		description = "Block left-clicks on rune pouches in PvP areas.<br>Right-click options still work.",
		position = 0,
		section = MISCLICK_PREVENTION_SECTION
	)
	default boolean runePouchBlocker()
	{
		return true;
	}

	@ConfigItem(
		keyName = "petSpellBlocker",
		name = "Block casting on pets",
		description = "Remove Cast and Examine options from pets while a spell is selected.",
		position = 1,
		section = MISCLICK_PREVENTION_SECTION
	)
	default boolean petSpellBlocker()
	{
		return true;
	}

	@ConfigItem(
		keyName = "emptyVialBlocker",
		name = "Block empty vial left-clicks",
		description = "Block left-clicks on empty vials in PvP areas.<br>Right-click options still work.",
		position = 2,
		section = MISCLICK_PREVENTION_SECTION
	)
	default boolean emptyVialBlocker()
	{
		return true;
	}

	@ConfigItem(
		keyName = "specialAttackOrbBlocker",
		name = "Block special attack orb",
		description = "Choose where clicks on the special attack orb are blocked.",
		position = 3,
		section = MISCLICK_PREVENTION_SECTION
	)
	default SpecialAttackOrbBlockMode specialAttackOrbBlocker()
	{
		return SpecialAttackOrbBlockMode.NEVER;
	}

	@ConfigItem(
		keyName = "marlinEqualsAnglerfish",
		name = "Show marlin as anglerfish",
		description = "Show the anglerfish icon on marlin in PvP areas.<br>This makes similar food easier to tell apart.",
		position = 4,
		section = MISCLICK_PREVENTION_SECTION
	)
	default boolean marlinEqualsAnglerfish()
	{
		return false;
	}

	@ConfigItem(
		keyName = "halibutEqualsKarambwan",
		name = "Show halibut as karambwan",
		description = "Show the karambwan icon on halibut in PvP areas.<br>This makes similar food easier to tell apart.",
		position = 5,
		section = MISCLICK_PREVENTION_SECTION
	)
	default boolean halibutEqualsKarambwan()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showIkodTrouverOverlay",
		name = "Show repair costs",
		description = "Add repair costs for broken and mangled untradeables to the risk total.",
		position = 0,
		section = ITEMS_KEPT_ON_DEATH_SECTION
	)
	default boolean showIkodRepairCostsOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "warningThresholdDefaultsMigratedV1",
		name = "Warning Threshold Defaults Migrated V1",
		description = "Internal flag for the first warning threshold default migration",
		hidden = true,
		position = 0
	)
	default boolean warningThresholdDefaultsMigratedV1()
	{
		return false;
	}

	@ConfigItem(
		keyName = "menaphiteProcTimerDisplayTicks",
		name = "Show in ticks",
		description = "Show the countdown in game ticks instead of seconds.",
		position = 1,
		section = MENAPHITE_PROC_TIMER_SECTION
	)
	default boolean menaphiteProcTimerDisplayTicks()
	{
		return false;
	}

	enum MenaphiteProcStatusBarMode
	{
		OFF("Off"),
		LEFT("Left"),
		RIGHT("Right");

		private final String label;

		MenaphiteProcStatusBarMode(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	@ConfigItem(
		keyName = "menaphiteProcTimerShowInfoBox",
		name = "Show info box",
		description = "Show the proc countdown in an info box.",
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
		description = "Show the proc countdown beside the inventory.<br>Choose which side it appears on, or turn it off.",
		position = 2,
		section = MENAPHITE_PROC_TIMER_SECTION
	)
	default MenaphiteProcStatusBarMode menaphiteProcTimerStatusBarMode()
	{
		return MenaphiteProcStatusBarMode.OFF;
	}

	@ConfigItem(
		keyName = "dmmOverloadProcTimerShowInfoBox",
		name = "Show info box",
		description = "Show the proc countdown in an info box.",
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
		description = "Show the countdown in game ticks instead of seconds.",
		position = 1,
		section = DMM_OVERLOAD_PROC_TIMER_SECTION
	)
	default boolean dmmOverloadProcTimerDisplayTicks()
	{
		return false;
	}

	enum DmmOverloadProcStatusBarMode
	{
		OFF("Off"),
		LEFT("Left"),
		RIGHT("Right");

		private final String label;

		DmmOverloadProcStatusBarMode(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	@ConfigItem(
		keyName = "dmmOverloadProcTimerStatusBarMode",
		name = "Status bar position",
		description = "Show the proc countdown beside the inventory.<br>Choose which side it appears on, or turn it off.",
		position = 2,
		section = DMM_OVERLOAD_PROC_TIMER_SECTION
	)
	default DmmOverloadProcStatusBarMode dmmOverloadProcTimerStatusBarMode()
	{
		return DmmOverloadProcStatusBarMode.OFF;
	}

	@ConfigItem(
		keyName = "protectItemInfoBox",
		name = "Protect Item reminder",
		description = "Show an info box in PvP areas when Protect Item is not active.",
		position = 0,
		section = WARNINGS_SECTION
	)
	default boolean protectItemInfoBox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "rangedAmmoWarnings",
		name = "Check ranged ammo",
		description = "Warn when supported ranged weapons have missing, low, or incompatible ammo.",
		position = 1,
		section = WARNINGS_SECTION
	)
	default boolean rangedAmmoWarnings()
	{
		return true;
	}

	@ConfigItem(
		keyName = "itemChargeWarnings",
		name = "Check item charges",
		description = "Warn when supported charged items are empty, low, or need to be checked.",
		position = 2,
		section = WARNINGS_SECTION
	)
	default boolean itemChargeWarnings()
	{
		return true;
	}

	@ConfigItem(
		keyName = "spellbookRuneWarnings",
		name = "Check spellbook and runes",
		description = "Warn when your spellbook and runes do not match.<br>Also warn when you have no supported casts or only a few left.",
		position = 3,
		section = WARNINGS_SECTION
	)
	default boolean spellbookRuneWarnings()
	{
		return true;
	}

	@ConfigItem(
		keyName = "removeObstructingPvpArenaScenery",
		name = "Remove obstructing scenery",
		description = "Remove tall Emir's Arena scenery that can block your view during fights.<br>"
			+ "This leaves a visible black gap around the arena.",
		position = 0,
		section = PVP_ARENA_SECTION
	)
	default boolean removeObstructingPvpArenaScenery()
	{
		return false;
	}

	@ConfigItem(
		keyName = "pvpArenaSpellbookWarningMode",
		name = "Check spellbooks",
		description = "Highlight PvP Arena spellbooks that do not match your chosen rule.<br>"
			+ "Require Ancient, Standard, or Lunar checks both players.<br>"
			+ "Players match checks that both players use the same spellbook.",
		position = 1,
		section = PVP_ARENA_SECTION
	)
	default PvpArenaSpellbookWarningMode pvpArenaSpellbookWarningMode()
	{
		return PvpArenaSpellbookWarningMode.ANCIENT;
	}

	@ConfigItem(
		keyName = "persistPrayerLayout",
		name = "Save prayer layouts",
		description = "Save reordered and hidden prayers for each character and supported PvP minigame build.<br>"
			+ "Requires prayer reordering in RuneLite's Prayer plugin.",
		position = 0,
		section = PRAYER_SECTION
	)
	default boolean persistPrayerReordering()
	{
		return true;
	}

	@ConfigItem(
		keyName = "gameMessageOnPrayerReordering",
		name = "Show restore message",
		description = "Show a game message when a saved prayer layout is restored.",
		position = 1,
		section = PRAYER_SECTION
	)
	default boolean gameMessageOnPrayerReordering()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideRsnInPrayerLayoutMessage",
		name = "Hide RSN",
		description = "Hide your character name in prayer layout restore messages.",
		position = 2,
		section = PRAYER_SECTION
	)
	default boolean hideRsnInPrayerLayoutMessage()
	{
		return false;
	}

	@ConfigItem(
		keyName = "teleportOutWarningMode",
		name = "Check teleport out",
		description = "Warn when you are not carrying or wearing a supported teleport out.<br>"
			+ "Uses the selected Wilderness level.",
		position = 4,
		section = WARNINGS_SECTION
	)
	default TeleportOutWarningMode teleportOutWarningMode()
	{
		return TeleportOutWarningMode.LEVEL_20;
	}

	@ConfigItem(
		keyName = "suboptimalRangedAmmoWarnings",
		name = "Warn about suboptimal ammo",
		description = "Warn when a supported ranged weapon only has lower-tier compatible ammo.",
		position = 5,
		section = WARNINGS_SECTION
	)
	default boolean suboptimalRangedAmmoWarnings()
	{
		return true;
	}

	@ConfigItem(
		keyName = "warningDisplayMode",
		name = "Show warnings at",
		description = "Choose where text overlay warnings appear.<br>"
			+ "Banks also keeps them visible for about one minute after leaving a PvP area.<br>"
			+ "PvP banks only shows them at supported PvP-related banks.",
		position = 0,
		section = WARNINGS_DISPLAY_SECTION
	)
	default WarningDisplayMode warningDisplayMode()
	{
		return WarningDisplayMode.BANK;
	}

	@ConfigItem(
		keyName = "onlyShowEquipmentWarningsWhenSkulled",
		name = "Only when skulled",
		description = "Only show text overlay warnings while you are skulled.",
		position = 1,
		section = WARNINGS_DISPLAY_SECTION
	)
	default boolean onlyShowEquipmentWarningsWhenSkulled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showEquipmentWarningsOnRestrictedAccounts",
		name = "Include Ironman and Leagues",
		description = "Show text overlay warnings on Ironman and Leagues characters.",
		position = 2,
		section = WARNINGS_DISPLAY_SECTION
	)
	default boolean showEquipmentWarningsOnRestrictedAccounts()
	{
		return true;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "atlatlDartMinimum",
		name = "Atlatl darts",
		description = "Warn when you have fewer than this many atlatl darts.<br>Set to 0 to disable this low-amount warning.",
		position = 0,
		section = WARNING_AMOUNTS_SECTION
	)
	default int atlatlDartMinimum()
	{
		return 100;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "boltMinimum",
		name = "Bolts",
		description = "Warn when you have fewer than this many compatible crossbow bolts.<br>Set to 0 to disable this low-amount warning.",
		position = 1,
		section = WARNING_AMOUNTS_SECTION
	)
	default int boltMinimum()
	{
		return 50;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "javelinMinimum",
		name = "Javelins",
		description = "Warn when you have fewer than this many dragon javelins.<br>Set to 0 to disable this low-amount warning.",
		position = 2,
		section = WARNING_AMOUNTS_SECTION
	)
	default int javelinMinimum()
	{
		return 50;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "arrowMinimum",
		name = "Arrows",
		description = "Warn when you have fewer than this many compatible arrows.<br>Set to 0 to disable this low-amount warning.",
		position = 3,
		section = WARNING_AMOUNTS_SECTION
	)
	default int arrowMinimum()
	{
		return 20;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "bowfaChargeMinimum",
		name = "Bowfa charges",
		description = "Warn when your Bowfa has fewer than this many charges.<br>Set to 0 to disable this low-amount warning.",
		position = 4,
		section = WARNING_AMOUNTS_SECTION
	)
	default int bowfaChargeMinimum()
	{
		return 300;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "tomeChargeMinimum",
		name = "Tome charges",
		description = "Warn when a supported tome has fewer than this many charges.<br>Set to 0 to disable this low-amount warning.",
		position = 5,
		section = WARNING_AMOUNTS_SECTION
	)
	default int tomeChargeMinimum()
	{
		return 50;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "toxicStaffChargeMinimum",
		name = "Toxic staff charges",
		description = "Warn when your toxic staff of the dead has fewer than this many charges.<br>Set to 0 to disable this low-amount warning.",
		position = 6,
		section = WARNING_AMOUNTS_SECTION
	)
	default int toxicStaffChargeMinimum()
	{
		return 300;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "serpentineHelmChargeMinimum",
		name = "Serpentine helm charges",
		description = "Warn when your serpentine helm has fewer than this many charges.<br>Set to 0 to disable this low-amount warning.",
		position = 7,
		section = WARNING_AMOUNTS_SECTION
	)
	default int serpentineHelmChargeMinimum()
	{
		return 300;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "teleBlockMinimum",
		name = "Tele Block casts",
		description = "Warn when you can cast Tele Block fewer than this many times.<br>Set to 0 to disable this low-amount warning.",
		position = 8,
		section = WARNING_AMOUNTS_SECTION
	)
	default int teleBlockMinimum()
	{
		return 10;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "entangleMinimum",
		name = "Freeze casts",
		description = "Warn when you can cast your supported freeze spell fewer than this many times.<br>Set to 0 to disable this low-amount warning.",
		position = 9,
		section = WARNING_AMOUNTS_SECTION
	)
	default int entangleMinimum()
	{
		return 50;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "surgeMinimum",
		name = "Damage casts",
		description = "Warn when you can cast your supported Standard spellbook damage spell fewer than this many times.<br>"
			+ "Set to 0 to disable this low-amount warning.",
		position = 10,
		section = WARNING_AMOUNTS_SECTION
	)
	default int surgeMinimum()
	{
		return 50;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "iceSpellMinimum",
		name = "Ice casts",
		description = "Warn when you can cast your supported Ancient ice spell fewer than this many times.<br>"
			+ "Set to 0 to disable this low-amount warning.",
		position = 11,
		section = WARNING_AMOUNTS_SECTION
	)
	default int iceSpellMinimum()
	{
		return 50;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "bloodSpellMinimum",
		name = "Blood Barrage casts",
		description = "Warn when you carry Blood Barrage runes but can cast it fewer than this many times.<br>"
			+ "Set to 0 to disable this low-amount warning.",
		position = 12,
		section = WARNING_AMOUNTS_SECTION
	)
	default int bloodSpellMinimum()
	{
		return 50;
	}

	@Range(min = 0)
	@ConfigItem(
		keyName = "vengeanceMinimum",
		name = "Vengeance casts",
		description = "Warn when you can cast Vengeance fewer than this many times.<br>Set to 0 to disable this low-amount warning.",
		position = 13,
		section = WARNING_AMOUNTS_SECTION
	)
	default int vengeanceMinimum()
	{
		return 10;
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
		name = "Disable RuneLite's freeze timer",
		description = "Turn off the freeze timer in Timers & Buffs when extended freeze timers are enabled.",
		position = 1,
		section = FREEZE_TIMERS_SECTION
	)
	default boolean disableDuplicateFreezeTimers()
	{
		return true;
	}

	@ConfigItem(
		keyName = "preserveFreezeTimerOnForcedMovement",
		name = "Keep timer after forced movement",
		description = "Keep the freeze timer running when mithril or adamant seeds move you.<br>"
			+ "Also applies to dragon spear/hasta special attacks.",
		position = 2,
		section = FREEZE_TIMERS_SECTION
	)
	default boolean preserveFreezeTimerOnForcedMovement()
	{
		return true;
	}
}
