package com.wildyqol.dmmoverload;

import com.wildyqol.WildyQoLConfig;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TextComponent;
import net.runelite.client.util.ImageUtil;

@Singleton
public class DmmOverloadProcStatusBarOverlay extends Overlay
{
	private static final Color BACKGROUND = new Color(0, 0, 0, 150);
	private static final Color BAR_COLOR = new Color(0, 0, 0, 220);
	private static final Color COUNTER_COLOR = Color.WHITE;
	private static final int BORDER_SIZE = 1;
	private static final int BAR_WIDTH = 20;
	private static final int HEIGHT = 252;
	private static final int RESIZED_BOTTOM_HEIGHT = 272;
	private static final int RESIZED_BOTTOM_OFFSET_Y = 12;
	private static final int RESIZED_BOTTOM_OFFSET_X = 10;
	private static final int ICON_MAX_HEIGHT = 32;
	private static final int ICON_HORIZONTAL_PADDING = 2;
	private static final int ICON_TOP_PADDING = 4;
	private static final int SKILL_ICON_HEIGHT = 35;
	private static final int COUNTER_ONLY_HEIGHT = 18;

	public enum DmmOverloadStatusBarPosition
	{
		LEFT,
		RIGHT
	}

	private final Client client;
	private final DmmOverloadProcTimerService timerService;
	private final WildyQoLConfig config;

	private BufferedImage overloadIcon;
	private BufferedImage cachedScaledIcon;
	private DmmOverloadStatusBarPosition position = DmmOverloadStatusBarPosition.LEFT;

