package com.wildyqol.warnings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BankProximityServiceTest
{
	@Test
	public void bankActionCounts()
	{
		assertTrue(BankProximityService.hasBankAction(new String[] {"Talk-to", "Bank"}));
		assertTrue(BankProximityService.hasBankAction(new String[] {"<col=ffff00>Bank</col>"}));
	}

	@Test
	public void nonBankActionsDoNotCount()
	{
		assertFalse(BankProximityService.hasBankAction(new String[] {"Deposit", "Collect"}));
		assertFalse(BankProximityService.hasBankAction(new String[] {null, "Banker"}));
		assertFalse(BankProximityService.hasBankAction(null));
	}
}
