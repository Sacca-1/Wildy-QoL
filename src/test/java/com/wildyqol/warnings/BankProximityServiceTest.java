package com.wildyqol.warnings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Proxy;
import net.runelite.api.ObjectComposition;
import org.junit.Test;

public class BankProximityServiceTest
{
	@Test
	public void bankActionCounts()
	{
		assertTrue(BankProximityService.hasBankAction(new String[] {"Talk-to", "Bank"}));
		assertTrue(BankProximityService.hasBankAction(new String[] {"<col=ffff00>Bank</col>"}));
		assertTrue(BankProximityService.hasBankObjectAction(
			objectComposition("Bank chest", new String[] {"<col=ffff00>Use</col>"})));
	}

	@Test
	public void nonBankActionsDoNotCount()
	{
		assertFalse(BankProximityService.hasBankAction(new String[] {"Deposit", "Collect"}));
		assertFalse(BankProximityService.hasBankAction(new String[] {"Use"}));
		assertFalse(BankProximityService.hasBankObjectAction(objectComposition("Chest", new String[] {"Use"})));
		assertFalse(BankProximityService.hasBankAction(new String[] {null, "Banker"}));
		assertFalse(BankProximityService.hasBankAction(null));
	}

	private ObjectComposition objectComposition(String name, String[] actions)
	{
		return (ObjectComposition) Proxy.newProxyInstance(
			ObjectComposition.class.getClassLoader(),
			new Class<?>[] {ObjectComposition.class},
			(proxy, method, args) ->
			{
				if ("getName".equals(method.getName()))
				{
					return name;
				}

				if ("getActions".equals(method.getName()))
				{
					return actions;
				}

				if (method.getReturnType().equals(int.class))
				{
					return 0;
				}

				return null;
			});
	}
}
