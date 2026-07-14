package com.wildyqol.warnings.magic;

import com.wildyqol.warnings.WarningSeverity;
import lombok.Value;

@Value
public class MagicSpellbookWarning
{
	WarningPriority priority;
	String text;

	public WarningSeverity getSeverity()
	{
		return priority == WarningPriority.LOW ? WarningSeverity.CAUTION : WarningSeverity.CRITICAL;
	}

	public enum WarningPriority
	{
		MISMATCH,
		MISSING,
		LOW
	}
}
