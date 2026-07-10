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
	private static final String UPDATE_MESSAGE_140_KEY = "updateMessageShown140";
	private static final String UPDATE_MESSAGE_143_KEY = "updateMessageShown143";
	private static final String UPDATE_MESSAGE_140 = "<col=00ff00>Wildy QoL 1.4.0:</col><br>- Added DMM overload proc timer<br>- Added warnings when banking for common PvP equipment problems (ammo, runes, spellbook, charges, teleport out). Please check the plugin settings if you want to customize this.<br>Found any issues or have a request? Message me on discord @sacca_1 or create an issue on the Wildy QoL github.";
	private static final String UPDATE_MESSAGE_143 = "<col=00ff00>Wildy QoL 1.4.3:</col><br>- Updated Items Kept on Death risk to include untradeable repair costs after the Trouver rework";

	private final ConfigManager configManager;
	private final WildyQoLConfig config;
	private final ChatMessageManager chatMessageManager;

	private boolean shouldShowUpdateMessage140;
	private boolean shouldShowUpdateMessage143;

	@Inject
	private UpdateMessageService(ConfigManager configManager, WildyQoLConfig config, ChatMessageManager chatMessageManager)
	{
		this.configManager = configManager;
		this.config = config;
		this.chatMessageManager = chatMessageManager;
	}

	public void startUp()
	{
		shouldShowUpdateMessage140 = !config.updateMessageShown140();
		shouldShowUpdateMessage143 = !config.updateMessageShown143();
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN
			|| !shouldShowUpdateMessage140 && !shouldShowUpdateMessage143)
		{
			return;
		}

		if (shouldShowUpdateMessage140)
		{
			queueUpdateMessage(UPDATE_MESSAGE_140);
			configManager.setConfiguration(CONFIG_GROUP, UPDATE_MESSAGE_140_KEY, true);
			shouldShowUpdateMessage140 = false;
		}

		if (shouldShowUpdateMessage143)
		{
			queueUpdateMessage(UPDATE_MESSAGE_143);
			configManager.setConfiguration(CONFIG_GROUP, UPDATE_MESSAGE_143_KEY, true);
			shouldShowUpdateMessage143 = false;
		}
	}

	private void queueUpdateMessage(String message)
	{
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage(message)
			.build());
	}
}
