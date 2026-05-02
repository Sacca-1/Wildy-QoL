package com.wildyqol;

import com.google.inject.Provides;
import com.wildyqol.freezetimers.ExtendedFreezeTimersService;
import com.wildyqol.itemskeptondeath.IkodParchmentRiskService;
import com.wildyqol.misclick.FishInventoryIconOverlay;
import com.wildyqol.misclick.MisclickPreventionService;
import com.wildyqol.proctimers.ProcTimerFeatureService;
import com.wildyqol.updates.UpdateMessageService;
import com.wildyqol.warnings.ProtectItemInfoBoxService;
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
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Wildy QoL",
	description = "Quality of life improvements for wilderness activities"
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
	private UpdateMessageService updateMessageService;

	@Inject
	private ProcTimerFeatureService procTimerFeatureService;

	@Inject
	private ExtendedFreezeTimersService extendedFreezeTimersService;

	@Override
	protected void startUp()
	{
		log.debug("Wildy QoL started");
		overlayManager.add(fishInventoryIconOverlay);
		ikodParchmentRiskService.startUp();
		clientThread.invokeLater(() -> ikodParchmentRiskService.refresh());
		protectItemInfoBoxService.startUp(this);
		procTimerFeatureService.startUp(this);
		extendedFreezeTimersService.startUp(this);
		updateMessageService.startUp();
	}

	@Override
	protected void shutDown()
	{
		log.debug("Wildy QoL stopped");
		overlayManager.remove(fishInventoryIconOverlay);
		ikodParchmentRiskService.shutDown();
		protectItemInfoBoxService.shutDown();
		procTimerFeatureService.shutDown();
		extendedFreezeTimersService.shutDown();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		extendedFreezeTimersService.onGameStateChanged(event);
		procTimerFeatureService.onGameStateChanged(event);
		updateMessageService.onGameStateChanged(event);
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
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		clientThread.invokeLater(() -> ikodParchmentRiskService.onWidgetLoaded(event));
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
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		ikodParchmentRiskService.onVarbitChanged(event);
		procTimerFeatureService.onVarbitChanged(event);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!"wildyqol".equals(event.getGroup()))
		{
			return;
		}

		if ("enableExtendedFreezeTimersV2".equals(event.getKey()))
		{
			extendedFreezeTimersService.onConfigChanged();
		}

		procTimerFeatureService.onConfigChanged(event);
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged event)
	{
		extendedFreezeTimersService.onGraphicChanged(event);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		extendedFreezeTimersService.onGameTick(event);
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
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		extendedFreezeTimersService.onChatMessage(event);
	}

	@Subscribe
	public void onPlayerDespawned(PlayerDespawned event)
	{
		extendedFreezeTimersService.onPlayerDespawned(event);
	}

	@Provides
	WildyQoLConfig provideConfig(ConfigManager configManager)
	{
		WildyQoLConfig cfg = configManager.getConfig(WildyQoLConfig.class);
		configManager.setDefaultConfiguration(cfg, false);
		return cfg;
	}
}
