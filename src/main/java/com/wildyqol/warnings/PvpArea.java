package com.wildyqol.warnings;

import net.runelite.api.Client;
import net.runelite.api.WorldType;
import net.runelite.api.gameval.VarbitID;

public final class PvpArea
{
	private PvpArea()
	{
	}

	public static boolean isPvpArea(Client client)
	{
		return !client.getWorldType().contains(WorldType.DEADMAN)
			&& (client.getVarbitValue(VarbitID.INSIDE_WILDERNESS) == 1
			|| client.getVarbitValue(VarbitID.PVP_AREA_CLIENT) == 1);
	}
}
