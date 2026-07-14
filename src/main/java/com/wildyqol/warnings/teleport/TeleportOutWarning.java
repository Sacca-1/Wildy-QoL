package com.wildyqol.warnings.teleport;

import com.wildyqol.warnings.WarningSeverity;
import lombok.Value;

@Value
public class TeleportOutWarning
{
	String text;

	public WarningSeverity getSeverity()
	{
		return WarningSeverity.CRITICAL;
	}
}
