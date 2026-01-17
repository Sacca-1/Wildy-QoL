package com.wildyqol;

import java.awt.Color;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

@Singleton
public class FishInventoryIconOverlay extends WidgetItemOverlay
{
    private static final Color INVENTORY_BACKGROUND_RESIZABLE = new Color(62, 53, 41);
    private static final Color INVENTORY_BACKGROUND_FIXED = new Color(66, 58, 45);
    private static final float ACTIVE_ALPHA = 0.55f;
    private static final int ACTIVE_LINGER_CYCLES = 10;

    private final Client client;
    private final ItemManager itemManager;
    private final WildyQoLConfig config;
    private final Map<Long, Integer> recentActiveCycles = new HashMap<>();

    @Inject
    private FishInventoryIconOverlay(Client client, ItemManager itemManager, WildyQoLConfig config)
    {
        this.client = client;
        this.itemManager = itemManager;
        this.config = config;
        drawAfterInterface(InterfaceID.INVENTORY);
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
    {
        if (!isInPvpArea())
        {
            return;
        }

        int replacementId = getReplacementId(itemId);
        if (replacementId == -1)
        {
            return;
        }

        Widget widget = widgetItem.getWidget();
        if (widget == null || widget.getParentId() != InterfaceID.Inventory.ITEMS)
        {
            return;
        }

        Rectangle bounds = widgetItem.getCanvasBounds();
        if (bounds == null)
        {
            return;
        }

        BufferedImage replacementImage = itemManager.getImage(replacementId, widgetItem.getQuantity(), false);
        if (replacementImage == null)
        {
            return;
        }

        BufferedImage originalImage = itemManager.getImage(itemId, widgetItem.getQuantity(), false);
        if (originalImage != null)
        {
            int clearX = bounds.x + (bounds.width - originalImage.getWidth()) / 2;
            int clearY = bounds.y + (bounds.height - originalImage.getHeight()) / 2;
            BufferedImage mask = maskWithBackground(originalImage, getInventoryBackgroundColor());
            graphics.drawImage(mask, clearX, clearY, null);
        }

        int drawX = bounds.x + (bounds.width - replacementImage.getWidth()) / 2;
        int drawY = bounds.y + (bounds.height - replacementImage.getHeight()) / 2;
        Graphics2D imageGraphics = (Graphics2D) graphics.create();
        if (isActiveWidget(widget, bounds))
        {
            imageGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ACTIVE_ALPHA));
        }
        imageGraphics.drawImage(replacementImage, drawX, drawY, null);
        imageGraphics.dispose();
    }

    private int getReplacementId(int itemId)
    {
        if (itemId == ItemID.MARLIN && config.marlinEqualsAnglerfish())
        {
            return ItemID.ANGLERFISH;
        }

        if (itemId == ItemID.HALIBUT && config.halibutEqualsKarambwan())
        {
            return ItemID.TBWT_COOKED_KARAMBWAN;
        }

        return -1;
    }

    private boolean isInPvpArea()
    {
        return client.getVarbitValue(VarbitID.INSIDE_WILDERNESS) == 1
            || client.getVarbitValue(VarbitID.PVP_AREA_CLIENT) == 1;
    }

    private BufferedImage maskWithBackground(BufferedImage source, Color background)
    {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage masked = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int backgroundRgb = background.getRGB() & 0x00FFFFFF;

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int argb = source.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha == 0)
                {
                    continue;
                }
                masked.setRGB(x, y, (alpha << 24) | backgroundRgb);
            }
        }

        return masked;
    }

    private Color getInventoryBackgroundColor()
    {
        return client.isResized() ? INVENTORY_BACKGROUND_RESIZABLE : INVENTORY_BACKGROUND_FIXED;
    }

    private boolean isActiveWidget(Widget widget, Rectangle bounds)
    {
        long key = widgetKey(widget);
        int cycle = client.getGameCycle();

        if (isSelectedWidget(widget) || isDraggedWidget(widget))
        {
            recentActiveCycles.put(key, cycle);
            return true;
        }

        if (client.getMouseCurrentButton() != 1)
        {
            return isRecentlyActive(key, cycle);
        }

        Point mouse = client.getMouseCanvasPosition();
        if (mouse == null)
        {
            return isRecentlyActive(key, cycle);
        }

        if (bounds.contains(mouse.getX(), mouse.getY()))
        {
            recentActiveCycles.put(key, cycle);
            return true;
        }

        return isRecentlyActive(key, cycle);
    }

    private boolean isDraggedWidget(Widget widget)
    {
        Widget dragged = client.getDraggedWidget();
        if (dragged == null)
        {
            return false;
        }

        return dragged == widget
            || (dragged.getId() == widget.getId() && dragged.getIndex() == widget.getIndex());
    }

    private boolean isSelectedWidget(Widget widget)
    {
        if (!client.isWidgetSelected())
        {
            return false;
        }

        Widget selected = client.getSelectedWidget();
        if (selected == null)
        {
            return false;
        }

        return selected == widget
            || (selected.getId() == widget.getId() && selected.getIndex() == widget.getIndex());
    }

    private boolean isRecentlyActive(long key, int currentCycle)
    {
        Integer lastCycle = recentActiveCycles.get(key);
        if (lastCycle == null)
        {
            return false;
        }

        if (currentCycle - lastCycle <= ACTIVE_LINGER_CYCLES)
        {
            return true;
        }

        recentActiveCycles.remove(key);
        return false;
    }

    private long widgetKey(Widget widget)
    {
        return ((long) widget.getId() << 32) | (widget.getIndex() & 0xffffffffL);
    }
}
