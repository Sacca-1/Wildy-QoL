package com.wildyqol.itemskeptondeath;

import com.wildyqol.WildyQoLConfig;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@Singleton
public class IkodParchmentRiskService
{
	private static final Pattern WILDERNESS_LEVEL_PATTERN = Pattern.compile("level\\s*:?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

	private final Client client;
	private final ItemManager itemManager;
	private final OverlayManager overlayManager;
	private final IkodParchmentRiskOverlay overlay;
	private final WildyQoLConfig config;

	@Inject
	private IkodParchmentRiskService(
		Client client,
		ItemManager itemManager,
		OverlayManager overlayManager,
		IkodParchmentRiskOverlay overlay,
		WildyQoLConfig config)
	{
		this.client = client;
		this.itemManager = itemManager;
		this.overlayManager = overlayManager;
		this.overlay = overlay;
		this.config = config;
	}

	public void startUp()
	{
		overlayManager.add(overlay);
	}

	public void refresh()
	{
		updateUntradeablesRepairCost();
	}

	public void shutDown()
	{
		overlayManager.remove(overlay);
		overlay.reset();
	}

	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.DEATHKEEP)
		{
			updateUntradeablesRepairCost();
		}
	}

	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() == InterfaceID.DEATHKEEP)
		{
			overlay.reset();
		}
	}

	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (isIkodOpen())
		{
			updateUntradeablesRepairCost();
		}
	}

	public void onVarbitChanged(VarbitChanged event)
	{
		if (isIkodOpen())
		{
			updateUntradeablesRepairCost();
		}
	}

	public void onGameTick(GameTick event)
	{
		if (isIkodOpen())
		{
			updateUntradeablesRepairCost();
		}
	}

	private void updateUntradeablesRepairCost()
	{
		if (!config.showIkodRepairCostsOverlay())
		{
			overlay.reset();
			return;
		}

		if (!isIkodOpen())
		{
			overlay.reset();
			return;
		}

		Widget riskWidget = client.getWidget(InterfaceID.Deathkeep.VALUE);
		if (riskWidget == null || riskWidget.isHidden())
		{
			overlay.reset();
			return;
		}

		boolean aboveLevel20 = isAboveLevel20Selected();
		long surcharge = getUntradeablesRepairCost(aboveLevel20);
		long baseRisk = getGuideRiskValue(riskWidget);
		Color labelColor = deriveLabelColor(riskWidget);

		overlay.update(true, surcharge, baseRisk, labelColor);
	}

	private boolean isIkodOpen()
	{
		Widget root = client.getWidget(InterfaceID.Deathkeep.CONTENTS);
		return root != null && !root.isHidden();
	}

	private long getUntradeablesRepairCost(boolean aboveLevel20)
	{
		Set<Widget> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		Deque<Widget> stack = new ArrayDeque<>();
		pushRoot(stack, client.getWidget(InterfaceID.Deathkeep.KEPT));
		pushRoot(stack, client.getWidget(InterfaceID.Deathkeep.GRAVE));

		if (stack.isEmpty())
		{
			pushRoot(stack, client.getWidget(InterfaceID.Deathkeep.ITEMS));
		}

		long repairCost = 0L;

		while (!stack.isEmpty())
		{
			Widget widget = stack.pop();
			if (widget == null || !visited.add(widget) || widget.isHidden())
			{
				continue;
			}

			repairCost += getRepairCostForWidget(widget, aboveLevel20);
			pushChildren(stack, widget.getDynamicChildren());
			pushChildren(stack, widget.getStaticChildren());
			pushChildren(stack, widget.getNestedChildren());
			pushChildren(stack, widget.getChildren());
		}

		return repairCost;
	}

	private void pushRoot(Deque<Widget> stack, Widget widget)
	{
		if (widget != null && !widget.isHidden())
		{
			stack.push(widget);
		}
	}

	private long getRepairCostForWidget(Widget widget, boolean aboveLevel20)
	{
		int itemId = widget.getItemId();
		if (itemId <= 0)
		{
			return 0L;
		}

		ItemComposition rawComposition = itemManager.getItemComposition(itemId);
		String rawItemName = rawComposition == null ? "" : rawComposition.getName();
		int canonicalId = itemManager.canonicalize(itemId);
		ItemComposition canonicalComposition = itemManager.getItemComposition(canonicalId);
		String canonicalItemName = canonicalComposition == null ? "" : canonicalComposition.getName();
		if (!isRepairCostCandidate(widget.getBorderType(), itemId, rawItemName, canonicalId, canonicalItemName))
		{
			return 0L;
		}

		long repairCost = getRepairCost(itemId, rawItemName, aboveLevel20);
		if (repairCost == 0L && canonicalId != itemId)
		{
			repairCost = getRepairCost(canonicalId, canonicalItemName, aboveLevel20);
		}

		return Math.max(1, widget.getItemQuantity()) * repairCost;
	}

	static boolean isRepairCostCandidate(int borderType, int itemId, String itemName, int canonicalId, String canonicalItemName)
	{
		boolean hasKnownRepairCost = IkodUntradeableRepairCosts.hasRepairCost(itemId, itemName)
			|| IkodUntradeableRepairCosts.hasRepairCost(canonicalId, canonicalItemName);
		return (borderType == 2 && hasKnownRepairCost)
			|| IkodUntradeableRepairCosts.isDisplayedBrokenOrMangled(itemName)
			|| IkodUntradeableRepairCosts.isDisplayedBrokenOrMangled(canonicalItemName);
	}

	private long getRepairCost(int itemId, String itemName, boolean aboveLevel20)
	{
		return IkodUntradeableRepairCosts.getRepairCost(
			itemId,
			itemName,
			aboveLevel20,
			itemManager::getItemPrice);
	}

	private void pushChildren(Deque<Widget> stack, Widget[] children)
	{
		if (children == null)
		{
			return;
		}

		for (Widget child : children)
		{
			if (child != null)
			{
				stack.push(child);
			}
		}
	}

	private boolean isAboveLevel20Selected()
	{
		Boolean selectedDepth = parseSelectedDepth(getVisibleWidgetText(client.getWidget(InterfaceID.Deathkeep.RIGHT_TEXT0)));
		if (selectedDepth != null)
		{
			return selectedDepth;
		}

		selectedDepth = parseSelectedDepth(getVisibleWidgetText(client.getWidget(InterfaceID.Deathkeep.TOPAREA)));
		if (selectedDepth != null)
		{
			return selectedDepth;
		}

		selectedDepth = parseSelectedDepth(getVisibleWidgetText(client.getWidget(InterfaceID.Deathkeep.CONTENTS)));
		if (selectedDepth != null)
		{
			return selectedDepth;
		}

		Widget wildernessLevelWidget = client.getWidget(InterfaceID.PvpIcons.WILDERNESSLEVEL);
		if (wildernessLevelWidget == null || wildernessLevelWidget.isHidden())
		{
			return false;
		}

		return isAboveLevel20(Text.removeTags(wildernessLevelWidget.getText()));
	}

	static Boolean parseSelectedDepth(String deathkeepText)
	{
		String text = Text.removeTags(deathkeepText == null ? "" : deathkeepText).toLowerCase(Locale.ROOT);
		boolean above = text.contains("above level 20")
			|| text.contains("over level 20")
			|| text.contains("beyond level 20")
			|| text.contains("deeper than level 20");
		boolean below = text.contains("below level 20")
			|| text.contains("under level 20")
			|| text.contains("up to level 20")
			|| text.contains("killed by a player")
			|| text.contains("killed by another player");

		if (above == below)
		{
			return null;
		}

		return above;
	}

	private String getVisibleWidgetText(Widget root)
	{
		if (root == null || root.isHidden())
		{
			return "";
		}

		Set<Widget> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		Deque<Widget> stack = new ArrayDeque<>();
		StringBuilder text = new StringBuilder();
		stack.push(root);

		while (!stack.isEmpty())
		{
			Widget widget = stack.pop();
			if (widget == null || !visited.add(widget) || widget.isHidden())
			{
				continue;
			}

			String widgetText = widget.getText();
			if (widgetText != null && !widgetText.isEmpty())
			{
				if (text.length() > 0)
				{
					text.append('\n');
				}
				text.append(widgetText);
			}

			pushChildren(stack, widget.getDynamicChildren());
			pushChildren(stack, widget.getStaticChildren());
			pushChildren(stack, widget.getNestedChildren());
			pushChildren(stack, widget.getChildren());
		}

		return text.toString();
	}

	static boolean isAboveLevel20(String wildernessText)
	{
		Matcher matcher = WILDERNESS_LEVEL_PATTERN.matcher(wildernessText == null ? "" : wildernessText);
		return matcher.find() && Integer.parseInt(matcher.group(1)) > 20;
	}

	private long getGuideRiskValue(Widget riskWidget)
	{
		String rawText = riskWidget.getText();
		if (rawText == null)
		{
			return 0L;
		}

		String sanitized = Text.removeTags(rawText).replaceAll("[^0-9]", "");
		if (sanitized.isEmpty())
		{
			return 0L;
		}

		try
		{
			return Long.parseLong(sanitized);
		}
		catch (NumberFormatException ex)
		{
			return 0L;
		}
	}

	private Color deriveLabelColor(Widget riskWidget)
	{
		int color = riskWidget.getTextColor();
		if (color == 0)
		{
			return Color.WHITE;
		}

		return new Color(color | 0xFF000000, true);
	}
}
