package com.wildyqol;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.WorldType;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;

public class ProtectItemInfoBox extends InfoBox
{
	private static final int SPRITE_PRAYER_PROTECT_ITEM = 123;
	private static final int VARBIT_IN_WILDERNESS = 5963;
	private static final int VARBIT_PVP_AREA_CLIENT = 8121;
	private static final int VARBIT_PRAYER_PROTECT_ITEM = 4112;

	private final Client client;
	private final WildyQoLConfig config;

	public ProtectItemInfoBox(WildyQoLConfig config, Client client, Plugin plugin, SpriteManager spriteManager)
	{
		super(null, plugin);
		this.config = config;
		this.client = client;
		spriteManager.getSpriteAsync(SPRITE_PRAYER_PROTECT_ITEM, 0, this);
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

	@Override
	public void setImage(BufferedImage image)
	{
		if (image == null)
		{
			super.setImage(null);
			return;
		}

		BufferedImage combined = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = combined.createGraphics();
		g.drawImage(image, 0, 0, null);

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
		super.setImage(combined);
	}

	@Override
	public boolean render()
	{
		if (!config.protectItemInfoBox() || !shouldNotifyProtectItem())
		{
			return false;
		}

		return super.render();
	}

	private boolean shouldNotifyProtectItem()
	{
		if (client.getWorldType().contains(WorldType.DEADMAN))
		{
			return false;
		}

		// Check if in PvP area
		boolean inPvp = client.getVarbitValue(VARBIT_IN_WILDERNESS) == 1
			|| client.getVarbitValue(VARBIT_PVP_AREA_CLIENT) == 1;

		if (!inPvp)
		{
			return false;
		}

		// Check if Protect Item is active
		boolean protectItemActive = client.getVarbitValue(VARBIT_PRAYER_PROTECT_ITEM) == 1;

		return !protectItemActive;
	}
}
