package com.wildyqol.warnings;

import com.wildyqol.WildyQoLConfig.WarningDisplayMode;

public class WarningEligibility
{
	private final WarningDisplayMode warningDisplayMode;
	private final boolean inPvp;
	private final boolean eligibleOutsidePvp;
	private final boolean equipmentWarningsVisible;

	WarningEligibility(
		WarningDisplayMode warningDisplayMode,
		boolean inPvp,
		boolean eligibleOutsidePvp,
		boolean equipmentWarningsVisible)
	{
		this.warningDisplayMode = warningDisplayMode;
		this.inPvp = inPvp;
		this.eligibleOutsidePvp = eligibleOutsidePvp;
		this.equipmentWarningsVisible = equipmentWarningsVisible;
	}

	public WarningDisplayMode getWarningDisplayMode()
	{
		return warningDisplayMode;
	}

	public boolean isInPvp()
	{
		return inPvp;
	}

	public boolean isEligibleOutsidePvp()
	{
		return eligibleOutsidePvp;
	}

	public boolean isEquipmentWarningsVisible()
	{
		return equipmentWarningsVisible;
	}
}
