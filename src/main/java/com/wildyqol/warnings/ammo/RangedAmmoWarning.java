package com.wildyqol.warnings.ammo;

import lombok.Value;

@Value
public class RangedAmmoWarning
{
	WarningPriority priority;
	String text;

	public enum WarningPriority
	{
		MISSING,
		WRONG,
		LOW,
		SUBOPTIMAL
	}
}
