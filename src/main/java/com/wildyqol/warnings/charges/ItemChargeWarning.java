package com.wildyqol.warnings.charges;

import com.wildyqol.warnings.WarningSeverity;
import lombok.Value;

@Value
public class ItemChargeWarning
{
	WarningPriority priority;
	String text;

	public WarningSeverity getSeverity()
	{
		return priority == WarningPriority.LOW ? WarningSeverity.CAUTION : WarningSeverity.CRITICAL;
	}

	public enum WarningPriority
	{
		MISSING,
		UNKNOWN,
		LOW
	}
}
