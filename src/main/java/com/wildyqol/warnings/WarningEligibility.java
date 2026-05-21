package com.wildyqol.warnings;

public class WarningEligibility
{
	private final boolean onlyWarnAtBank;
	private final boolean inPvp;
	private final boolean eligibleOutsidePvp;
	private final boolean equipmentWarningsVisible;

	WarningEligibility(boolean onlyWarnAtBank, boolean inPvp, boolean eligibleOutsidePvp, boolean equipmentWarningsVisible)
	{
		this.onlyWarnAtBank = onlyWarnAtBank;
		this.inPvp = inPvp;
		this.eligibleOutsidePvp = eligibleOutsidePvp;
		this.equipmentWarningsVisible = equipmentWarningsVisible;
	}

	public boolean isOnlyWarnAtBank()
	{
		return onlyWarnAtBank;
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
