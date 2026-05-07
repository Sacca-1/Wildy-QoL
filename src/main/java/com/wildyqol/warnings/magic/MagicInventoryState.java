package com.wildyqol.warnings.magic;

import java.util.Map;
import java.util.Set;
import lombok.Value;

@Value
public class MagicInventoryState
{
	MagicSpellbook spellbook;
	int magicLevel;
	Set<Integer> itemIds;
	Map<MagicRune, Integer> runeCounts;
	Map<Integer, Integer> itemCounts;
	Set<MagicRune> providedRunes;
	Map<MagicRune, Integer> tomeCharges;
	boolean magicCape;
	boolean validGodStaff;
	boolean chargedWildySceptre;
	boolean unchargedWildySceptre;

	boolean hasValidGodStaff()
	{
		return validGodStaff;
	}
}
