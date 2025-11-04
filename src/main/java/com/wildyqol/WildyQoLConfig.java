package com.wildyqol;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("wildyqol")
public interface WildyQoLConfig extends Config
{
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

	@ConfigItem(
		keyName = "petSpellBlocker",
		name = "Pet Spell Blocker",
		description = "Removes 'Cast' menu entries on pets",
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
		keyName = "updateMessageShown120",
		name = "Update Message Shown v1.2.0",
		description = "Internal flag to track if the v1.2.0 update message has been shown",
		hidden = true,
        position = 3
	)
	default boolean updateMessageShown120()
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
}
