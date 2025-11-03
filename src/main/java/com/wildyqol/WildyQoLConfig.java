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
		keyName = "updateMessageShown111",
		name = "Update Message Shown v1.1.1",
		description = "Internal flag to track if the v1.1.1 update message has been shown",
		hidden = true,
        position = 3
	)
	default boolean updateMessageShown111()
	{
		return false;
	}
} 
