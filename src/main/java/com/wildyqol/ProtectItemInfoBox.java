package com.wildyqol;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;

public class ProtectItemInfoBox extends InfoBox
{
	private static final int SPRITE_PRAYER_PROTECT_ITEM = 123;

	public ProtectItemInfoBox(BufferedImage image, Plugin plugin)
	{
		super(image, plugin);
	}

	public static BufferedImage createImage(SpriteManager spriteManager)
	{
		BufferedImage prayerImage = spriteManager.getSprite(SPRITE_PRAYER_PROTECT_ITEM, 0);
		if (prayerImage == null)
		{
			return null;
		}

		BufferedImage combined = new BufferedImage(prayerImage.getWidth(), prayerImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = combined.createGraphics();
		g.drawImage(prayerImage, 0, 0, null);

		// Draw red cross in bottom right
		g.setColor(Color.RED);
		int w = combined.getWidth();
		int h = combined.getHeight();

		// Thick red cross
		// \
		g.drawLine(w - 10, h - 10, w - 2, h - 2);
		g.drawLine(w - 10, h - 9, w - 3, h - 2); // thickness

		// /
		g.drawLine(w - 10, h - 2, w - 2, h - 10);
		g.drawLine(w - 9, h - 2, w - 2, h - 9); // thickness

		g.dispose();
		return combined;
	}

	@Override
	public String getText()
	{
		return null;
	}

	@Override
	public Color getTextColor()
	{
		return null;
	}

	@Override
	public String getTooltip()
	{
		return "Enable Protect Item!";
	}
}
