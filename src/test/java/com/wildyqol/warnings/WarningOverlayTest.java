package com.wildyqol.warnings;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class WarningOverlayTest
{
	@Test
	public void cautionSeverityUsesCautionColor()
	{
		assertEquals(Color.ORANGE, WarningOverlay.colorForWarning(WarningSeverity.CAUTION));
	}

	@Test
	public void criticalSeverityUsesCriticalColor()
	{
		assertEquals(Color.RED, WarningOverlay.colorForWarning(WarningSeverity.CRITICAL));
	}

	@Test
	public void ordersCriticalWarningsBeforeCautionWarnings()
	{
		WarningLine lowCharges = new WarningLine(WarningSeverity.CAUTION, "Low tome of fire charges: 200/500");
		WarningLine missingTeleBlock = new WarningLine(WarningSeverity.CRITICAL, "No Tele Block casts");
		WarningLine suboptimalAmmo = new WarningLine(WarningSeverity.CAUTION, "Suboptimal ammo: Ruby dragon bolts");
		WarningLine missingTeleport = new WarningLine(WarningSeverity.CRITICAL, "No teleport out");

		List<WarningLine> warnings = WarningOverlay.orderWarnings(Arrays.asList(
			lowCharges,
			missingTeleBlock,
			suboptimalAmmo,
			missingTeleport));

		assertEquals(Arrays.asList(
			missingTeleBlock,
			missingTeleport,
			lowCharges,
			suboptimalAmmo), warnings);
	}
}
