package com.wildyqol.proctimers.dmmoverload;

import com.wildyqol.WildyQoLConfig;
import com.wildyqol.proctimers.ProcTimerInfoBox;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.client.plugins.Plugin;

public class DmmOverloadProcInfoBox extends ProcTimerInfoBox
{
	public DmmOverloadProcInfoBox(BufferedImage image, Plugin plugin, DmmOverloadProcTimerService timerService, WildyQoLConfig config, Client client)
	{
		super(image, plugin, timerService, client, config::dmmOverloadProcTimerDisplayTicks);
	}
}
