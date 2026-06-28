package com.wildyqol.scenery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.wildyqol.WildyQoLConfig;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.WorldType;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import org.junit.Test;

public class EmirsArenaSceneryServiceTest
{
	@Test
	public void pvpVarbitChangeDoesNotReloadSceneOutsidePvpArenaWorld() throws Exception
	{
		AtomicBoolean sceneReloaded = new AtomicBoolean(false);
		EmirsArenaSceneryService service = service(EnumSet.noneOf(WorldType.class), true, sceneReloaded);

		service.onVarbitChanged(varbitChanged(VarbitID.PVP_AREA_CLIENT, 1));

		assertFalse(sceneReloaded.get());
	}

	@Test
	public void pvpVarbitChangeReloadsSceneInPvpArenaWorld() throws Exception
	{
		AtomicBoolean sceneReloaded = new AtomicBoolean(false);
		EmirsArenaSceneryService service = service(EnumSet.of(WorldType.PVP_ARENA), true, sceneReloaded);

		service.onVarbitChanged(varbitChanged(VarbitID.PVP_AREA_CLIENT, 1));

		assertTrue(sceneReloaded.get());
	}

	private static EmirsArenaSceneryService service(
		EnumSet<WorldType> worldTypes,
		boolean rawPvpArea,
		AtomicBoolean sceneReloaded) throws Exception
	{
		EmirsArenaSceneryService service = new EmirsArenaSceneryService();
		setField(service, "client", client(worldTypes, rawPvpArea, sceneReloaded));
		setField(service, "clientThread", new ImmediateClientThread());
		setField(service, "config", config(true));
		setField(service, "removeObstructingScenery", true);
		setField(service, "inPvpArea", false);
		return service;
	}

	private static Client client(
		EnumSet<WorldType> worldTypes,
		boolean rawPvpArea,
		AtomicBoolean sceneReloaded)
	{
		return (Client) Proxy.newProxyInstance(
			Client.class.getClassLoader(),
			new Class<?>[] {Client.class},
			(proxy, method, args) ->
			{
				switch (method.getName())
				{
					case "getWorldType":
						return worldTypes;
					case "getVarbitValue":
						int varbitId = (int) args[0];
						return varbitId == VarbitID.INSIDE_WILDERNESS || varbitId == VarbitID.PVP_AREA_CLIENT
							? rawPvpArea ? 1 : 0
							: 0;
					case "getGameState":
						return GameState.LOGGED_IN;
					case "setGameState":
						sceneReloaded.set(args[0] == GameState.LOADING);
						return null;
					case "toString":
						return "client";
					default:
						return defaultValue(method.getReturnType());
				}
			});
	}

	private static WildyQoLConfig config(boolean removeObstructingScenery)
	{
		return (WildyQoLConfig) Proxy.newProxyInstance(
			WildyQoLConfig.class.getClassLoader(),
			new Class<?>[] {WildyQoLConfig.class},
			(proxy, method, args) ->
			{
				if ("removeObstructingPvpArenaScenery".equals(method.getName()))
				{
					return removeObstructingScenery;
				}

				if ("toString".equals(method.getName()))
				{
					return "config";
				}

				Object defaultValue = method.getDefaultValue();
				return defaultValue != null ? defaultValue : defaultValue(method.getReturnType());
			});
	}

	private static VarbitChanged varbitChanged(int varbitId, int value)
	{
		VarbitChanged event = new VarbitChanged();
		event.setVarbitId(varbitId);
		event.setValue(value);
		return event;
	}

	private static void setField(Object target, String name, Object value) throws Exception
	{
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private static Object defaultValue(Class<?> type)
	{
		if (!type.isPrimitive())
		{
			return null;
		}

		if (type == boolean.class)
		{
			return false;
		}

		if (type == void.class)
		{
			return null;
		}

		return 0;
	}

	private static class ImmediateClientThread extends ClientThread
	{
		@Override
		public void invoke(Runnable r)
		{
			r.run();
		}
	}
}
