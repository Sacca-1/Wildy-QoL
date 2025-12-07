package com.wildyqol.freezetimers;

import com.wildyqol.WildyQoLConfig;
import com.wildyqol.WildyQoLPlugin;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;
import net.runelite.client.ui.overlay.infobox.Timer;
import net.runelite.client.util.RSTimeUnit;

@Singleton
public class ExtendedFreezeTimersService
{
	private static final Map<Integer, FreezeType> GRAPHIC_TO_TYPE = buildGraphicMap();

	private final Client client;
	private final SpriteManager spriteManager;
	private final InfoBoxManager infoBoxManager;
	private final WildyQoLConfig config;

	private WildyQoLPlugin plugin;
	private FreezeTimer activeTimer;
	private int freezeAppliedTick = -1;
	private WorldPoint freezeLocation;

	@Inject
	public ExtendedFreezeTimersService(
		Client client,
		SpriteManager spriteManager,
		InfoBoxManager infoBoxManager,
		WildyQoLConfig config)
	{
		this.client = client;
		this.spriteManager = spriteManager;
		this.infoBoxManager = infoBoxManager;
		this.config = config;
	}

	public void startUp(WildyQoLPlugin plugin)
	{
		this.plugin = plugin;
	}

	public void shutDown()
	{
		removeActiveTimer();
		plugin = null;
	}

	public void onConfigChanged()
	{
		if (!isEnabled())
		{
			removeActiveTimer();
		}
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGGING_IN || state == GameState.HOPPING || state == GameState.CONNECTION_LOST || state == GameState.LOGIN_SCREEN)
		{
			removeActiveTimer();
		}
	}

	public void onGraphicChanged(GraphicChanged event)
	{
		if (!isEnabled())
		{
			return;
		}

		Actor actor = event.getActor();
		if (actor != client.getLocalPlayer())
		{
			return;
		}

		FreezeType type = GRAPHIC_TO_TYPE.get(actor.getGraphic());
		if (type == null)
		{
			return;
		}

		int tickCount = client.getTickCount();

		// Replace with shorter freeze if a downgrade is detected on the same tick.
		if (activeTimer != null && freezeAppliedTick == tickCount && type.getBaseTicks() < activeTimer.getFreezeType().getBaseTicks())
		{
			startTimer(type);
			return;
		}

		startTimer(type);
	}

	public void onGameTick(GameTick event)
	{
		if (!isEnabled())
		{
			return;
		}

		Player local = client.getLocalPlayer();
		if (local == null)
		{
			removeActiveTimer();
			return;
		}

		if (activeTimer != null && freezeLocation != null && freezeAppliedTick != client.getTickCount())
		{
			WorldPoint current = local.getWorldLocation();
			if (current != null && !current.equals(freezeLocation))
			{
				removeActiveTimer();
			}
		}
	}

	private void startTimer(FreezeType type)
	{
		removeActiveTimer();

		Duration duration = Duration.of(type.getBaseTicks(), RSTimeUnit.GAME_TICKS);
		activeTimer = new FreezeTimer(type, duration, plugin);
		spriteManager.getSpriteAsync(type.getSpriteId(), 0, activeTimer);
		activeTimer.setTooltip(type.getDisplayName());
		infoBoxManager.addInfoBox(activeTimer);
		freezeAppliedTick = client.getTickCount();
		freezeLocation = client.getLocalPlayer() != null ? client.getLocalPlayer().getWorldLocation() : null;
	}

	private void removeActiveTimer()
	{
		if (activeTimer != null)
		{
			infoBoxManager.removeInfoBox(activeTimer);
			activeTimer = null;
		}

		freezeLocation = null;
		freezeAppliedTick = -1;
	}

	private boolean isEnabled()
	{
		return plugin != null && config.enableExtendedFreezeTimers();
	}

	private static Map<Integer, FreezeType> buildGraphicMap()
	{
		Map<Integer, FreezeType> map = new HashMap<>();
		for (FreezeType type : FreezeType.values())
		{
			map.put(type.getGraphicId(), type);
		}
		return map;
	}

	private enum FreezeType
	{
		BIND("Bind", SpriteID.Magicon2.BIND, SpotanimID.BIND_IMPACT, 8),
		SNARE("Snare", SpriteID.Magicon2.SNARE, SpotanimID.SNARE_IMPACT, 16),
		ENTANGLE("Entangle", SpriteID.Magicon2.ENTANGLE, SpotanimID.ENTANGLE_IMPACT, 24),
		ICE_RUSH("Ice rush", SpriteID.Magicon2.ICE_RUSH, SpotanimID.ICE_RUSH_IMPACT, 8),
		ICE_BURST("Ice burst", SpriteID.Magicon2.ICE_BURST, SpotanimID.ICE_BLITZ_IMPACT, 16),
		ICE_BLITZ("Ice blitz", SpriteID.Magicon2.ICE_BLITZ, SpotanimID.ICE_BURST_IMPACT, 24),
		ICE_BARRAGE("Ice barrage", SpriteID.Magicon2.ICE_BARRAGE, SpotanimID.ICE_BARRAGE_IMPACT, 32);

		private final String displayName;
		private final int spriteId;
		private final int graphicId;
		private final int baseTicks;

		FreezeType(String displayName, int spriteId, int graphicId, int baseTicks)
		{
			this.displayName = displayName;
			this.spriteId = spriteId;
			this.graphicId = graphicId;
			this.baseTicks = baseTicks;
		}

		int getSpriteId()
		{
			return spriteId;
		}

		int getGraphicId()
		{
			return graphicId;
		}

		int getBaseTicks()
		{
			return baseTicks;
		}

		String getDisplayName()
		{
			return displayName;
		}
	}

	private static class FreezeTimer extends Timer
	{
		private final FreezeType freezeType;

		FreezeTimer(FreezeType freezeType, Duration duration, WildyQoLPlugin plugin)
		{
			super(duration.toMillis(), ChronoUnit.MILLIS, null, plugin);
			this.freezeType = freezeType;
			setPriority(InfoBoxPriority.MED);
		}

		FreezeType getFreezeType()
		{
			return freezeType;
		}

		@Override
		public String getName()
		{
			return freezeType.getDisplayName();
		}
	}
}
