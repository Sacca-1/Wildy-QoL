package com.wildyqol.proctimers;

import java.time.Duration;
import java.time.Instant;

public abstract class ProcTimerService
{
	private static final int PROC_INTERVAL_TICKS = 25;
	private static final double TICK_LENGTH_SECONDS = 0.6d;
	private static final Duration PROC_INTERVAL_DURATION = Duration.ofMillis((long) (PROC_INTERVAL_TICKS * TICK_LENGTH_SECONDS * 1000));

	private boolean active;
	private int nextProcTick = -1;
	private int lastVarbitValue = -1;
	private Instant nextProcInstant;

	public void reset()
	{
		active = false;
		nextProcTick = -1;
		lastVarbitValue = -1;
		nextProcInstant = null;
	}

	public void handleVarbitUpdate(int varbitValue, int currentTick)
	{
		if (varbitValue <= 0)
		{
			reset();
			return;
		}

		if (varbitValue <= 1)
		{
			active = false;
			nextProcTick = -1;
			lastVarbitValue = varbitValue;
			nextProcInstant = null;
			return;
		}

		if (!active || varbitValue != lastVarbitValue)
		{
			nextProcTick = currentTick + PROC_INTERVAL_TICKS;
			nextProcInstant = Instant.now().plus(PROC_INTERVAL_DURATION);
			active = true;
		}

		lastVarbitValue = varbitValue;
	}

	public int getTicksUntilNextProc(int currentTick)
	{
		if (!active || nextProcTick < 0)
		{
			return -1;
		}

		return Math.max(0, nextProcTick - currentTick);
	}

	public double getSecondsUntilNextProc()
	{
		if (!active || nextProcInstant == null)
		{
			return -1;
		}

		Duration remaining = Duration.between(Instant.now(), nextProcInstant);
		if (remaining.isNegative())
		{
			return 0.0;
		}

		return remaining.toMillis() / 1000.0;
	}

	public boolean isActive()
	{
		return active;
	}

	public double getProcIntervalSeconds()
	{
		return PROC_INTERVAL_DURATION.toMillis() / 1000.0;
	}

	public int getProcIntervalTicks()
	{
		return PROC_INTERVAL_TICKS;
	}

	public double getFractionRemaining()
	{
		if (!active || nextProcInstant == null)
		{
			return -1;
		}

		final double totalMillis = PROC_INTERVAL_DURATION.toMillis();
		if (totalMillis <= 0)
		{
			return -1;
		}

		final Duration remaining = Duration.between(Instant.now(), nextProcInstant);
		final double millisRemaining = remaining.toMillis();

		if (millisRemaining <= 0)
		{
			return 0.0;
		}

		final double fraction = millisRemaining / totalMillis;
		if (fraction <= 0)
		{
			return 0.0;
		}

		if (fraction >= 1)
		{
			return 1.0;
		}

		return fraction;
	}

	@SuppressWarnings("unused")
	public double getFractionRemainingTicks(int currentTick)
	{
		if (!active || nextProcTick < 0)
		{
			return -1;
		}

		if (PROC_INTERVAL_TICKS <= 0)
		{
			return -1;
		}

		int ticksRemaining = Math.max(0, nextProcTick - currentTick);
		if (ticksRemaining <= 0)
		{
			return 0.0;
		}

		double fraction = (double) ticksRemaining / PROC_INTERVAL_TICKS;
		if (fraction <= 0)
		{
			return 0.0;
		}

		if (fraction >= 1)
		{
			return 1.0;
		}

		return fraction;
	}
}
