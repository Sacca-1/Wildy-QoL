package com.wildyqol.freezetimers;

import com.google.common.collect.ImmutableSet;
import com.wildyqol.WildyQoLConfig;
import com.wildyqol.WildyQoLPlugin;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;
import net.runelite.client.ui.overlay.infobox.Timer;
import net.runelite.client.util.RSTimeUnit;
import net.runelite.client.util.Text;

@Singleton
public class ExtendedFreezeTimersService
{
	private static final String CORE_TIMERS_GROUP = "timers";
	private static final String CORE_SHOW_FREEZES_KEY = "showFreezes";
	private static final int INACTIVITY_TIMEOUT_TICKS = 50;

	private static final Set<Integer> ANCIENT_SCEPTRES = buildAncientSceptres();
	private static final Set<Integer> SWAMPBARK_ITEMS = buildSwampbarkItems();
	private static final Map<Integer, FreezeType> GRAPHIC_TO_TYPE = buildGraphicMap();

	private final Client client;
	private final ConfigManager configManager;
	private final ChatMessageManager chatMessageManager;
	private final SpriteManager spriteManager;
	private final InfoBoxManager infoBoxManager;
	private final WildyQoLConfig config;

	private WildyQoLPlugin plugin;
	private Player currentOpponent;
	private String currentOpponentName;
	private int lastActivityTick = -1;
	private FreezeTimer activeTimer;
	private int freezeAppliedTick = -1;
	private int lastFrozenMessageTick = -1;
	private WorldPoint lastPoint;
	private Boolean previousTimersShowFreezes;
	private boolean toggledCoreTimers;
	private boolean warnedDuplicate;

	@Inject
	public ExtendedFreezeTimersService(
		Client client,
		ConfigManager configManager,
		ChatMessageManager chatMessageManager,
		SpriteManager spriteManager,
		InfoBoxManager infoBoxManager,
		WildyQoLConfig config)
	{
		this.client = client;
		this.configManager = configManager;
		this.chatMessageManager = chatMessageManager;
		this.spriteManager = spriteManager;
		this.infoBoxManager = infoBoxManager;
		this.config = config;
	}

	public void startUp(WildyQoLPlugin plugin)
	{
		this.plugin = plugin;
		warnedDuplicate = false;
		if (!isEnabled())
		{
			return;
		}

		disableCoreFreezeTimers();
	}

	public void shutDown()
	{
		removeActiveTimer();
		restoreCoreFreezeSetting();
		clearOpponent();
		plugin = null;
		warnedDuplicate = false;
	}

	public void onConfigChanged()
	{
		if (config.enableExtendedFreezeTimers())
		{
			warnedDuplicate = false;
			disableCoreFreezeTimers();
		}
		else
		{
			removeActiveTimer();
			restoreCoreFreezeSetting();
			clearOpponent();
		}
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		if (!isEnabled())
		{
			return;
		}

		GameState state = event.getGameState();
		if (state == GameState.LOGGING_IN || state == GameState.HOPPING || state == GameState.CONNECTION_LOST || state == GameState.LOGIN_SCREEN)
		{
			removeActiveTimer();
			clearOpponent();
		}
	}

	public void onInteractingChanged(InteractingChanged event)
	{
		if (!isEnabled())
		{
			return;
		}

		if (!(event.getSource() instanceof Player) || !(event.getTarget() instanceof Player))
		{
			return;
		}

		Player local = client.getLocalPlayer();
		if (local == null)
		{
			return;
		}

		Player candidate = null;
		if (event.getSource() == local)
		{
			candidate = (Player) event.getTarget();
		}
		else if (event.getTarget() == local)
		{
			candidate = (Player) event.getSource();
		}

		if (candidate == null)
		{
			return;
		}

		if (currentOpponent != null && Objects.equals(currentOpponentName, candidate.getName()))
		{
			refreshActivity();
			return;
		}

		setOpponent(candidate);
	}

	public void onAnimationChanged(AnimationChanged event)
	{
		if (!isEnabled())
		{
			return;
		}

		Actor actor = event.getActor();
		if (actor == null)
		{
			return;
		}

		if (actor == client.getLocalPlayer() || isOpponent(actor))
		{
			refreshActivity();
		}
	}

	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (!isEnabled())
		{
			return;
		}

		Actor actor = event.getActor();
		if (actor == null)
		{
			return;
		}

