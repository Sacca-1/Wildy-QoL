package com.wildyqol.pvparena;

import com.wildyqol.WildyQoLConfig;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class PvpArenaSpellbookWarningOverlay extends Overlay
{
	private static final Color HIGHLIGHT_COLOR = Color.RED;
	private static final float HIGHLIGHT_STROKE_WIDTH = 2.0f;
	private static final int HIGHLIGHT_PADDING = 2;

	private final Client client;
	private final WildyQoLConfig config;
	private final PvpArenaSpellbookWarningEvaluator evaluator = new PvpArenaSpellbookWarningEvaluator();

	@Inject
	private PvpArenaSpellbookWarningOverlay(Client client, WildyQoLConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.pvpArenaSpellbookWarningMode() == WildyQoLConfig.PvpArenaSpellbookWarningMode.NEVER
			|| !isPvpArenaLoadoutInterfaceVisible())
		{
			return null;
		}

		if (config.pvpArenaSpellbookWarningMode() == WildyQoLConfig.PvpArenaSpellbookWarningMode.PLAYERS_MATCH
			&& !isOpponentSpellbookVisible())
		{
			return null;
		}

		PvpArenaSpellbookWarning warning = evaluator.evaluate(
			config.pvpArenaSpellbookWarningMode(),
			client.getVarbitValue(VarbitID.PVPA_TRANSMIT_BUILD),
			client.getVarbitValue(VarbitID.PVPA_LOADOUT_A_SPELLBOOK),
			client.getVarbitValue(VarbitID.PVPA_LOADOUT_B_SPELLBOOK),
			client.getVarbitValue(VarbitID.PVPA_LOADOUT_C_SPELLBOOK),
			client.getVarbitValue(VarbitID.PVPA_UNRANKEDDUEL_TRANSMIT_OPPONENTSPELLBOOK));

		if (!warning.hasWarning())
		{
			return null;
		}

		if (warning.isOwnSpellbookOffending())
		{
			drawWidgetHighlight(graphics, activeOwnSpellbookDisplay(warning.getActiveBuild()));
			if (isOpponentKitVisible())
			{
				drawWidgetHighlight(graphics, activeBuildTab(warning.getActiveBuild()));
			}
		}

		if (warning.isOpponentSpellbookOffending())
		{
			drawWidgetHighlight(graphics, visibleWidget(InterfaceID.PvpArenaUnrankedduel.OPPONENTSPELLBOOK_DISPLAY));
		}

		return null;
	}

	private boolean isPvpArenaLoadoutInterfaceVisible()
	{
		return isWidgetVisible(InterfaceID.PvpArenaStagingareaShareloadout.CONTENTS)
			|| isWidgetVisible(InterfaceID.PvpArenaStagingareaSupplies.CONTENTS)
			|| isWidgetVisible(InterfaceID.PvpArenaUnrankedduel.CONTENTS);
	}

	private boolean isOpponentKitVisible()
	{
		return isWidgetVisible(InterfaceID.PvpArenaUnrankedduel.OPPONENT)
			|| isWidgetVisible(InterfaceID.PvpArenaUnrankedduel.OPPONENTSPELLBOOK_DISPLAY);
	}

	private boolean isOpponentSpellbookVisible()
	{
		return isWidgetVisible(InterfaceID.PvpArenaUnrankedduel.OPPONENTSPELLBOOK_DISPLAY);
	}

	private Widget activeOwnSpellbookDisplay(PvpArenaBuild build)
	{
		Widget stagingDisplay = activeStagingSpellbookDisplay(build);
		if (stagingDisplay != null)
		{
			return stagingDisplay;
		}

		return activeUnrankedSpellbookDisplay(build);
	}

	private Widget activeStagingSpellbookDisplay(PvpArenaBuild build)
	{
		switch (build)
		{
			case MAIN:
				return visibleWidget(InterfaceID.PvpArenaStagingareaSupplies._0SPELLBOOK_DISPLAY);
			case ZERK:
				return visibleWidget(InterfaceID.PvpArenaStagingareaSupplies._1SPELLBOOK_DISPLAY);
			case PURE:
				return visibleWidget(InterfaceID.PvpArenaStagingareaSupplies._2SPELLBOOK_DISPLAY);
			default:
				return null;
		}
	}

	private Widget activeUnrankedSpellbookDisplay(PvpArenaBuild build)
	{
		switch (build)
		{
			case MAIN:
				return visibleWidget(InterfaceID.PvpArenaUnrankedduel._0SPELLBOOK_DISPLAY);
			case ZERK:
				return visibleWidget(InterfaceID.PvpArenaUnrankedduel._1SPELLBOOK_DISPLAY);
			case PURE:
				return visibleWidget(InterfaceID.PvpArenaUnrankedduel._2SPELLBOOK_DISPLAY);
			default:
				return null;
		}
	}

	private Widget activeBuildTab(PvpArenaBuild build)
	{
		Widget shareLoadoutTab = activeShareLoadoutBuildTab(build);
		if (shareLoadoutTab != null)
		{
			return shareLoadoutTab;
		}

		Widget stagingTab = activeStagingBuildTab(build);
		if (stagingTab != null)
		{
			return stagingTab;
		}

		return visibleWidget(InterfaceID.PvpArenaUnrankedduel.TAB_MYEQUIPMENT);
	}

	private Widget activeShareLoadoutBuildTab(PvpArenaBuild build)
	{
		switch (build)
		{
			case MAIN:
				return visibleWidget(InterfaceID.PvpArenaStagingareaShareloadout.TAB_EQUIPMENT0);
			case ZERK:
				return visibleWidget(InterfaceID.PvpArenaStagingareaShareloadout.TAB_EQUIPMENT1);
			case PURE:
				return visibleWidget(InterfaceID.PvpArenaStagingareaShareloadout.TAB_EQUIPMENT2);
			default:
				return null;
		}
	}

	private Widget activeStagingBuildTab(PvpArenaBuild build)
	{
		switch (build)
		{
			case MAIN:
				return visibleWidget(InterfaceID.PvpArenaStagingareaSupplies.TAB_EQUIPMENT0);
			case ZERK:
				return visibleWidget(InterfaceID.PvpArenaStagingareaSupplies.TAB_EQUIPMENT1);
			case PURE:
				return visibleWidget(InterfaceID.PvpArenaStagingareaSupplies.TAB_EQUIPMENT2);
			default:
				return null;
		}
	}

	private void drawWidgetHighlight(Graphics2D graphics, Widget widget)
	{
		if (widget == null)
		{
			return;
		}

		Rectangle bounds = widget.getBounds();
		if (bounds == null || bounds.width <= 0 || bounds.height <= 0)
		{
			return;
		}

		Stroke previousStroke = graphics.getStroke();
		Color previousColor = graphics.getColor();
		graphics.setStroke(new BasicStroke(HIGHLIGHT_STROKE_WIDTH));
		graphics.setColor(HIGHLIGHT_COLOR);
		graphics.drawRect(
			bounds.x - HIGHLIGHT_PADDING,
			bounds.y - HIGHLIGHT_PADDING,
			bounds.width + HIGHLIGHT_PADDING * 2,
			bounds.height + HIGHLIGHT_PADDING * 2);
		graphics.setStroke(previousStroke);
		graphics.setColor(previousColor);
	}

	private Widget visibleWidget(int widgetId)
	{
		Widget widget = client.getWidget(widgetId);
		if (widget == null || widget.isHidden())
		{
			return null;
		}

		return widget;
	}

	private boolean isWidgetVisible(int widgetId)
	{
		return visibleWidget(widgetId) != null;
	}
}
