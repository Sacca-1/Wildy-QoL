package com.wildyqol.warnings.magic;

import lombok.Value;

@Value
public class MagicSpellbookWarning
{
	WarningPriority priority;
	String text;

	public enum WarningPriority
	{
		MISMATCH,
		MISSING,
		LOW
	}
}
