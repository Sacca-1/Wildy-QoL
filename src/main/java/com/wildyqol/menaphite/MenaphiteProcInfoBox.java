package com.wildyqol.menaphite;

import com.wildyqol.WildyQoLConfig;
import java.awt.Color;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;

public class MenaphiteProcInfoBox extends InfoBox
{
	private final MenaphiteProcTimerService timerService;
	private final WildyQoLConfig config;
	private final Client client;

	public MenaphiteProcInfoBox(BufferedImage image, Plugin plugin, MenaphiteProcTimerService timerService, WildyQoLConfig config, Client client)
	{
		super(image, plugin);
		this.timerService = timerService;
		this.config = config;
		this.client = client;
	}

	@Override
	public String getText()
	{
		final int ticksRemaining = timerService.getTicksUntilNextProc(client.getTickCount());
		if (ticksRemaining < 0)
		{
			return "";
		}

		if (config.menaphiteProcTimerDisplayTicks())
		{
			return Integer.toString(ticksRemaining);
		}

		double secondsRemaining = timerService.getSecondsUntilNextProc();
		if (secondsRemaining < 0)
		{
			return "";
		}

		int seconds = (int) secondsRemaining;
		if (seconds < 0)
		{
			seconds = 0;
		}

		return Integer.toString(seconds);
	}

	@Override
	public Color getTextColor()
	{
		int ticksRemaining = timerService.getTicksUntilNextProc(client.getTickCount());
		if (config.menaphiteProcTimerDisplayTicks())
		{
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
