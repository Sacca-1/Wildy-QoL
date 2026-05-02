package com.wildyqol.itemskeptondeath;

import com.wildyqol.WildyQoLConfig;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@Singleton
public class IkodParchmentRiskService
{
	private static final long TROUVER_REPARCH_COST = 500_000L;

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
		updateTrouverSurcharge();
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
			updateTrouverSurcharge();
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
			updateTrouverSurcharge();
		}
	}

	public void onVarbitChanged(VarbitChanged event)
	{
		if (isIkodOpen())
		{
			updateTrouverSurcharge();
		}
	}

	private void updateTrouverSurcharge()
	{
		if (!config.showIkodTrouverOverlay())
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

		int lockedCount = countLockedItems();
		long perItemCost = getPerItemCost();
		long surcharge = lockedCount * perItemCost;
		long baseRisk = getGuideRiskValue(riskWidget);
		Color labelColor = deriveLabelColor(riskWidget);

		overlay.update(true, lockedCount, perItemCost, surcharge, baseRisk, labelColor);
	}

	private boolean isIkodOpen()
	{
		Widget root = client.getWidget(InterfaceID.Deathkeep.CONTENTS);
		return root != null && !root.isHidden();
	}

	private int countLockedItems()
	{
		Set<Widget> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		Deque<Widget> stack = new ArrayDeque<>();
		pushRoot(stack, client.getWidget(InterfaceID.Deathkeep.KEPT));
		pushRoot(stack, client.getWidget(InterfaceID.Deathkeep.GRAVE));

		if (stack.isEmpty())
		{
			pushRoot(stack, client.getWidget(InterfaceID.Deathkeep.ITEMS));
		}

		int lockedCount = 0;

		while (!stack.isEmpty())
		{
			Widget widget = stack.pop();
			if (widget == null || !visited.add(widget) || widget.isHidden())
			{
				continue;
			}

			lockedCount += countLockedItemsForWidget(widget);
			pushChildren(stack, widget.getDynamicChildren());
			pushChildren(stack, widget.getStaticChildren());
			pushChildren(stack, widget.getNestedChildren());
			pushChildren(stack, widget.getChildren());
		}

		return lockedCount;
	}

	private void pushRoot(Deque<Widget> stack, Widget widget)
	{
		if (widget != null && !widget.isHidden())
		{
			stack.push(widget);
		}
	}

	private int countLockedItemsForWidget(Widget widget)
	{
		int itemId = widget.getItemId();
		if (itemId <= 0 || widget.getBorderType() != 2)
		{
			return 0;
		}

		int canonicalId = itemManager.canonicalize(itemId);
		ItemComposition composition = itemManager.getItemComposition(canonicalId);
		if (composition == null || !composition.getName().endsWith(" (l)"))
		{
			return 0;
		}

		return Math.max(1, widget.getItemQuantity());
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

	private long getPerItemCost()
	{
		long parchmentPrice = itemManager.getItemPrice(ItemID.TROUVER_PARCHMENT);
		return parchmentPrice + TROUVER_REPARCH_COST;
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
