package com.wildyqol.warnings;

import java.util.Locale;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.util.Text;

@Singleton
public class BankProximityService
{
	private static final int BANK_PROXIMITY_DISTANCE = 10;

	private final Client client;

	@Inject
	BankProximityService(Client client)
	{
		this.client = client;
	}

	public boolean isBankVisible()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return false;
		}

		WorldPoint playerLocation = localPlayer.getWorldLocation();
		WorldView worldView = client.getTopLevelWorldView();
		if (worldView == null)
		{
			return false;
		}

		int plane = worldView.getPlane();
		if (hasVisibleBankObject(worldView.getScene(), plane, playerLocation))
		{
			return true;
		}

		for (NPC npc : client.getNpcs())
		{
			if (isWithinBankDistance(playerLocation, npc.getWorldLocation())
				&& isBankNpc(npc))
			{
				return true;
			}
		}

		return false;
	}

	private boolean hasVisibleBankObject(@Nullable Scene scene, int plane, WorldPoint playerLocation)
	{
		if (scene == null)
		{
			return false;
		}

		Tile[][][] tiles = scene.getTiles();
		if (tiles == null || plane < 0 || plane >= tiles.length || tiles[plane] == null)
		{
			return false;
		}

		for (Tile[] row : tiles[plane])
		{
			if (row == null)
			{
				continue;
			}

			for (Tile tile : row)
			{
				if (isBankTile(tile, playerLocation))
				{
					return true;
				}
			}
		}

		return false;
	}

	private boolean isBankTile(@Nullable Tile tile, WorldPoint playerLocation)
	{
		if (tile == null)
		{
			return false;
		}

		if (isBankObjectWithinDistance(tile.getWallObject(), playerLocation)
			|| isBankObjectWithinDistance(tile.getDecorativeObject(), playerLocation)
			|| isBankObjectWithinDistance(tile.getGroundObject(), playerLocation))
		{
			return true;
		}

		GameObject[] gameObjects = tile.getGameObjects();
		if (gameObjects == null)
		{
			return false;
		}

		for (GameObject gameObject : gameObjects)
		{
			if (isBankObjectWithinDistance(gameObject, playerLocation))
			{
				return true;
			}
		}

		return false;
	}

	private boolean isBankObjectWithinDistance(@Nullable TileObject object, WorldPoint playerLocation)
	{
		return object != null
			&& isWithinBankDistance(playerLocation, object.getWorldLocation())
			&& isBankObject(object);
	}

	private boolean isBankObject(@Nullable TileObject object)
	{
		if (object == null)
		{
			return false;
		}

		ObjectComposition composition = client.getObjectDefinition(object.getId());
		if (composition == null)
		{
			return false;
		}

		if (composition.getImpostorIds() != null)
		{
			composition = composition.getImpostor();
			if (composition == null)
			{
				return false;
			}
		}

		return hasBankObjectAction(composition);
	}

	private boolean isBankNpc(NPC npc)
	{
		NPCComposition composition = npc.getComposition();
		if (composition == null)
		{
			return false;
		}

		if (composition.getConfigs() != null)
		{
			composition = composition.transform();
			if (composition == null)
			{
				return false;
			}
		}

		return hasBankAction(composition.getActions());
	}

	static boolean isWithinBankDistance(WorldPoint playerLocation, WorldPoint bankLocation)
	{
		return playerLocation.getPlane() == bankLocation.getPlane()
			&& Math.max(
				Math.abs(playerLocation.getX() - bankLocation.getX()),
				Math.abs(playerLocation.getY() - bankLocation.getY())) <= BANK_PROXIMITY_DISTANCE;
	}

	static boolean hasBankAction(@Nullable String[] actions)
	{
		if (actions == null)
		{
			return false;
		}

		for (String action : actions)
		{
			if (action != null && isBankAction(Text.removeTags(action).trim()))
			{
				return true;
			}
		}

		return false;
	}

	static boolean hasBankObjectAction(ObjectComposition composition)
	{
		if (hasBankAction(composition.getActions()))
		{
			return true;
		}

		return isBankObjectName(composition.getName()) && hasUseAction(composition.getActions());
	}

	private static boolean isBankAction(String action)
	{
		return "Bank".equalsIgnoreCase(action);
	}

	private static boolean hasUseAction(@Nullable String[] actions)
	{
		if (actions == null)
		{
			return false;
		}

		for (String action : actions)
		{
			if (action != null && "Use".equalsIgnoreCase(Text.removeTags(action).trim()))
			{
				return true;
			}
		}

		return false;
	}

	private static boolean isBankObjectName(@Nullable String name)
	{
		return name != null && Text.removeTags(name).toLowerCase(Locale.ROOT).contains("bank");
	}
}
