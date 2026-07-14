package com.wildyqol.prayer;

import com.wildyqol.AreaDetection;
import com.wildyqol.WildyQoLConfig;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ConfigProfile;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.prayer.PrayerPlugin;

@Singleton
public class PrayerLayoutService implements PrayerLayoutCoordinator.Backend
{
	private static final String PRAYER_CONFIG_GROUP = "prayer";
	private static final String ORDER_KEY_PREFIX = "prayer_order_book";
	private static final String HIDDEN_KEY_PREFIX = "prayer_hidden_book";
	private static final String PERSIST_CONFIG_KEY = "persistPrayerLayout";

	private final Client client;
	private final ClientThread clientThread;
	private final ConfigManager configManager;
	private final PluginManager pluginManager;
	private final ChatMessageManager chatMessageManager;
	private final WildyQoLConfig config;
	private final PrayerLayoutStore store;
	private final PrayerLayoutContextResolver contextResolver = new PrayerLayoutContextResolver();
	private final PrayerLayoutCoordinator coordinator;
	private final PrayerLayoutCaptureGate captureGate = new PrayerLayoutCaptureGate();
	private final AtomicBoolean captureScheduled = new AtomicBoolean();

	private volatile boolean applyingLayout;
	private boolean prayerPluginActive;

	@Inject
	private PrayerLayoutService(
		Client client,
		ClientThread clientThread,
		ConfigManager configManager,
		PluginManager pluginManager,
		ChatMessageManager chatMessageManager,
		WildyQoLConfig config,
		PrayerLayoutStore store)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.configManager = configManager;
		this.pluginManager = pluginManager;
		this.chatMessageManager = chatMessageManager;
		this.config = config;
		this.store = store;
		this.coordinator = new PrayerLayoutCoordinator(this);
	}

	public void startUp()
	{
		clientThread.invokeLater(() ->
		{
			captureGate.activate(getConfigProfileId());
			prayerPluginActive = isCorePrayerPluginActive();
			contextResolver.reset();
			coordinator.clear();
			reconcile(false);
		});
	}

	public void shutDown()
	{
		captureGate.invalidate();
		clientThread.invokeLater(() ->
		{
			if (config.persistPrayerReordering() && prayerPluginActive)
			{
				restoreAccountAndClear();
			}
			else
			{
				coordinator.clear();
			}
			store.flush();
			contextResolver.reset();
			prayerPluginActive = false;
		});
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			reconcile(false);
		}
		else if (config.persistPrayerReordering() && prayerPluginActive)
		{
			coordinator.leaveCurrent();
			store.flush();
			contextResolver.reset();
		}
	}

	public void onGameTick()
	{
		reconcile(true);
	}

	public void onVarbitChanged(VarbitChanged event)
	{
		int varbitId = event.getVarbitId();
		if (varbitId == VarbitID.BR_INGAME
			|| varbitId == VarbitID.PVPA_BATTLEAREA_STATUS
			|| varbitId == VarbitID.PVPA_TRANSMIT_BUILD)
		{
			reconcile(false);
		}
	}

	public void onRuneScapeProfileChanged()
	{
		reconcile(false);
	}

	public void onProfileChanged()
	{
		captureGate.invalidate();
		clientThread.invokeLater(() ->
		{
			coordinator.clear();
			contextResolver.reset();
			captureGate.activate(getConfigProfileId());
			prayerPluginActive = isCorePrayerPluginActive();
			reconcile(false);
		});
	}

	public void onPluginChanged(PluginChanged event)
	{
		if (!(event.getPlugin() instanceof PrayerPlugin))
		{
			return;
		}

		captureGate.invalidate();
		clientThread.invokeLater(() ->
		{
			if (!event.isLoaded() && config.persistPrayerReordering() && prayerPluginActive)
			{
				restoreAccountAndClear();
			}
			else
			{
				coordinator.clear();
			}

			prayerPluginActive = event.isLoaded();
			contextResolver.reset();
			if (prayerPluginActive)
			{
				captureGate.activate(getConfigProfileId());
				reconcile(false);
			}
		});
	}

	public void onConfigChanged(ConfigChanged event)
	{
		if (PrayerLayoutStore.CONFIG_GROUP.equals(event.getGroup())
			&& PERSIST_CONFIG_KEY.equals(event.getKey()))
		{
			captureGate.invalidate();
			clientThread.invokeLater(() ->
			{
				prayerPluginActive = isCorePrayerPluginActive();
				if (!config.persistPrayerReordering() && prayerPluginActive)
				{
					restoreAccountAndClear();
				}
				else
				{
					coordinator.clear();
				}
				contextResolver.reset();
				if (config.persistPrayerReordering())
				{
					captureGate.activate(getConfigProfileId());
					reconcile(false);
				}
			});
			return;
		}

		if (!PRAYER_CONFIG_GROUP.equals(event.getGroup())
			|| !PrayerLayoutSnapshot.isLayoutKey(event.getKey())
			|| applyingLayout
			|| !config.persistPrayerReordering()
			|| !prayerPluginActive)
		{
			return;
		}

		PrayerLayoutCaptureGate.Token captureToken = captureGate.token(getConfigProfileId());
		if (captureToken != null && captureScheduled.compareAndSet(false, true))
		{
			clientThread.invokeLater(() ->
			{
				captureScheduled.set(false);
				if (!applyingLayout
					&& config.persistPrayerReordering()
					&& prayerPluginActive
					&& captureGate.isCurrent(captureToken, getConfigProfileId()))
				{
					coordinator.saveCurrent();
				}
			});
		}
	}

	private long getConfigProfileId()
	{
		ConfigProfile profile = configManager.getProfile();
		return profile == null ? Long.MIN_VALUE : profile.getId();
	}

	private void reconcile(boolean allowLmsClassification)
	{
		if (!config.persistPrayerReordering()
			|| !prayerPluginActive
			|| client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		boolean activeLms = AreaDetection.isActiveLmsGame(client);
		PrayerLayoutContext context = contextResolver.resolve(
			configManager.getRSProfileKey(),
			getCharacterName(),
			activeLms,
			allowLmsClassification,
			activeLms ? client.getBoostedSkillLevel(Skill.DEFENCE) : 0,
			AreaDetection.isPvpArenaMatch(client),
			client.getVarbitValue(VarbitID.PVPA_TRANSMIT_BUILD));

		coordinator.transitionTo(context);
	}

	private String getCharacterName()
	{
		String profileName = configManager.getRSProfileConfiguration(
			ConfigManager.RSPROFILE_GROUP,
			ConfigManager.RSPROFILE_DISPLAY_NAME,
			String.class);
		if (profileName != null && !profileName.isEmpty())
		{
			return profileName;
		}

		Player localPlayer = client.getLocalPlayer();
		return localPlayer == null ? null : localPlayer.getName();
	}

	private void restoreAccountAndClear()
	{
		String profileKey = configManager.getRSProfileKey();
		if (profileKey == null)
		{
			coordinator.saveCurrent();
			coordinator.clear();
			return;
		}

		coordinator.transitionTo(PrayerLayoutContext.account(profileKey, getCharacterName()));
		coordinator.clear();
	}

	private boolean isCorePrayerPluginActive()
	{
		for (Plugin plugin : pluginManager.getPlugins())
		{
			if (plugin instanceof PrayerPlugin)
			{
				return pluginManager.isPluginActive(plugin);
			}
		}

		return false;
	}

	@Override
	public PrayerLayoutSnapshot capture()
	{
		Map<String, String> values = new TreeMap<>();
		captureKeys(ORDER_KEY_PREFIX, values);
		captureKeys(HIDDEN_KEY_PREFIX, values);
		return new PrayerLayoutSnapshot(values);
	}

	private void captureKeys(String prefix, Map<String, String> values)
	{
		for (String wholeKey : configManager.getConfigurationKeys(PRAYER_CONFIG_GROUP + "." + prefix))
		{
			int separator = wholeKey.indexOf('.');
			if (separator < 0 || !PRAYER_CONFIG_GROUP.equals(wholeKey.substring(0, separator)))
			{
				continue;
			}

			String key = wholeKey.substring(separator + 1);
			String value = configManager.getConfiguration(PRAYER_CONFIG_GROUP, key);
			if (PrayerLayoutSnapshot.isLayoutKey(key) && value != null)
			{
				values.put(key, value);
			}
		}
	}

	@Override
	public PrayerLayoutSnapshot load(PrayerLayoutContext context)
	{
		return store.load(context);
	}

	@Override
	public void save(PrayerLayoutContext context, PrayerLayoutSnapshot snapshot)
	{
		store.save(context, snapshot);
	}

	@Override
	public boolean apply(PrayerLayoutSnapshot target, PrayerLayoutSnapshot current)
	{
		if (!prayerPluginActive)
		{
			return false;
		}

		Map<String, String> targetValues = target.getValues();
		Map<String, String> currentValues = current.getValues();
		boolean changed = false;
		applyingLayout = true;
		try
		{
			for (String key : currentValues.keySet())
			{
				if (!targetValues.containsKey(key))
				{
					configManager.unsetConfiguration(PRAYER_CONFIG_GROUP, key);
					changed = true;
				}
			}

			for (Map.Entry<String, String> entry : targetValues.entrySet())
			{
				if (!entry.getValue().equals(currentValues.get(entry.getKey())))
				{
					configManager.setConfiguration(PRAYER_CONFIG_GROUP, entry.getKey(), entry.getValue());
					changed = true;
				}
			}
		}
		finally
		{
			applyingLayout = false;
		}

		if (changed)
		{
			redrawPrayers();
		}
		return changed;
	}

	private void redrawPrayers()
	{
		Widget prayerBook = client.getWidget(InterfaceID.PRAYERBOOK, 0);
		if (prayerBook != null && prayerBook.getOnVarTransmitListener() != null)
		{
			client.runScript(prayerBook.getOnVarTransmitListener());
		}
	}

	@Override
	public void restored(PrayerLayoutContext context)
	{
		if (!config.gameMessageOnPrayerReordering() || !prayerPluginActive)
		{
			return;
		}

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage(context.getRestoreMessage(config.hideRsnInPrayerLayoutMessage()))
			.build());
	}
}