		if (actor == client.getLocalPlayer() || isOpponent(actor))
		{
			refreshActivity();
		}
	}

	public void onPlayerDespawned(PlayerDespawned event)
	{
		if (!isEnabled())
		{
			return;
		}

		if (isOpponent(event.getPlayer()))
		{
			clearOpponent();
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

		// For ancient freezes, only allow processing on the same tick as the freeze chat message.
		if (type.isAncient() && lastFrozenMessageTick != tickCount)
		{
			return;
		}

		refreshActivity();

		// Replace with shorter freeze if a downgrade is detected on the same tick.
		if (activeTimer != null && freezeAppliedTick == tickCount && type.getBaseTicks() < activeTimer.getFreezeType().getBaseTicks())
		{
			startTimer(type);
			return;
		}

		// Reapply if we got frozen again.
		if (activeTimer == null || type.getBaseTicks() >= activeTimer.getFreezeType().getBaseTicks() || freezeAppliedTick != tickCount)
		{
			startTimer(type);
		}
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
			clearOpponent();
			return;
		}

		if (activeTimer != null && freezeAppliedTick != client.getTickCount())
		{
			WorldPoint current = local.getWorldLocation();
			if (current != null && lastPoint != null && !current.equals(lastPoint))
			{
				removeActiveTimer();
			}
		}

		if (currentOpponent != null && lastActivityTick > -1 && client.getTickCount() - lastActivityTick > INACTIVITY_TIMEOUT_TICKS)
		{
			clearOpponent();
		}

		lastPoint = local.getWorldLocation();
	}

	public void onChatMessage(ChatMessage event)
	{
		if (!isEnabled())
		{
			return;
		}

		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		String message = Text.removeTags(event.getMessage());
		if ("You have been frozen!".equals(message))
		{
			lastFrozenMessageTick = client.getTickCount();
			startTimer(FreezeType.ICE_BARRAGE);
		}
	}

	private void startTimer(FreezeType type)
	{
		removeActiveTimer();

		Duration duration = calculateDuration(type);
		activeTimer = new FreezeTimer(type, duration, plugin);
		spriteManager.getSpriteAsync(type.getSpriteId(), 0, activeTimer);
		activeTimer.setTooltip(type.getDisplayName());
		infoBoxManager.addInfoBox(activeTimer);
		freezeAppliedTick = client.getTickCount();

		// If the core timer is still on, warn once.
		Boolean coreFreeze = configManager.getConfiguration(CORE_TIMERS_GROUP, CORE_SHOW_FREEZES_KEY, Boolean.class);
		if (coreFreeze == null || Boolean.TRUE.equals(coreFreeze))
		{
			warnDuplicateTimersIfNeeded();
		}
	}

	private Duration calculateDuration(FreezeType type)
	{
		long ticks = type.getBaseTicks();
		if (type.isAncient())
		{
			if (opponentHasAncientSceptre())
			{
				ticks = Math.round(ticks * 1.1d);
			}
		}
		else
		{
			ticks += countSwampbarkPieces();
		}

		ticks = Math.max(1L, ticks);
		return Duration.of(ticks, RSTimeUnit.GAME_TICKS);
	}

	private void removeActiveTimer()
	{
		if (activeTimer != null)
		{
			infoBoxManager.removeInfoBox(activeTimer);
			activeTimer = null;
		}

		freezeAppliedTick = -1;
	}

	private void setOpponent(Player opponent)
	{
		currentOpponent = opponent;
		currentOpponentName = opponent != null ? opponent.getName() : null;
		refreshActivity();
	}

	private void clearOpponent()
	{
		currentOpponent = null;
		currentOpponentName = null;
		lastActivityTick = -1;
	}

	private boolean isOpponent(Actor actor)
	{
		return actor instanceof Player && currentOpponent != null && currentOpponent == actor;
	}

	private void refreshActivity()
	{
		lastActivityTick = client.getTickCount();
	}

	private boolean opponentHasAncientSceptre()
	{
		Player opponent = currentOpponent;
		if (opponent == null)
		{
			return false;
		}

		for (int itemId : getEquipmentIds(opponent))
		{
			if (ANCIENT_SCEPTRES.contains(itemId))
			{
				return true;
			}
		}

		return false;
	}

	private int countSwampbarkPieces()
	{
		Player opponent = currentOpponent;
		if (opponent == null)
		{
			return 0;
		}

		int count = 0;
		for (int itemId : getEquipmentIds(opponent))
		{
			if (SWAMPBARK_ITEMS.contains(itemId))
			{
				count++;
			}
		}

		return count;
	}

	private int[] getEquipmentIds(Player player)
	{
		PlayerComposition comp = player.getPlayerComposition();
		if (comp == null || comp.getEquipmentIds() == null)
		{
			return new int[0];
		}

		int[] equipmentIds = comp.getEquipmentIds();
		int[] fixed = new int[equipmentIds.length];
		for (int i = 0; i < equipmentIds.length; i++)
		{
			int id = equipmentIds[i];
			fixed[i] = id > PlayerComposition.ITEM_OFFSET ? id - PlayerComposition.ITEM_OFFSET : id;
		}

		return fixed;
	}

	private boolean isEnabled()
	{
		return plugin != null && config.enableExtendedFreezeTimers();
	}

	private void disableCoreFreezeTimers()
	{
		if (toggledCoreTimers)
		{
			return;
		}

		previousTimersShowFreezes = configManager.getConfiguration(CORE_TIMERS_GROUP, CORE_SHOW_FREEZES_KEY, Boolean.class);
		toggledCoreTimers = true;
		configManager.setConfiguration(CORE_TIMERS_GROUP, CORE_SHOW_FREEZES_KEY, false);

		Boolean after = configManager.getConfiguration(CORE_TIMERS_GROUP, CORE_SHOW_FREEZES_KEY, Boolean.class);
		if (Boolean.TRUE.equals(after))
		{
			warnDuplicateTimersIfNeeded();
		}
	}

	private void restoreCoreFreezeSetting()
	{
		if (!toggledCoreTimers)
		{
			return;
		}

		Boolean current = configManager.getConfiguration(CORE_TIMERS_GROUP, CORE_SHOW_FREEZES_KEY, Boolean.class);
		// If the user changed the setting back while enabled, don't override it.
		if (Boolean.TRUE.equals(current))
		{
			toggledCoreTimers = false;
			previousTimersShowFreezes = null;
			return;
		}

		boolean restoreValue = previousTimersShowFreezes == null ? true : previousTimersShowFreezes;
		configManager.setConfiguration(CORE_TIMERS_GROUP, CORE_SHOW_FREEZES_KEY, restoreValue);
		toggledCoreTimers = false;
		previousTimersShowFreezes = null;
	}

	private void warnDuplicateTimersIfNeeded()
	{
		if (warnedDuplicate || !config.warnDuplicateFreezeTimers())
		{
			return;
		}

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage("<col=ff9040>Wildy QoL:</col> Extended freeze timers are active, but Timers & Buffs freeze timers are still enabled. Disable \"Freeze timer\" in Timers & Buffs to avoid duplicates.")
			.build());
		warnedDuplicate = true;
	}

	private static Set<Integer> buildAncientSceptres()
	{
		Set<Integer> ids = new HashSet<>();
		addVariations(ids, ItemID.ANCIENT_SCEPTRE);
		addVariations(ids, ItemID.ANCIENT_SCEPTRE_BLOOD);
		addVariations(ids, ItemID.ANCIENT_SCEPTRE_ICE);
		addVariations(ids, ItemID.ANCIENT_SCEPTRE_SMOKE);
		addVariations(ids, ItemID.ANCIENT_SCEPTRE_SHADOW);
		addVariations(ids, ItemID.ANCIENT_SCEPTRE_TROUVER);
		addVariations(ids, ItemID.ANCIENT_SCEPTRE_BLOOD_TROUVER);
		addVariations(ids, ItemID.ANCIENT_SCEPTRE_ICE_TROUVER);
		addVariations(ids, ItemID.ANCIENT_SCEPTRE_SMOKE_TROUVER);
		addVariations(ids, ItemID.ANCIENT_SCEPTRE_SHADOW_TROUVER);
		return ids;
	}

	private static Set<Integer> buildSwampbarkItems()
	{
		return ImmutableSet.of(
			ItemID.SWAMPBARK_HELM,
			ItemID.SWAMPBARK_BODY,
			ItemID.SWAMPBARK_LEGS
		);
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

	private static void addVariations(Set<Integer> ids, int itemId)
	{
		ids.add(itemId);
		ids.addAll(ItemVariationMapping.getVariations(itemId));
	}

	private enum FreezeType
	{
		BIND("Bind", SpriteID.Magicon2.BIND, SpotanimID.BIND_IMPACT, 8, false),
		SNARE("Snare", SpriteID.Magicon2.SNARE, SpotanimID.SNARE_IMPACT, 16, false),
		ENTANGLE("Entangle", SpriteID.Magicon2.ENTANGLE, SpotanimID.ENTANGLE_IMPACT, 24, false),
		ICE_RUSH("Ice rush", SpriteID.Magicon2.ICE_RUSH, SpotanimID.ICE_RUSH_IMPACT, 8, true),
		ICE_BURST("Ice burst", SpriteID.Magicon2.ICE_BURST, SpotanimID.ICE_BURST_IMPACT, 16, true),
		ICE_BLITZ("Ice blitz", SpriteID.Magicon2.ICE_BLITZ, SpotanimID.ICE_BLITZ_IMPACT, 24, true),
		ICE_BARRAGE("Ice barrage", SpriteID.Magicon2.ICE_BARRAGE, SpotanimID.ICE_BARRAGE_IMPACT, 32, true);

		private final String displayName;
		private final int spriteId;
		private final int graphicId;
		private final int baseTicks;
		private final boolean ancient;

		FreezeType(String displayName, int spriteId, int graphicId, int baseTicks, boolean ancient)
		{
			this.displayName = displayName;
			this.spriteId = spriteId;
			this.graphicId = graphicId;
			this.baseTicks = baseTicks;
			this.ancient = ancient;
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

		boolean isAncient()
		{
			return ancient;
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
