package com.wildyqol.warnings;

import com.wildyqol.AreaDetection;
import net.runelite.api.Client;

public final class PvpArea
{
	private PvpArea()
	{
	}

	public static boolean isPvpArea(Client client)
	{
		return AreaDetection.isWarningPvpArea(client);
	}
}
