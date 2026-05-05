package com.wildyqol.warnings;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;

public class WarningVisibility<T>
{
	public static final int PVP_GRACE_TICKS = 15;

	private final Function<T, String> textProvider;

	@Nullable
	private String pvpGraceWarningText;
	private boolean wasInPvp;
	private int pvpGraceTicksRemaining;

	public WarningVisibility(Function<T, String> textProvider)
	{
		this.textProvider = textProvider;
	}

	public List<T> update(List<T> warnings, boolean enabled, boolean inPvp, boolean gameTick)
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

	public Optional<T> update(Optional<T> warning, boolean enabled, boolean inPvp, boolean gameTick)
	{
		List<T> warnings = warning
			.map(ImmutableList::of)
			.orElseGet(ImmutableList::of);
		List<T> visibleWarnings = update(warnings, enabled, inPvp, gameTick);
		return visibleWarnings.isEmpty() ? Optional.empty() : Optional.of(visibleWarnings.get(0));
	}

	public void reset()
	{
		reset(false);
	}

	private void reset(boolean inPvp)
	{
		pvpGraceWarningText = null;
		pvpGraceTicksRemaining = 0;
		wasInPvp = inPvp;
	}

	private String getWarningKey(List<T> warnings)
	{
		StringBuilder key = new StringBuilder();
		for (T warning : warnings)
		{
			if (key.length() > 0)
			{
				key.append('\n');
			}
			key.append(textProvider.apply(warning));
		}
		return key.toString();
	}
}
