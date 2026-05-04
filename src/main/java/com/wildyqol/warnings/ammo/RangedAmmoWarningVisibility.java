package com.wildyqol.warnings.ammo;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

class RangedAmmoWarningVisibility
{
	static final int PVP_GRACE_TICKS = 15;

	@Nullable
	private String pvpGraceWarningText;
	private boolean wasInPvp;
	private int pvpGraceTicksRemaining;

	List<RangedAmmoWarning> update(List<RangedAmmoWarning> warnings, boolean enabled, boolean inPvp, boolean gameTick)
	{
		if (inPvp && gameTick && pvpGraceTicksRemaining > 0)
		{
			pvpGraceTicksRemaining--;
		}

		if (!enabled || warnings.isEmpty())
		{
			reset(inPvp);
			return ImmutableList.of();
		}

		if (!inPvp)
		{
			pvpGraceWarningText = getWarningKey(warnings);
			pvpGraceTicksRemaining = PVP_GRACE_TICKS;
			wasInPvp = false;
			return warnings;
		}

		if (!wasInPvp)
		{
			pvpGraceWarningText = getWarningKey(warnings);
			pvpGraceTicksRemaining = PVP_GRACE_TICKS;
		}

		wasInPvp = true;
		if (pvpGraceTicksRemaining <= 0 || !getWarningKey(warnings).equals(pvpGraceWarningText))
		{
			return ImmutableList.of();
		}

		return warnings;
	}

	Optional<RangedAmmoWarning> update(Optional<RangedAmmoWarning> warning, boolean enabled, boolean inPvp, boolean gameTick)
	{
		List<RangedAmmoWarning> warnings = warning
			.map(ImmutableList::of)
			.orElseGet(ImmutableList::of);
		List<RangedAmmoWarning> visibleWarnings = update(warnings, enabled, inPvp, gameTick);
		return visibleWarnings.isEmpty() ? Optional.empty() : Optional.of(visibleWarnings.get(0));
	}

	void reset()
	{
		reset(false);
	}

	private void reset(boolean inPvp)
	{
		pvpGraceWarningText = null;
		pvpGraceTicksRemaining = 0;
		wasInPvp = inPvp;
	}

	private String getWarningKey(List<RangedAmmoWarning> warnings)
	{
		StringBuilder key = new StringBuilder();
		for (RangedAmmoWarning warning : warnings)
		{
			if (key.length() > 0)
			{
				key.append('\n');
			}
			key.append(warning.getText());
		}
		return key.toString();
	}
}
