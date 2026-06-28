package com.wildyqol.scenery;

import com.wildyqol.WildyQoLConfig;
import com.wildyqol.AreaDetection;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.RenderCallback;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.events.ConfigChanged;

@Singleton
public class EmirsArenaSceneryService
{
	private static final int MARKER_REGION_BASE_X = 3328; // Region 13384, x region 52.
	private static final int MARKER_REGION_BASE_Y = 4608; // Region 13384, y region 72.
	private static final TileRectangle OUTER_HIDE_RECT = new TileRectangle(17, 28, 50, 49);
	private static final TileRectangle INNER_KEEP_RECT = new TileRectangle(23, 31, 44, 45);
	private static final Set<TileRectangle> EXCLUDE_RECTS = Set.of(
		new TileRectangle(22, 37, 23, 39), // W gate
		new TileRectangle(44, 37, 45, 39)  // E gate
	);

	private final RenderCallback renderCallback = new RenderCallback()
	{
		@Override
		public boolean drawObject(Scene scene, TileObject object)
		{
			return true;
		}

		@Override
		public boolean drawTile(Scene scene, Tile tile)
		{
			return shouldDrawTile(scene, tile);
		}
	};

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private RenderCallbackManager renderCallbackManager;

	@Inject
	private WildyQoLConfig config;

	private volatile boolean removeObstructingScenery;
	private volatile boolean inPvpArea;

	public void startUp()
	{
		updateConfig();
		renderCallbackManager.register(renderCallback);
		clientThread.invoke(() -> inPvpArea = isInPvpArea());
	}

	public void shutDown()
	{
		renderCallbackManager.unregister(renderCallback);
	}

	public void onConfigChanged(ConfigChanged event)
	{
		if (!"wildyqol".equals(event.getGroup()) || !"removeObstructingPvpArenaScenery".equals(event.getKey()))
		{
			return;
		}

		reloadSceneWithCurrentFightState();
	}

	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarbitId() != VarbitID.INSIDE_WILDERNESS && event.getVarbitId() != VarbitID.PVP_AREA_CLIENT)
		{
			return;
		}

		boolean nowInPvpArea = isInPvpArea();
		if (inPvpArea == nowInPvpArea)
		{
			return;
		}

		inPvpArea = nowInPvpArea;
		if (shouldRemoveArenaScenery())
		{
			reloadScene();
		}
	}

	private boolean shouldDrawTile(Scene scene, Tile tile)
	{
		if (!shouldRemoveArenaScenery() || !inPvpArea || tile == null || tile.getLocalLocation() == null || !isRemovedWallTile(scene, tile))
		{
			return true;
		}

		scene.removeTile(tile);
		return false;
	}

	private boolean isRemovedWallTile(Scene scene, Tile tile)
	{
		try
		{
			WorldPoint templateLocation = WorldPoint.fromLocalInstance(scene, tile.getLocalLocation(), tile.getPlane());
			return templateLocation != null && isArenaWallTile(templateLocation.getX(), templateLocation.getY());
		}
		catch (RuntimeException ignored)
		{
			return false;
		}
	}

	private boolean isArenaWallTile(int worldX, int worldY)
	{
		int regionX = worldX - MARKER_REGION_BASE_X;
		int regionY = worldY - MARKER_REGION_BASE_Y;
		if (isInAnyRect(EXCLUDE_RECTS, regionX, regionY))
		{
			return false;
		}

		return OUTER_HIDE_RECT.contains(regionX, regionY) && !INNER_KEEP_RECT.strictlyContains(regionX, regionY);
	}

	private boolean isInAnyRect(Set<TileRectangle> rects, int regionX, int regionY)
	{
		for (TileRectangle rect : rects)
		{
			if (rect.contains(regionX, regionY))
			{
				return true;
			}
		}

		return false;
	}

	private void reloadScene()
	{
		clientThread.invoke(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				client.setGameState(GameState.LOADING);
			}
		});
	}

	private void reloadSceneWithCurrentFightState()
	{
		updateConfig();
		clientThread.invoke(() ->
		{
			inPvpArea = isInPvpArea();
			if (shouldRemoveArenaScenery() && client.getGameState() == GameState.LOGGED_IN)
			{
				client.setGameState(GameState.LOADING);
			}
		});
	}

	private void updateConfig()
	{
		removeObstructingScenery = config.removeObstructingPvpArenaScenery();
	}

	private boolean isInPvpArea()
	{
		return AreaDetection.isRawPvpArea(client);
	}

	private boolean shouldRemoveArenaScenery()
	{
		return removeObstructingScenery && AreaDetection.isPvpArenaWorld(client);
	}

	private static final class TileRectangle
	{
		private final int minX;
		private final int minY;
		private final int maxX;
		private final int maxY;

		private TileRectangle(int x1, int y1, int x2, int y2)
		{
			minX = Math.min(x1, x2);
			minY = Math.min(y1, y2);
			maxX = Math.max(x1, x2);
			maxY = Math.max(y1, y2);
		}

		private boolean contains(int x, int y)
		{
			return x >= minX && x <= maxX && y >= minY && y <= maxY;
		}

		private boolean strictlyContains(int x, int y)
		{
			return x > minX && x < maxX && y > minY && y < maxY;
		}
	}
}
