package com.wildyqol;

import com.google.inject.Provides;
import com.wildyqol.freezetimers.ExtendedFreezeTimersService;
import com.wildyqol.itemskeptondeath.IkodParchmentRiskService;
import com.wildyqol.misclick.FishInventoryIconOverlay;
import com.wildyqol.misclick.MisclickPreventionService;
import com.wildyqol.proctimers.ProcTimerFeatureService;
import com.wildyqol.prayer.PrayerLayoutService;
import com.wildyqol.pvparena.PvpArenaSpellbookWarningOverlay;
import com.wildyqol.scenery.EmirsArenaSceneryService;
import com.wildyqol.warnings.ProtectItemInfoBoxService;
import com.wildyqol.warnings.WarningDefaultsMigrationService;
import com.wildyqol.warnings.WarningOverlay;
import com.wildyqol.warnings.WarningServiceManager;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Wildy QoL",
	description = "PvP and Wilderness tools for preventing misclicks, checking your setup, improving timers, saving prayer layouts, and more."
)
public class WildyQoLPlugin extends Plugin
{
	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private FishInventoryIconOverlay fishInventoryIconOverlay;

	@Inject
	private MisclickPreventionService misclickPreventionService;

	@Inject
	private IkodParchmentRiskService ikodParchmentRiskService;

	@Inject
	private ProtectItemInfoBoxService protectItemInfoBoxService;

	@Inject
	private ProcTimerFeatureService procTimerFeatureService;

	@Inject
	private ExtendedFreezeTimersService extendedFreezeTimersService;

	@Inject
	private WarningServiceManager warningServiceManager;

	@Inject
	private WarningDefaultsMigrationService warningDefaultsMigrationService;

	@Inject
	private WarningOverlay warningOverlay;

	@Inject
	private EmirsArenaSceneryService emirsArenaSceneryService;

	@Inject
	private PvpArenaSpellbookWarningOverlay pvpArenaSpellbookWarningOverlay;

	@Inject
	private PrayerLayoutService prayerLayoutService;

	@Override
	protected void startUp()
	{
		log.debug("Wildy QoL started");
		overlayManager.add(fishInventoryIconOverlay);
		overlayManager.add(warningOverlay);
		overlayManager.add(pvpArenaSpellbookWarningOverlay);
		warningDefaultsMigrationService.migrate();
		ikodParchmentRiskService.startUp();
		clientThread.invokeLater(() -> ikodParchmentRiskService.refresh());
		protectItemInfoBoxService.startUp(this);
		warningServiceManager.startUp();
		procTimerFeatureService.startUp(this);
		extendedFreezeTimersService.startUp(this);
		emirsArenaSceneryService.startUp();
		prayerLayoutService.startUp();
	}

	@Override
	protected void shutDown()
	{
		log.debug("Wildy QoL stopped");
		overlayManager.remove(fishInventoryIconOverlay);
		overlayManager.remove(warningOverlay);
		overlayManager.remove(pvpArenaSpellbookWarningOverlay);
		ikodParchmentRiskService.shutDown();
		protectItemInfoBoxService.shutDown();
		warningServiceManager.shutDown();
		procTimerFeatureService.shutDown();
		extendedFreezeTimersService.shutDown();
		emirsArenaSceneryService.shutDown();
		prayerLayoutService.shutDown();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		extendedFreezeTimersService.onGameStateChanged(event);
		procTimerFeatureService.onGameStateChanged(event);
		prayerLayoutService.onGameStateChanged(event);
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		misclickPreventionService.onMenuEntryAdded(event);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		misclickPreventionService.onMenuOptionClicked(event);
		warningServiceManager.onMenuOptionClicked(event);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		clientThread.invokeLater(() -> ikodParchmentRiskService.onWidgetLoaded(event));
		warningServiceManager.onWidgetLoaded(event);
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		ikodParchmentRiskService.onWidgetClosed(event);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		ikodParchmentRiskService.onItemContainerChanged(event);
		extendedFreezeTimersService.onItemContainerChanged(event);
		warningServiceManager.onItemContainerChanged(event);
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		ikodParchmentRiskService.onVarbitChanged(event);
		procTimerFeatureService.onVarbitChanged(event);
		emirsArenaSceneryService.onVarbitChanged(event);
		prayerLayoutService.onVarbitChanged(event);
		warningServiceManager.onVarbitChanged(event);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		prayerLayoutService.onConfigChanged(event);

		if (!"wildyqol".equals(event.getGroup()))
		{
			return;
		}

		if ("enableExtendedFreezeTimersV2".equals(event.getKey()))
		{
			extendedFreezeTimersService.onConfigChanged();
		}

		procTimerFeatureService.onConfigChanged(event);
		emirsArenaSceneryService.onConfigChanged(event);
		warningServiceManager.refreshOnClientThread();
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged event)
	{
		extendedFreezeTimersService.onGraphicChanged(event);
		warningServiceManager.onGraphicChanged(event);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		prayerLayoutService.onGameTick();
		ikodParchmentRiskService.onGameTick(event);
		extendedFreezeTimersService.onGameTick(event);
		warningServiceManager.onGameTick(event);
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		extendedFreezeTimersService.onInteractingChanged(event);
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		extendedFreezeTimersService.onAnimationChanged(event);
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		extendedFreezeTimersService.onHitsplatApplied(event);
		warningServiceManager.onHitsplatApplied(event);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		extendedFreezeTimersService.onChatMessage(event);
		warningServiceManager.onChatMessage(event);
	}

	@Subscribe
	public void onPlayerDespawned(PlayerDespawned event)
	{
		extendedFreezeTimersService.onPlayerDespawned(event);
	}

	@Subscribe
	public void onPluginChanged(PluginChanged event)
	{
		prayerLayoutService.onPluginChanged(event);
	}

	@Subscribe
	public void onProfileChanged(ProfileChanged event)
	{
		warningDefaultsMigrationService.migrate();
		prayerLayoutService.onProfileChanged();
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		prayerLayoutService.onRuneScapeProfileChanged();
		warningServiceManager.onRuneScapeProfileChanged();
	}

	@Provides
	WildyQoLConfig provideConfig(ConfigManager configManager)
	{
		WildyQoLConfig cfg = configManager.getConfig(WildyQoLConfig.class);
		configManager.setDefaultConfiguration(cfg, false);
		return cfg;
	}
}
