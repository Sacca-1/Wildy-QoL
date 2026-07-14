package com.wildyqol.warnings;

import static org.junit.Assert.assertEquals;

import com.wildyqol.warnings.ammo.RangedAmmoWarning;
import com.wildyqol.warnings.charges.ItemChargeWarning;
import com.wildyqol.warnings.magic.MagicSpellbookWarning;
import com.wildyqol.warnings.teleport.TeleportOutWarning;
import org.junit.Test;

public class WarningSeverityTest
{
	@Test
	public void lowAndSuboptimalWarningsAreCautions()
	{
		assertEquals(WarningSeverity.CAUTION, new RangedAmmoWarning(
			RangedAmmoWarning.WarningPriority.LOW, "low").getSeverity());
		assertEquals(WarningSeverity.CAUTION, new RangedAmmoWarning(
			RangedAmmoWarning.WarningPriority.SUBOPTIMAL, "suboptimal").getSeverity());
		assertEquals(WarningSeverity.CAUTION, new ItemChargeWarning(
			ItemChargeWarning.WarningPriority.LOW, "low").getSeverity());
		assertEquals(WarningSeverity.CAUTION, new MagicSpellbookWarning(
			MagicSpellbookWarning.WarningPriority.LOW, "low").getSeverity());
	}

	@Test
	public void missingWrongAndUnknownWarningsAreCritical()
	{
		assertEquals(WarningSeverity.CRITICAL, new RangedAmmoWarning(
			RangedAmmoWarning.WarningPriority.MISSING, "missing").getSeverity());
		assertEquals(WarningSeverity.CRITICAL, new RangedAmmoWarning(
			RangedAmmoWarning.WarningPriority.WRONG, "wrong").getSeverity());
		assertEquals(WarningSeverity.CRITICAL, new ItemChargeWarning(
			ItemChargeWarning.WarningPriority.UNKNOWN, "unknown").getSeverity());
		assertEquals(WarningSeverity.CRITICAL, new MagicSpellbookWarning(
			MagicSpellbookWarning.WarningPriority.MISMATCH, "mismatch").getSeverity());
		assertEquals(WarningSeverity.CRITICAL, new TeleportOutWarning("missing").getSeverity());
	}
}
