package com.wildyqol.warnings.charges;

import lombok.Value;

@Value
public class ItemChargeWarning
{
	WarningPriority priority;
	String text;

	public enum WarningPriority
	{
		MISSING,
		UNKNOWN,
		LOW
	}
}
