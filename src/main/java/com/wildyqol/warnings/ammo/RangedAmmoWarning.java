package com.wildyqol.warnings.ammo;

import com.wildyqol.warnings.WarningSeverity;
import lombok.Value;

@Value
public class RangedAmmoWarning
{
	WarningPriority priority;
	String text;

	public WarningSeverity getSeverity()
	{
		return priority == WarningPriority.LOW || priority == WarningPriority.SUBOPTIMAL
			? WarningSeverity.CAUTION
			: WarningSeverity.CRITICAL;
	}

	public enum WarningPriority
	{
		MISSING,
		WRONG,
		LOW,
		SUBOPTIMAL
	}
}
