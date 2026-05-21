package com.wildyqol.updates;

import com.wildyqol.WildyQoLConfig;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;

@Singleton
public class UpdateMessageService
{
	private static final String CONFIG_GROUP = "wildyqol";
	private static final String UPDATE_MESSAGE_KEY = "updateMessageShown140";

	private final ConfigManager configManager;
	private final WildyQoLConfig config;
	private final ChatMessageManager chatMessageManager;

	private boolean shouldShowUpdateMessage;

	@Inject
	private UpdateMessageService(ConfigManager configManager, WildyQoLConfig config, ChatMessageManager chatMessageManager)
	{
		this.configManager = configManager;
		this.config = config;
		this.chatMessageManager = chatMessageManager;
	}

	public void startUp()
	{
		shouldShowUpdateMessage = !config.updateMessageShown140();
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN || !shouldShowUpdateMessage)
		{
			return;
		}

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage("<col=00ff00>Wildy QoL 1.4.0:</col><br>- Added DMM overload proc timer<br>- Added warnings when banking for common PvP equipment problems (ammo, runes, spellbook, charges, teleport out). Please check the plugin settings if you want to customize this.<br>Found any issues or have a request? Message me on discord @sacca_1 or create an issue on the Wildy QoL github.")
			.build());
		configManager.setConfiguration(CONFIG_GROUP, UPDATE_MESSAGE_KEY, true);
		shouldShowUpdateMessage = false;
	}
}
