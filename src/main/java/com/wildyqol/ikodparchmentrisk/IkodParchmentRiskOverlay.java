package com.wildyqol.ikodparchmentrisk;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.client.util.Text;
import com.wildyqol.WildyQoLConfig;

@Singleton
public class IkodParchmentRiskOverlay extends Overlay
{
    private static final int BASELINE_MARGIN = 4;
    private static final int LINE_SPACING = 4;

    private final Client client;
    private final WildyQoLConfig config;

    private boolean ikodOpen;
    private long surcharge;
    private long baseRisk;
    private Color labelColor = Color.WHITE;

    @Inject
    private IkodParchmentRiskOverlay(Client client, WildyQoLConfig config)
    {
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public void update(boolean ikodOpen, int lockedCount, long perItemCost, long surcharge, long baseRisk, Color labelColor)
    {
        this.ikodOpen = ikodOpen;
        this.surcharge = surcharge;
        this.baseRisk = baseRisk;
        this.labelColor = labelColor != null ? labelColor : Color.WHITE;
    }

    public void reset()
    {
        update(false, 0, 0L, 0L, 0L, Color.WHITE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showIkodTrouverOverlay() || !ikodOpen)
        {
            return null;
        }

        Widget riskWidget = client.getWidget(InterfaceID.Deathkeep.VALUE);
        if (riskWidget == null || riskWidget.isHidden())
        {
            return null;
        }

        Rectangle bounds = riskWidget.getBounds();
        Widget messageWidget = client.getWidget(InterfaceID.Deathkeep.TOPAREA);
        Rectangle messageBounds = messageWidget != null ? messageWidget.getBounds() : null;
        boolean condensed = hasAdditionalMessage(messageWidget);

        if (bounds == null)
        {
            return null;
        }

        Font font = FontManager.getRunescapeSmallFont();
        graphics.setFont(font);

        String surchargeText = QuantityFormatter.formatNumber(surcharge);
        long totalRisk = baseRisk + surcharge;
        String totalRiskText = QuantityFormatter.formatNumber(totalRisk);

        String[] lines = condensed
            ? new String[] { "Total risk:", totalRiskText }
            : new String[] { "Trouver cost:", surchargeText, "Total risk:", totalRiskText };

        Color[] colors = condensed
            ? new Color[] { labelColor, Color.WHITE }
            : new Color[] { labelColor, Color.WHITE, labelColor, Color.WHITE };

        int centerX = bounds.x + bounds.width / 2;
        int lineHeight = graphics.getFontMetrics().getHeight();
        int spacing = condensed ? 1 : LINE_SPACING;
        int step = lineHeight + spacing;
        int bottomY = bounds.y - BASELINE_MARGIN;

        if (condensed && messageBounds != null)
        {
            int requiredBottom = messageBounds.y + messageBounds.height + BASELINE_MARGIN + (lines.length - 1) * step;
            bottomY = Math.max(bottomY, requiredBottom);
            bottomY += 4;
        }

        int y = bottomY;

        for (int i = lines.length - 1; i >= 0; i--)
        {
            String line = lines[i];
            int textWidth = graphics.getFontMetrics().stringWidth(line);
            int textX = centerX - textWidth / 2;
            int textY = y;
            if (condensed)
            {
                if (i == 0)
                {
                    textY += 5;
                }
                else
                {
                    textY += 2;
                }
            }
            Point location = new Point(textX, textY);
            OverlayUtil.renderTextLocation(graphics, location, lines[i], colors[i]);
            y -= step;
        }

        return null;
    }

    private boolean hasAdditionalMessage(Widget messageWidget)
    {
        if (messageWidget == null)
        {
            return false;
        }

        StringBuilder textBuilder = new StringBuilder();
        appendWidgetText(textBuilder, messageWidget);

        Widget[] dynamicChildren = messageWidget.getDynamicChildren();
        if (dynamicChildren != null)
        {
            for (Widget child : dynamicChildren)
            {
                appendWidgetText(textBuilder, child);
            }
        }

        if (textBuilder.length() == 0)
        {
            return false;
        }

        String sanitized = Text.removeTags(textBuilder.toString()).toLowerCase();
        return sanitized.contains("player-owned house")
            || sanitized.contains("gravestone")
            || sanitized.contains("safe area");
    }

    private void appendWidgetText(StringBuilder builder, Widget widget)
    {
        if (widget == null)
        {
            return;
        }

        String text = widget.getText();
        if (text == null || text.isEmpty())
        {
            return;
        }

        if (builder.length() > 0)
        {
            builder.append('\n');
        }

        builder.append(text);
    }
}
