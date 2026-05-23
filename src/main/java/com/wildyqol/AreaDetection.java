package com.wildyqol;

import net.runelite.api.Client;
import net.runelite.api.WorldType;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;

public final class AreaDetection
{
	private AreaDetection()
	{
	}

	public static boolean isActiveLmsGame(Client client)
	{
		return client.getVarbitValue(VarbitID.BR_INGAME) == 1;
	}

	public static boolean isRawPvpArea(Client client)
	{
		return client.getVarbitValue(VarbitID.INSIDE_WILDERNESS) == 1
			|| client.getVarbitValue(VarbitID.PVP_AREA_CLIENT) == 1;
	}

	public static boolean isWarningPvpArea(Client client)
	{
		return isRawPvpArea(client);
	}

	public static boolean isDeadmanWorld(Client client)
	{
		return client.getWorldType().contains(WorldType.DEADMAN);
	}

	public static boolean isMisclickPvpArea(Client client)
	{
		return isRawPvpArea(client) && !isDeadmanGuardedZone(client);
	}

	public static boolean isPvpArenaMatch(Client client)
	{
		return client.getVarbitValue(VarbitID.PVPA_BATTLEAREA_STATUS) > 0;
	}

	public static boolean isPvpArenaWorld(Client client)
	{
		return client.getWorldType().contains(WorldType.PVP_ARENA);
	}

	public static boolean isDeadmanGuardedZone(Client client)
	{
		return isDeadmanWorld(client)
			&& isWidgetVisible(client, InterfaceID.PvpIcons.PVPW_SAFE);
	}

	private static boolean isWidgetVisible(Client client, int widgetId)
	{
		Widget widget = client.getWidget(widgetId);
		return widget != null && !widget.isHidden();
	}
}
