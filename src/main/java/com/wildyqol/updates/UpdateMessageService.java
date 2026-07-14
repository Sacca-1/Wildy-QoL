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
	private static final String UPDATE_MESSAGE_150_KEY = "updateMessageShown150";
	private static final String UPDATE_MESSAGE_150 = "<col=00ff00>Wildy QoL 1.5.0:</col><br>"
		+ "Added prayer layout persistence. Your prayer layouts will be remembered and reapplied per character and per LMS / PvP Arena build. (on by default)<br>"
		+ "Updated default warning amounts, clarified plugin settings and messages.";

	private final ConfigManager configManager;
	private final WildyQoLConfig config;
	private final ChatMessageManager chatMessageManager;

	private boolean shouldShowUpdateMessage150;

	@Inject
	private UpdateMessageService(ConfigManager configManager, WildyQoLConfig config, ChatMessageManager chatMessageManager)
	{
		this.configManager = configManager;
		this.config = config;
		this.chatMessageManager = chatMessageManager;
	}

	public void startUp()
	{
		shouldShowUpdateMessage150 = !config.updateMessageShown150();
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN || !shouldShowUpdateMessage150)
		{
			return;
		}

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage(UPDATE_MESSAGE_150)
			.build());
		configManager.setConfiguration(CONFIG_GROUP, UPDATE_MESSAGE_150_KEY, true);
		shouldShowUpdateMessage150 = false;
	}
}
