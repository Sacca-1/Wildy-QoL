package com.wildyqol.warnings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WarningEligibilityServiceTest
{
	@Test
	public void recentlyLeftPvpLastsOneHundredTicks()
	{
		assertTrue(WarningEligibilityService.isRecentlyLeftPvp(false, 100, 0));
		assertFalse(WarningEligibilityService.isRecentlyLeftPvp(false, 101, 0));
	}

	@Test
	public void currentPvpAndNoPriorPvpDoNotCountAsRecentlyLeft()
	{
		assertFalse(WarningEligibilityService.isRecentlyLeftPvp(true, 100, 100));
		assertFalse(WarningEligibilityService.isRecentlyLeftPvp(false, 100, Integer.MIN_VALUE));
	}
}
