package com.wildyqol.proctimers.menaphite;

import com.wildyqol.WildyQoLConfig;
import com.wildyqol.proctimers.ProcTimerInfoBox;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.client.plugins.Plugin;

public class MenaphiteProcInfoBox extends ProcTimerInfoBox
{
	public MenaphiteProcInfoBox(BufferedImage image, Plugin plugin, MenaphiteProcTimerService timerService, WildyQoLConfig config, Client client)
	{
		super(image, plugin, timerService, client, config::menaphiteProcTimerDisplayTicks);
	}
}
