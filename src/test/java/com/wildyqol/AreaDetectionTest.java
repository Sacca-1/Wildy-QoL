package com.wildyqol;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Proxy;
import java.util.EnumSet;
import net.runelite.api.Client;
import net.runelite.api.WorldType;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import org.junit.Test;

public class AreaDetectionTest
{
	@Test
	public void activeLmsGameUsesBrIngameVarbit()
	{
		assertTrue(AreaDetection.isActiveLmsGame(client(EnumSet.noneOf(WorldType.class), true, false, false, false)));
		assertFalse(AreaDetection.isActiveLmsGame(client(EnumSet.noneOf(WorldType.class), false, false, false, false)));
	}

	@Test
	public void rawPvpAreaUsesWildernessOrPvpAreaVarbits()
	{
		assertTrue(AreaDetection.isRawPvpArea(client(EnumSet.noneOf(WorldType.class), false, false, false, true)));
		assertFalse(AreaDetection.isRawPvpArea(client(EnumSet.noneOf(WorldType.class), false, false, false, false)));
	}

	@Test
	public void warningPvpAreaUsesRawPvpArea()
	{
		assertTrue(AreaDetection.isWarningPvpArea(client(EnumSet.noneOf(WorldType.class), false, false, false, true)));
		assertTrue(AreaDetection.isWarningPvpArea(client(EnumSet.of(WorldType.DEADMAN), false, false, false, true)));
		assertFalse(AreaDetection.isWarningPvpArea(client(EnumSet.of(WorldType.DEADMAN), false, false, false, false)));
	}

	@Test
	public void deadmanWorldUsesWorldType()
	{
		assertTrue(AreaDetection.isDeadmanWorld(client(EnumSet.of(WorldType.DEADMAN), false, false, false, false)));
		assertFalse(AreaDetection.isDeadmanWorld(client(EnumSet.noneOf(WorldType.class), false, false, false, false)));
	}

	@Test
	public void misclickPvpAreaExcludesDeadmanGuardedZones()
	{
		assertTrue(AreaDetection.isMisclickPvpArea(client(EnumSet.of(WorldType.DEADMAN), false, false, false, true)));
		assertFalse(AreaDetection.isMisclickPvpArea(client(EnumSet.of(WorldType.DEADMAN), false, false, true, true)));
	}

	@Test
	public void pvpArenaMatchUsesBattleStatusVarbit()
	{
		assertTrue(AreaDetection.isPvpArenaMatch(client(EnumSet.noneOf(WorldType.class), false, true, false, false)));
		assertFalse(AreaDetection.isPvpArenaMatch(client(EnumSet.noneOf(WorldType.class), false, false, false, false)));
	}

	@Test
	public void pvpArenaWorldUsesWorldType()
	{
		assertTrue(AreaDetection.isPvpArenaWorld(client(EnumSet.of(WorldType.PVP_ARENA), false, false, false, false)));
		assertFalse(AreaDetection.isPvpArenaWorld(client(EnumSet.noneOf(WorldType.class), false, false, false, false)));
	}

	@Test
	public void deadmanGuardedZoneRequiresDeadmanWorldAndVisibleSafeWidget()
	{
		assertTrue(AreaDetection.isDeadmanGuardedZone(client(EnumSet.of(WorldType.DEADMAN), false, false, true, false)));
		assertFalse(AreaDetection.isDeadmanGuardedZone(client(EnumSet.of(WorldType.DEADMAN), false, false, false, false)));
		assertFalse(AreaDetection.isDeadmanGuardedZone(client(EnumSet.noneOf(WorldType.class), false, false, true, false)));
	}

	private static Client client(
		EnumSet<WorldType> worldTypes,
		boolean activeLmsGame,
		boolean pvpArenaMatch,
		boolean deadmanSafeWidgetVisible,
		boolean rawPvpArea)
	{
		return (Client) Proxy.newProxyInstance(
			Client.class.getClassLoader(),
			new Class<?>[] {Client.class},
			(proxy, method, args) ->
			{
				if ("getWorldType".equals(method.getName()))
				{
					return worldTypes;
				}

				if ("getVarbitValue".equals(method.getName()))
				{
					int varbitId = (int) args[0];
					if (varbitId == VarbitID.BR_INGAME)
					{
						return activeLmsGame ? 1 : 0;
					}

					if (varbitId == VarbitID.PVPA_BATTLEAREA_STATUS)
					{
						return pvpArenaMatch ? 1 : 0;
					}

					if (varbitId == VarbitID.INSIDE_WILDERNESS || varbitId == VarbitID.PVP_AREA_CLIENT)
					{
						return rawPvpArea ? 1 : 0;
					}

					return 0;
				}

				if ("getWidget".equals(method.getName()))
				{
					int widgetId = (int) args[0];
					if (widgetId == InterfaceID.PvpIcons.PVPW_SAFE)
					{
						return deadmanSafeWidgetVisible ? widget(false) : null;
					}

					return null;
				}

				if ("toString".equals(method.getName()))
				{
					return "client";
				}

				return null;
			});
	}

	private static Widget widget(boolean hidden)
	{
		return (Widget) Proxy.newProxyInstance(
			Widget.class.getClassLoader(),
			new Class<?>[] {Widget.class},
			(proxy, method, args) ->
			{
				if ("isHidden".equals(method.getName()))
				{
					return hidden;
				}

				if ("toString".equals(method.getName()))
				{
					return "widget";
				}

				return null;
			});
	}
}
