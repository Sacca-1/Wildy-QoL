package com.wildyqol.warnings.charges;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ItemChargeTrackerTest
{
	@Test
	public void parsesCommaAndDotFormattedQuantities()
	{
		assertEquals(1234, ItemChargeTracker.parseQuantity("1,234"));
		assertEquals(1234, ItemChargeTracker.parseQuantity("1.234"));
		assertEquals(500, ItemChargeTracker.parseQuantity("500"));
	}
}
