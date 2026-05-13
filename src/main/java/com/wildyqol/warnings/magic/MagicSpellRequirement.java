package com.wildyqol.warnings.magic;

import com.google.common.collect.ImmutableMap;
import java.util.EnumMap;
import java.util.Map;
import lombok.Value;

@Value
class MagicSpellRequirement
{
	Map<MagicRune, Integer> runes;

	static MagicSpellRequirement of(Object... runeCosts)
	{
		EnumMap<MagicRune, Integer> runes = new EnumMap<>(MagicRune.class);
		for (int i = 0; i < runeCosts.length; i += 2)
		{
			runes.put((MagicRune) runeCosts[i], (Integer) runeCosts[i + 1]);
		}
		return new MagicSpellRequirement(ImmutableMap.copyOf(runes));
	}
}