	@Inject
	private DmmOverloadProcStatusBarOverlay(Client client, DmmOverloadProcTimerService timerService, WildyQoLConfig config)
	{
		this.client = client;
		this.timerService = timerService;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	public void setOverloadImage(BufferedImage icon)
	{
		this.overloadIcon = icon;
		this.cachedScaledIcon = null;
	}

	public void clearOverloadImage()
	{
		this.overloadIcon = null;
		this.cachedScaledIcon = null;
	}

	public void setPosition(DmmOverloadStatusBarPosition position)
	{
		this.position = position != null ? position : DmmOverloadStatusBarPosition.LEFT;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		WildyQoLConfig.DmmOverloadProcStatusBarMode mode = config.dmmOverloadProcTimerStatusBarMode();
		if (mode == null)
		{
			return null;
		}
		if (mode == WildyQoLConfig.DmmOverloadProcStatusBarMode.OFF)
		{
			return null;
		}

		DmmOverloadStatusBarPosition desiredPosition =
			mode == WildyQoLConfig.DmmOverloadProcStatusBarMode.LEFT
				? DmmOverloadStatusBarPosition.LEFT
				: DmmOverloadStatusBarPosition.RIGHT;
		if (desiredPosition != position)
		{
			position = desiredPosition;
		}

		if (!timerService.isActive())
		{
			return null;
		}

		double fractionRemaining = config.dmmOverloadProcTimerDisplayTicks()
			? timerService.getFractionRemainingTicks(client.getTickCount())
			: timerService.getFractionRemaining();
		if (fractionRemaining < 0)
		{
			return null;
		}

		double fillFraction = clamp01(1.0 - fractionRemaining);

		ViewportMatch viewportMatch = locateViewport();
		if (viewportMatch == null)
		{
			return null;
		}

		renderStatusBar(graphics, viewportMatch, fillFraction);
		return null;
	}

	private void renderStatusBar(Graphics2D graphics, ViewportMatch viewportMatch, double fillFraction)
	{
		Point widgetLocation = viewportMatch.widget.getCanvasLocation();
		if (widgetLocation == null)
		{
			return;
		}

		int width = BAR_WIDTH;
		int height = viewportMatch.viewport == DmmOverloadViewport.RESIZED_BOTTOM ? RESIZED_BOTTOM_HEIGHT : HEIGHT;
		int x;
		int y;

		if (viewportMatch.viewport == DmmOverloadViewport.RESIZED_BOTTOM)
		{
			int barWidthOffset = width - BAR_WIDTH;
			if (position == DmmOverloadStatusBarPosition.LEFT)
			{
				x = widgetLocation.getX() + RESIZED_BOTTOM_OFFSET_X - viewportMatch.viewport.getOffsetLeft().getX() - 2 * barWidthOffset;
				y = widgetLocation.getY() - RESIZED_BOTTOM_OFFSET_Y - viewportMatch.viewport.getOffsetLeft().getY();
			}
			else
			{
				x = widgetLocation.getX() + RESIZED_BOTTOM_OFFSET_X - viewportMatch.viewport.getOffsetRight().getX() - barWidthOffset;
				y = widgetLocation.getY() - RESIZED_BOTTOM_OFFSET_Y - viewportMatch.viewport.getOffsetRight().getY();
			}
		}
		else
		{
			if (position == DmmOverloadStatusBarPosition.LEFT)
			{
				x = widgetLocation.getX() - viewportMatch.viewport.getOffsetLeft().getX();
				y = widgetLocation.getY() - viewportMatch.viewport.getOffsetLeft().getY();
			}
			else
			{
				x = widgetLocation.getX() - viewportMatch.viewport.getOffsetRight().getX() + viewportMatch.widget.getWidth();
				y = widgetLocation.getY() - viewportMatch.viewport.getOffsetRight().getY();
			}
		}

		drawBar(graphics, x, y, width, height, fillFraction);
		boolean iconDrawn = drawIcon(graphics, x, y, width);
		drawCounter(graphics, x, y, width, iconDrawn);
	}

	private void drawBar(Graphics2D graphics, int x, int y, int width, int height, double fillFraction)
	{
		int fillHeight = (int) Math.round(height * clamp01(fillFraction));
		fillHeight = Math.max(0, Math.min(height, fillHeight));

		graphics.setColor(BACKGROUND);
		graphics.drawRect(x, y, width - BORDER_SIZE, height - BORDER_SIZE);
		graphics.fillRect(x, y, width, height);

		int innerHeight = fillHeight - BORDER_SIZE * 2;
		if (innerHeight <= 0)
		{
			return;
		}

		graphics.setColor(BAR_COLOR);
		graphics.fillRect(
			x + BORDER_SIZE,
			y + BORDER_SIZE + (height - fillHeight),
			width - BORDER_SIZE * 2,
			innerHeight
		);
	}

	private boolean drawIcon(Graphics2D graphics, int x, int y, int width)
	{
		BufferedImage icon = getIconForWidth(width);
		if (icon == null)
		{
			return false;
		}

		int drawX = x + (width - icon.getWidth()) / 2;
		int drawY = y + ICON_TOP_PADDING;
		graphics.drawImage(icon, drawX, drawY, null);
		return true;
	}

	private void drawCounter(Graphics2D graphics, int x, int y, int width, boolean iconDrawn)
	{
		String counterText = getCounterText();
		if (counterText == null)
		{
			return;
		}

		graphics.setFont(FontManager.getRunescapeFont());
		FontMetrics metrics = graphics.getFontMetrics();
		int textWidth = metrics.stringWidth(counterText);
		int textX = x + (width / 2) - (textWidth / 2);
		int yOffset = iconDrawn ? SKILL_ICON_HEIGHT : COUNTER_ONLY_HEIGHT;
		int textY = y + yOffset;

		TextComponent textComponent = new TextComponent();
		textComponent.setText(counterText);
		textComponent.setColor(COUNTER_COLOR);
		textComponent.setPosition(new java.awt.Point(textX, textY));
		textComponent.render(graphics);
	}

	private BufferedImage getIconForWidth(int width)
	{
		if (overloadIcon == null)
		{
			return null;
		}

		int availableWidth = Math.max(1, width - BORDER_SIZE * 2 - ICON_HORIZONTAL_PADDING);
		int availableHeight = Math.max(1, ICON_MAX_HEIGHT);

		int iconWidth = overloadIcon.getWidth();
		int iconHeight = overloadIcon.getHeight();

		if (iconWidth <= availableWidth && iconHeight <= availableHeight)
		{
			return overloadIcon;
		}

		double scale = Math.min((double) availableWidth / iconWidth, (double) availableHeight / iconHeight);
		int scaledWidth = Math.max(1, (int) Math.round(iconWidth * scale));
		int scaledHeight = Math.max(1, (int) Math.round(iconHeight * scale));

		if (cachedScaledIcon != null
			&& cachedScaledIcon.getWidth() == scaledWidth
			&& cachedScaledIcon.getHeight() == scaledHeight)
		{
			return cachedScaledIcon;
		}

		cachedScaledIcon = ImageUtil.resizeImage(overloadIcon, scaledWidth, scaledHeight);
		return cachedScaledIcon;
	}

	private String getCounterText()
	{
		if (!timerService.isActive())
		{
			return null;
		}

		if (config.dmmOverloadProcTimerDisplayTicks())
		{
			int ticksRemaining = timerService.getTicksUntilNextProc(client.getTickCount());
			if (ticksRemaining < 0)
			{
				return null;
			}

			return Integer.toString(ticksRemaining);
		}

		double secondsRemaining = timerService.getSecondsUntilNextProc();
		if (secondsRemaining < 0)
		{
			return null;
		}

		int seconds = (int) secondsRemaining;
		if (seconds < 0)
		{
			seconds = 0;
		}

		return Integer.toString(seconds);
	}

	private ViewportMatch locateViewport()
	{
		for (DmmOverloadViewport viewport : DmmOverloadViewport.values())
		{
			Widget viewportWidget = client.getWidget(viewport.getInterfaceId());
			if (viewportWidget != null && !viewportWidget.isHidden())
			{
				return new ViewportMatch(viewport, viewportWidget);
			}
		}

		return null;
	}

	private static double clamp01(double value)
	{
		if (value <= 0)
		{
			return 0.0;
		}

		if (value >= 1)
		{
			return 1.0;
		}

		return value;
	}

	private static final class ViewportMatch
	{
		private final DmmOverloadViewport viewport;
		private final Widget widget;

		private ViewportMatch(DmmOverloadViewport viewport, Widget widget)
		{
			this.viewport = viewport;
			this.widget = widget;
		}
	}

	private enum DmmOverloadViewport
	{
		RESIZED_BOX(InterfaceID.ToplevelOsrsStretch.SIDE_CONTAINER, new Point(20, -4), new Point(0, -4)),
		RESIZED_BOTTOM(InterfaceID.ToplevelPreEoc.SIDE_BACKGROUND, new Point(61, -12), new Point(35, -12)),
		FIXED(InterfaceID.Toplevel.SIDE_PANELS, new Point(20, -4), new Point(0, -4)),
		FIXED_BANK(InterfaceID.Bankside.ITEMS, new Point(20, -4), new Point(0, -4));

		private final int interfaceId;
		private final Point offsetLeft;
		private final Point offsetRight;

		DmmOverloadViewport(int interfaceId, Point offsetLeft, Point offsetRight)
		{
			this.interfaceId = interfaceId;
			this.offsetLeft = offsetLeft;
			this.offsetRight = offsetRight;
		}

		int getInterfaceId()
		{
			return interfaceId;
		}

		Point getOffsetLeft()
		{
			return offsetLeft;
		}

		Point getOffsetRight()
		{
			return offsetRight;
		}
	}
}
