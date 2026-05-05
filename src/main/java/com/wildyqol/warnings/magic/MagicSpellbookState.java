package com.wildyqol.warnings.magic;

import java.util.Map;
import java.util.Set;
import lombok.Value;

@Value
class MagicSpellbookState
{
	MagicSpellbook spellbook;
	Map<MagicRune, Integer> runeCounts;
	Map<Integer, Integer> itemCounts;
	Set<MagicRune> providedRunes;
	boolean magicCape;
	boolean validGodStaff;
	boolean chargedWildySceptre;
	boolean unchargedWildySceptre;

	boolean hasValidGodStaff()
	{
		return validGodStaff;
	}
}
