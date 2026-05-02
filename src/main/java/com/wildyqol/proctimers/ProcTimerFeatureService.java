package com.wildyqol.proctimers;

import com.wildyqol.WildyQoLConfig;
import com.wildyqol.proctimers.dmmoverload.DmmOverloadProcInfoBox;
import com.wildyqol.proctimers.dmmoverload.DmmOverloadProcStatusBarOverlay;
import com.wildyqol.proctimers.dmmoverload.DmmOverloadProcTimerService;
import com.wildyqol.proctimers.menaphite.MenaphiteProcInfoBox;
import com.wildyqol.proctimers.menaphite.MenaphiteProcStatusBarOverlay;
import com.wildyqol.proctimers.menaphite.MenaphiteProcTimerService;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;

@Singleton
public class ProcTimerFeatureService
{
	private final Client client;
	private final ClientThread clientThread;
	private final ItemManager itemManager;
	private final InfoBoxManager infoBoxManager;
	private final OverlayManager overlayManager;
	private final WildyQoLConfig config;
	private final MenaphiteProcTimerService menaphiteTimerService;
	private final DmmOverloadProcTimerService dmmOverloadTimerService;
	private final MenaphiteProcStatusBarOverlay menaphiteStatusBarOverlay;
	private final DmmOverloadProcStatusBarOverlay dmmOverloadStatusBarOverlay;

	private Plugin plugin;
	private MenaphiteProcInfoBox menaphiteInfoBox;
	private BufferedImage menaphiteImage;
	private boolean menaphiteStatusBarOverlayAdded;
	private DmmOverloadProcInfoBox dmmOverloadInfoBox;
	private BufferedImage dmmOverloadImage;
	private boolean dmmOverloadStatusBarOverlayAdded;

