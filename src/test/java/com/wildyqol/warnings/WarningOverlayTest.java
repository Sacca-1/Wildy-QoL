package com.wildyqol.warnings;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class WarningOverlayTest
{
	@Test
	public void lowAndSuboptimalWarningsUseCautionColor()
	{
		assertEquals(Color.ORANGE, WarningOverlay.colorForWarning("Low ammo: bolts 42/100"));
		assertEquals(Color.ORANGE, WarningOverlay.colorForWarning("Suboptimal ammo: Ruby dragon bolts"));
	}

	@Test
	public void missingAndWrongWarningsUseCriticalColor()
	{
		assertEquals(Color.RED, WarningOverlay.colorForWarning("Missing runes: Tele Block"));
		assertEquals(Color.RED, WarningOverlay.colorForWarning("No charges: toxic SOTD"));
		assertEquals(Color.RED, WarningOverlay.colorForWarning("Wrong ammo: dragon crossbow bolts required"));
		assertEquals(Color.RED, WarningOverlay.colorForWarning("Spellbook and runes do not match"));
	}

	@Test
	public void ordersCriticalWarningsBeforeCautionWarnings()
	{
		List<String> warnings = WarningOverlay.orderWarnings(Arrays.asList(
			"Low charges: tome of fire 200/500",
			"Missing runes: Tele Block",
			"Suboptimal ammo: Ruby dragon bolts",
			"Missing teleport out"));

		assertEquals(Arrays.asList(
			"Missing runes: Tele Block",
			"Missing teleport out",
			"Low charges: tome of fire 200/500",
			"Suboptimal ammo: Ruby dragon bolts"), warnings);
	}
}
