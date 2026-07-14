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

		List<WarningLine> warnings = orderWarnings(warningServiceManager.getOverlayWarnings());
		if (warnings.isEmpty())
		{
			return null;
		}

		panelComponent.getChildren().add(LineComponent.builder()
			.left(warnings.size() == 1 ? "PvP setup warning" : "PvP setup warnings")
			.leftColor(Color.WHITE)
			.build());

		for (WarningLine warning : warnings)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("- " + warning.getText())
				.leftColor(colorForWarning(warning.getSeverity()))
				.build());
		}
		return super.render(graphics);
	}

	static List<WarningLine> orderWarnings(List<WarningLine> warnings)
	{
		List<WarningLine> orderedWarnings = new ArrayList<>(warnings);
		orderedWarnings.sort((left, right) -> Integer.compare(
			severityRank(left.getSeverity()),
			severityRank(right.getSeverity())));
		return orderedWarnings;
	}

	static Color colorForWarning(WarningSeverity severity)
	{
		return severity == WarningSeverity.CAUTION ? CAUTION_WARNING_COLOR : CRITICAL_WARNING_COLOR;
	}

	private static int severityRank(WarningSeverity severity)
	{
		return severity == WarningSeverity.CAUTION ? 1 : 0;
	}
}
