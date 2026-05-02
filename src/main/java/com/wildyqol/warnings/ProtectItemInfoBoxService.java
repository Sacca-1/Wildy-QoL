package com.wildyqol.warnings;

import com.wildyqol.WildyQoLConfig;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

@Singleton
public class ProtectItemInfoBoxService
{
	private final Client client;
	private final WildyQoLConfig config;
	private final SpriteManager spriteManager;
	private final InfoBoxManager infoBoxManager;

	private ProtectItemInfoBox protectItemInfoBox;

	@Inject
	private ProtectItemInfoBoxService(
		Client client,
		WildyQoLConfig config,
		SpriteManager spriteManager,
		InfoBoxManager infoBoxManager)
	{
		this.client = client;
		this.config = config;
		this.spriteManager = spriteManager;
		this.infoBoxManager = infoBoxManager;
	}

	public void startUp(Plugin plugin)
	{
		protectItemInfoBox = new ProtectItemInfoBox(config, client, plugin, spriteManager);
		infoBoxManager.addInfoBox(protectItemInfoBox);
	}

	public void shutDown()
	{
		if (protectItemInfoBox != null)
		{
			infoBoxManager.removeInfoBox(protectItemInfoBox);
			protectItemInfoBox = null;
		}
	}
}