	@Inject
	private ProcTimerFeatureService(
		Client client,
		ClientThread clientThread,
		ItemManager itemManager,
		InfoBoxManager infoBoxManager,
		OverlayManager overlayManager,
		WildyQoLConfig config,
		MenaphiteProcTimerService menaphiteTimerService,
		DmmOverloadProcTimerService dmmOverloadTimerService,
		MenaphiteProcStatusBarOverlay menaphiteStatusBarOverlay,
		DmmOverloadProcStatusBarOverlay dmmOverloadStatusBarOverlay)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.itemManager = itemManager;
		this.infoBoxManager = infoBoxManager;
		this.overlayManager = overlayManager;
		this.config = config;
		this.menaphiteTimerService = menaphiteTimerService;
		this.dmmOverloadTimerService = dmmOverloadTimerService;
		this.menaphiteStatusBarOverlay = menaphiteStatusBarOverlay;
		this.dmmOverloadStatusBarOverlay = dmmOverloadStatusBarOverlay;
	}

	public void startUp(Plugin plugin)
	{
		this.plugin = plugin;
		resetMenaphite();
		menaphiteImage = loadImage(ItemID._4DOSESTATRENEWAL);
		menaphiteStatusBarOverlay.setMenaphiteImage(menaphiteImage);
		updateMenaphiteStatusBarOverlay();

		resetDmmOverload();
		dmmOverloadImage = loadImage(ItemID.DEADMAN4DOSEOVERLOAD);
		dmmOverloadStatusBarOverlay.setOverloadImage(dmmOverloadImage);
		updateDmmOverloadStatusBarOverlay();
	}

	public void shutDown()
	{
		resetMenaphite();
		menaphiteStatusBarOverlay.clearMenaphiteImage();
		menaphiteImage = null;
		resetDmmOverload();
		dmmOverloadStatusBarOverlay.clearOverloadImage();
		dmmOverloadImage = null;
		plugin = null;
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOADING || state == GameState.LOGGED_IN)
		{
			return;
		}

		resetMenaphite();
		resetDmmOverload();
	}

	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarbitId() == VarbitID.STATRENEWAL_POTION_TIMER)
		{
			handleMenaphiteVarbit(event.getValue());
		}

		if (event.getVarbitId() == VarbitID.DEADMAN_OVERLOAD_POTION_EFFECTS)
		{
			handleDmmOverloadVarbit(event.getValue());
		}
	}

	public void onConfigChanged(ConfigChanged event)
	{
		if ("menaphiteProcTimerShowInfoBox".equals(event.getKey()))
		{
			if (!config.menaphiteProcTimerShowInfoBox())
			{
				removeMenaphiteInfoBox();
			}
			else if (menaphiteTimerService.isActive())
			{
				ensureMenaphiteInfoBox();
			}

			invokeMenaphiteRefresh();
		}

		if ("menaphiteProcTimerStatusBarMode".equals(event.getKey()))
		{
			invokeMenaphiteRefresh();
		}

		if ("dmmOverloadProcTimerShowInfoBox".equals(event.getKey()))
		{
			if (!config.dmmOverloadProcTimerShowInfoBox())
			{
				removeDmmOverloadInfoBox();
			}
			else if (dmmOverloadTimerService.isActive())
			{
				ensureDmmOverloadInfoBox();
			}

			invokeDmmOverloadRefresh();
		}

		if ("dmmOverloadProcTimerStatusBarMode".equals(event.getKey()))
		{
			invokeDmmOverloadRefresh();
		}
	}

	private void invokeMenaphiteRefresh()
	{
		clientThread.invokeLater(() ->
		{
			int varbitValue = client.getVarbitValue(VarbitID.STATRENEWAL_POTION_TIMER);
			handleMenaphiteVarbit(varbitValue);
		});
	}

	private void invokeDmmOverloadRefresh()
	{
		clientThread.invokeLater(() ->
		{
			int varbitValue = client.getVarbitValue(VarbitID.DEADMAN_OVERLOAD_POTION_EFFECTS);
			handleDmmOverloadVarbit(varbitValue);
		});
	}

	private void handleMenaphiteVarbit(int varbitValue)
	{
		if (!isMenaphiteTimerEnabled())
		{
			resetMenaphite();
			return;
		}

		menaphiteTimerService.handleVarbitUpdate(varbitValue, client.getTickCount());
		if (menaphiteTimerService.isActive() && config.menaphiteProcTimerShowInfoBox())
		{
			ensureMenaphiteInfoBox();
		}
		else
		{
			removeMenaphiteInfoBox();
		}

		updateMenaphiteStatusBarOverlay();
	}

	private void handleDmmOverloadVarbit(int varbitValue)
	{
		if (!isDmmOverloadTimerEnabled())
		{
			resetDmmOverload();
			return;
		}

		dmmOverloadTimerService.handleVarbitUpdate(varbitValue, client.getTickCount());
		if (dmmOverloadTimerService.isActive() && config.dmmOverloadProcTimerShowInfoBox())
		{
			ensureDmmOverloadInfoBox();
		}
		else
		{
			removeDmmOverloadInfoBox();
		}

		updateDmmOverloadStatusBarOverlay();
	}

	private void resetMenaphite()
	{
		menaphiteTimerService.reset();
		removeMenaphiteInfoBox();
		removeMenaphiteStatusBarOverlay();
	}

	private void resetDmmOverload()
	{
		dmmOverloadTimerService.reset();
		removeDmmOverloadInfoBox();
		removeDmmOverloadStatusBarOverlay();
	}

	private void ensureMenaphiteInfoBox()
	{
		if (!config.menaphiteProcTimerShowInfoBox())
		{
			removeMenaphiteInfoBox();
			return;
		}

		if (menaphiteInfoBox != null)
		{
			return;
		}

		if (menaphiteImage == null)
		{
			menaphiteImage = loadImage(ItemID._4DOSESTATRENEWAL);
		}

		menaphiteInfoBox = new MenaphiteProcInfoBox(menaphiteImage, plugin, menaphiteTimerService, config, client);
		menaphiteInfoBox.setTooltip("Time until next menaphite remedy proc");
		menaphiteInfoBox.setPriority(InfoBoxPriority.MED);
		infoBoxManager.addInfoBox(menaphiteInfoBox);
		menaphiteStatusBarOverlay.setMenaphiteImage(menaphiteImage);
	}

	private void ensureDmmOverloadInfoBox()
	{
		if (!config.dmmOverloadProcTimerShowInfoBox())
		{
			removeDmmOverloadInfoBox();
			return;
		}

		if (dmmOverloadInfoBox != null)
		{
			return;
		}

		if (dmmOverloadImage == null)
		{
			dmmOverloadImage = loadImage(ItemID.DEADMAN4DOSEOVERLOAD);
		}

		dmmOverloadInfoBox = new DmmOverloadProcInfoBox(dmmOverloadImage, plugin, dmmOverloadTimerService, config, client);
		dmmOverloadInfoBox.setTooltip("Time until next DMM overload proc");
		dmmOverloadInfoBox.setPriority(InfoBoxPriority.MED);
		infoBoxManager.addInfoBox(dmmOverloadInfoBox);
		dmmOverloadStatusBarOverlay.setOverloadImage(dmmOverloadImage);
	}

	private void removeMenaphiteInfoBox()
	{
		if (menaphiteInfoBox != null)
		{
			infoBoxManager.removeInfoBox(menaphiteInfoBox);
			menaphiteInfoBox = null;
		}
	}

	private void removeDmmOverloadInfoBox()
	{
		if (dmmOverloadInfoBox != null)
		{
			infoBoxManager.removeInfoBox(dmmOverloadInfoBox);
			dmmOverloadInfoBox = null;
		}
	}

	private BufferedImage loadImage(int itemId)
	{
		BufferedImage image = itemManager.getImage(itemId);
		if (image != null)
		{
			return image;
		}

		return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	}

	private void updateMenaphiteStatusBarOverlay()
	{
		WildyQoLConfig.MenaphiteProcStatusBarMode mode = config.menaphiteProcTimerStatusBarMode();
		if (mode == null || mode == WildyQoLConfig.MenaphiteProcStatusBarMode.OFF)
		{
			removeMenaphiteStatusBarOverlay();
			return;
		}

		MenaphiteProcStatusBarOverlay.MenaphiteStatusBarPosition position =
			mode == WildyQoLConfig.MenaphiteProcStatusBarMode.LEFT
				? MenaphiteProcStatusBarOverlay.MenaphiteStatusBarPosition.LEFT
				: MenaphiteProcStatusBarOverlay.MenaphiteStatusBarPosition.RIGHT;
		menaphiteStatusBarOverlay.setPosition(position);
		ensureMenaphiteStatusBarOverlay();
		if (menaphiteImage == null)
		{
			menaphiteImage = loadImage(ItemID._4DOSESTATRENEWAL);
		}
		menaphiteStatusBarOverlay.setMenaphiteImage(menaphiteImage);
	}

	private void updateDmmOverloadStatusBarOverlay()
	{
		WildyQoLConfig.DmmOverloadProcStatusBarMode mode = config.dmmOverloadProcTimerStatusBarMode();
		if (mode == null || mode == WildyQoLConfig.DmmOverloadProcStatusBarMode.OFF)
		{
			removeDmmOverloadStatusBarOverlay();
			return;
		}

		DmmOverloadProcStatusBarOverlay.DmmOverloadStatusBarPosition position =
			mode == WildyQoLConfig.DmmOverloadProcStatusBarMode.LEFT
				? DmmOverloadProcStatusBarOverlay.DmmOverloadStatusBarPosition.LEFT
				: DmmOverloadProcStatusBarOverlay.DmmOverloadStatusBarPosition.RIGHT;
		dmmOverloadStatusBarOverlay.setPosition(position);
		ensureDmmOverloadStatusBarOverlay();
		if (dmmOverloadImage == null)
		{
			dmmOverloadImage = loadImage(ItemID.DEADMAN4DOSEOVERLOAD);
		}
		dmmOverloadStatusBarOverlay.setOverloadImage(dmmOverloadImage);
	}

	private void ensureMenaphiteStatusBarOverlay()
	{
		if (!menaphiteStatusBarOverlayAdded)
		{
			overlayManager.add(menaphiteStatusBarOverlay);
			menaphiteStatusBarOverlayAdded = true;
		}
	}

	private void ensureDmmOverloadStatusBarOverlay()
	{
		if (!dmmOverloadStatusBarOverlayAdded)
		{
			overlayManager.add(dmmOverloadStatusBarOverlay);
			dmmOverloadStatusBarOverlayAdded = true;
		}
	}

	private void removeMenaphiteStatusBarOverlay()
	{
		if (menaphiteStatusBarOverlayAdded)
		{
			overlayManager.remove(menaphiteStatusBarOverlay);
			menaphiteStatusBarOverlayAdded = false;
		}
	}

	private void removeDmmOverloadStatusBarOverlay()
	{
		if (dmmOverloadStatusBarOverlayAdded)
		{
			overlayManager.remove(dmmOverloadStatusBarOverlay);
			dmmOverloadStatusBarOverlayAdded = false;
		}
	}

	private boolean isMenaphiteTimerEnabled()
	{
		WildyQoLConfig.MenaphiteProcStatusBarMode mode = config.menaphiteProcTimerStatusBarMode();
		boolean statusBarEnabled = mode != null && mode != WildyQoLConfig.MenaphiteProcStatusBarMode.OFF;
		return config.menaphiteProcTimerShowInfoBox() || statusBarEnabled;
	}

	private boolean isDmmOverloadTimerEnabled()
	{
		WildyQoLConfig.DmmOverloadProcStatusBarMode mode = config.dmmOverloadProcTimerStatusBarMode();
		boolean statusBarEnabled = mode != null && mode != WildyQoLConfig.DmmOverloadProcStatusBarMode.OFF;
		return config.dmmOverloadProcTimerShowInfoBox() || statusBarEnabled;
	}
}
