package com.wildyqol.warnings;

public class WarningEligibility
{
	private final boolean onlyWarnAtBank;
	private final boolean inPvp;
	private final boolean eligibleOutsidePvp;

	WarningEligibility(boolean onlyWarnAtBank, boolean inPvp, boolean eligibleOutsidePvp)
	{
		this.onlyWarnAtBank = onlyWarnAtBank;
		this.inPvp = inPvp;
		this.eligibleOutsidePvp = eligibleOutsidePvp;
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
}
