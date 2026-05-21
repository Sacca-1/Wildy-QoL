package com.wildyqol.warnings;

import com.google.common.collect.ImmutableList;
import com.wildyqol.WildyQoLConfig.WarningDisplayMode;
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
	private int pvpGraceTicksRemaining;

	public WarningVisibility(Function<T, String> textProvider)
	{
		this.textProvider = textProvider;
	}

	public List<T> update(
		List<T> warnings,
		boolean enabled,
		WarningDisplayMode warningDisplayMode,
		boolean inPvp,
		boolean eligibleOutsidePvp,
		boolean gameTick)
	{
		if (!enabled || warnings.isEmpty())
		{
			reset();
			return ImmutableList.of();
		}

		if (warningDisplayMode == WarningDisplayMode.ALWAYS)
		{
			reset();
			return warnings;
		}

		if (warningDisplayMode == WarningDisplayMode.PVP_AREA)
		{
			reset();
			return inPvp ? warnings : ImmutableList.of();
		}

		if (inPvp && gameTick && pvpGraceTicksRemaining > 0)
		{
			pvpGraceTicksRemaining--;
		}

		if (!inPvp)
		{
			if (!eligibleOutsidePvp)
			{
				reset();
				return ImmutableList.of();
			}

			pvpGraceWarningText = getWarningKey(warnings);
			pvpGraceTicksRemaining = PVP_GRACE_TICKS;
			return warnings;
		}

		String warningKey = getWarningKey(warnings);
		if (pvpGraceTicksRemaining <= 0 || !warningKey.equals(pvpGraceWarningText))
		{
			return ImmutableList.of();
		}

		return warnings;
	}

	public Optional<T> update(
		Optional<T> warning,
		boolean enabled,
		WarningDisplayMode warningDisplayMode,
		boolean inPvp,
		boolean eligibleOutsidePvp,
		boolean gameTick)
	{
		List<T> warnings = warning
			.map(ImmutableList::of)
			.orElseGet(ImmutableList::of);
		List<T> visibleWarnings = update(warnings, enabled, warningDisplayMode, inPvp, eligibleOutsidePvp, gameTick);
		return visibleWarnings.isEmpty() ? Optional.empty() : Optional.of(visibleWarnings.get(0));
	}

	public void reset()
	{
		pvpGraceWarningText = null;
		pvpGraceTicksRemaining = 0;
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
