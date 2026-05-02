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
	private static final String UPDATE_MESSAGE_KEY = "updateMessageShown130";

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
		shouldShowUpdateMessage = !config.updateMessageShown130();
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN || !shouldShowUpdateMessage)
		{
			return;
		}

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage("<col=00ff00>Wildy QoL:</col> v1.3.0: added remapping for new fish icons, extended freeze timers (ancient sceptres, swampbark), and rune pouch misclick prevention. Check plugin settings for details.")
			.build());
		configManager.setConfiguration(CONFIG_GROUP, UPDATE_MESSAGE_KEY, true);
		shouldShowUpdateMessage = false;
	}
}
