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
		description = "Prevents left-clicking 'Use' on empty vials in dangerous areas",
		position = 2,
		section = MISCLICK_PREVENTION_SECTION
	)
	default boolean emptyVialBlocker()
	{
		return false;
	}

	@ConfigItem(
		keyName = "updateMessageShown110",
		name = "Update Message Shown v1.1.0",
		description = "Internal flag to track if the v1.1.0 update message has been shown",
		hidden = true,
        position = 3
	)
	default boolean updateMessageShown110()
	{
		return false;
	}
} 
