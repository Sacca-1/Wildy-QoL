package com.wildyqol.proctimers;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.function.BooleanSupplier;
import net.runelite.api.Client;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;

public class ProcTimerInfoBox extends InfoBox
{
	private final ProcTimerService timerService;
	private final Client client;
	private final BooleanSupplier displayTicks;

	public ProcTimerInfoBox(
		BufferedImage image,
		Plugin plugin,
		ProcTimerService timerService,
		Client client,
		BooleanSupplier displayTicks)
	{
		super(image, plugin);
		this.timerService = timerService;
		this.client = client;
		this.displayTicks = displayTicks;
	}

	@Override
	public String getText()
	{
		final int ticksRemaining = timerService.getTicksUntilNextProc(client.getTickCount());
		if (ticksRemaining < 0)
		{
			return "";
		}

		if (displayTicks.getAsBoolean())
		{
			return Integer.toString(ticksRemaining);
		}

		double secondsRemaining = timerService.getSecondsUntilNextProc();
		if (secondsRemaining < 0)
		{
			return "";
		}

		return Integer.toString(Math.max(0, (int) secondsRemaining));
	}

	@Override
	public Color getTextColor()
	{
		if (displayTicks.getAsBoolean())
		{
			int ticksRemaining = timerService.getTicksUntilNextProc(client.getTickCount());
			if (ticksRemaining >= 0 && ticksRemaining <= 5)
			{
				return Color.RED;
			}
		}
		else
		{
			double secondsRemaining = timerService.getSecondsUntilNextProc();
			if (secondsRemaining >= 0 && secondsRemaining <= 3)
			{
				return Color.RED;
			}
		}

		return Color.WHITE;
	}
}
