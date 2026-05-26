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
	private static final String UPDATE_MESSAGE_142_KEY = "updateMessageShown142";
	private static final String UPDATE_MESSAGE_140 = "<col=00ff00>Wildy QoL 1.4.0:</col><br>- Added DMM overload proc timer<br>- Added warnings when banking for common PvP equipment problems (ammo, runes, spellbook, charges, teleport out). Please check the plugin settings if you want to customize this.<br>Found any issues or have a request? Message me on discord @sacca_1 or create an issue on the Wildy QoL github.";
	private static final String UPDATE_MESSAGE_142 = "<col=00ff00>Wildy QoL 1.4.2:</col><br>New features for the Emir's PvP arena:<br>- Hide obstructive scenery to get a better view of your fight (off by default)<br>- Highlight for wrong selected spellbooks in unranked duel (ancients by default)";

	private final ConfigManager configManager;
	private final WildyQoLConfig config;
	private final ChatMessageManager chatMessageManager;

	private boolean shouldShowUpdateMessage140;
	private boolean shouldShowUpdateMessage142;

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
		shouldShowUpdateMessage142 = !config.updateMessageShown142();
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN || !shouldShowUpdateMessage140 && !shouldShowUpdateMessage142)
		{
			return;
		}

		if (shouldShowUpdateMessage140)
		{
			queueUpdateMessage(UPDATE_MESSAGE_140);
			configManager.setConfiguration(CONFIG_GROUP, UPDATE_MESSAGE_140_KEY, true);
			shouldShowUpdateMessage140 = false;
		}

		if (shouldShowUpdateMessage142)
		{
			queueUpdateMessage(UPDATE_MESSAGE_142);
			configManager.setConfiguration(CONFIG_GROUP, UPDATE_MESSAGE_142_KEY, true);
			shouldShowUpdateMessage142 = false;
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
