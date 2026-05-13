package com.wildyqol.warnings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

@Singleton
public class WarningOverlay extends OverlayPanel
{
	private static final int WARNING_PANEL_WIDTH = 175;
	private static final Color CRITICAL_WARNING_COLOR = Color.RED;
	private static final Color CAUTION_WARNING_COLOR = Color.ORANGE;

	private final WarningServiceManager warningServiceManager;

	@Inject
	WarningOverlay(WarningServiceManager warningServiceManager)
	{
		this.warningServiceManager = warningServiceManager;
		setPosition(OverlayPosition.TOP_LEFT);
		setPreferredSize(new Dimension(WARNING_PANEL_WIDTH, 0));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		setPreferredSize(new Dimension(WARNING_PANEL_WIDTH, 0));

		List<String> texts = orderWarnings(warningServiceManager.getOverlayTexts());
		if (texts.isEmpty())
		{
			return null;
		}

		panelComponent.getChildren().add(LineComponent.builder()
			.left(texts.size() == 1 ? "Wildy QoL warning" : "Wildy QoL warnings")
			.leftColor(Color.WHITE)
			.build());

		for (String text : texts)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("- " + text)
				.leftColor(colorForWarning(text))
				.build());
		}
		return super.render(graphics);
	}

	static List<String> orderWarnings(List<String> texts)
	{
		List<String> orderedTexts = new ArrayList<>(texts);
		orderedTexts.sort((left, right) -> Integer.compare(severityRank(left), severityRank(right)));
		return orderedTexts;
	}

	static Color colorForWarning(String text)
	{
		if (text.startsWith("Low ") || text.startsWith("Suboptimal "))
		{
			return CAUTION_WARNING_COLOR;
		}

		return CRITICAL_WARNING_COLOR;
	}

	private static int severityRank(String text)
	{
		return colorForWarning(text).equals(CAUTION_WARNING_COLOR) ? 1 : 0;
	}
}
