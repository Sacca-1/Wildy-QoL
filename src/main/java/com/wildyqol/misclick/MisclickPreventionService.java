package com.wildyqol.misclick;

import com.wildyqol.WildyQoLConfig;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.game.ItemManager;

@Slf4j
@Singleton
public class MisclickPreventionService
{
	private static final Set<Integer> RUNE_POUCH_ITEM_IDS = Set.of(
		ItemID.BH_RUNE_POUCH,
		ItemID.BH_RUNE_POUCH_TROUVER,
		ItemID.DIVINE_RUNE_POUCH,
		ItemID.DIVINE_RUNE_POUCH_TROUVER
	);

	private final Client client;
	private final ItemManager itemManager;
	private final WildyQoLConfig config;

	@Inject
	private MisclickPreventionService(Client client, ItemManager itemManager, WildyQoLConfig config)
	{
		this.client = client;
		this.itemManager = itemManager;
		this.config = config;
	}

	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (config.petSpellBlocker())
		{
			handlePetSpellBlock(event);
		}
	}

	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (handleRunePouchLeftClick(event))
		{
			return;
		}

		handleEmptyVialBlocker(event);
	}

	private void handlePetSpellBlock(MenuEntryAdded event)
	{
		boolean isCastOption = "Cast".equals(event.getOption());
		boolean isExamineNpc = event.getMenuEntry().getType() == MenuAction.EXAMINE_NPC;
		boolean isSpellSelected = isSpellSelected();

		if (!isCastOption && (!isSpellSelected || !isExamineNpc))
		{
			return;
		}

		int npcIndex = event.getIdentifier();
		if (npcIndex < 0)
		{
			return;
		}

		NPC npc = client.getTopLevelWorldView().npcs().byIndex(npcIndex);
		if (npc == null)
		{
			return;
		}

		NPCComposition comp = npc.getComposition();
		if (comp != null && comp.isFollower())
		{
			client.getMenu().removeMenuEntry(event.getMenuEntry());
			log.debug("Removed spell cast menu entry for pet: {}", npc.getName());
		}
	}

	private boolean isSpellSelected()
	{
		if (!client.isWidgetSelected())
		{
			return false;
		}

		Widget selectedWidget = client.getSelectedWidget();
		return selectedWidget != null
			&& WidgetUtil.componentToInterface(selectedWidget.getId()) == InterfaceID.MAGIC_SPELLBOOK;
	}

	private boolean handleRunePouchLeftClick(MenuOptionClicked event)
	{
		if (!config.runePouchBlocker() || client.isMenuOpen())
		{
			return false;
		}

		if (!isInPvpArea())
		{
			return false;
		}

		MenuEntry clicked = event.getMenuEntry();
		if (!isRunePouch(clicked.getItemId()) || clicked.getType() == MenuAction.EXAMINE_ITEM)
		{
			return false;
		}

		event.consume();
		return true;
	}

	private boolean isRunePouch(int itemId)
	{
		int canonicalId = itemManager.canonicalize(itemId);
		return canonicalId > 0 && RUNE_POUCH_ITEM_IDS.contains(canonicalId);
	}

	private boolean isInPvpArea()
	{
		return client.getVarbitValue(VarbitID.INSIDE_WILDERNESS) == 1
			|| client.getVarbitValue(VarbitID.PVP_AREA_CLIENT) == 1;
	}

	private void handleEmptyVialBlocker(MenuOptionClicked event)
	{
		if (!config.emptyVialBlocker() || !isInPvpArea())
		{
			return;
		}

		if (!"Use".equals(event.getMenuEntry().getOption()))
		{
			return;
		}

		if (event.getMenuEntry().getItemId() == ItemID.VIAL_EMPTY)
		{
			event.consume();
		}
	}
}
