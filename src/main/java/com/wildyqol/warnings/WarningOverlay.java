package com.wildyqol.warnings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

@Singleton
public class WarningOverlay extends OverlayPanel
{
	private final WarningServiceManager warningServiceManager;

	@Inject
	WarningOverlay(WarningServiceManager warningServiceManager)
	{
		this.warningServiceManager = warningServiceManager;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		List<String> texts = warningServiceManager.getOverlayTexts();
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
				.left(text)
				.leftColor(Color.RED)
				.build());
		}
		return super.render(graphics);
	}
}
