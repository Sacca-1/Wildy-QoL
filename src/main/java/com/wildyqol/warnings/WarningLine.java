package com.wildyqol.warnings;

import lombok.Value;

@Value
public class WarningLine
{
	WarningSeverity severity;
	String text;
}
