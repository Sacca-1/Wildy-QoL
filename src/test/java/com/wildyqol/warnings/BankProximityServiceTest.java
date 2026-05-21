package com.wildyqol.warnings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Proxy;
import net.runelite.api.ObjectComposition;
import net.runelite.api.coords.WorldPoint;
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

	@Test
	public void bankDistanceIsTenTilesOnSamePlane()
	{
		WorldPoint playerLocation = new WorldPoint(3200, 3200, 0);

		assertTrue(BankProximityService.isWithinBankDistance(playerLocation, new WorldPoint(3210, 3200, 0)));
		assertTrue(BankProximityService.isWithinBankDistance(playerLocation, new WorldPoint(3210, 3210, 0)));
		assertFalse(BankProximityService.isWithinBankDistance(playerLocation, new WorldPoint(3211, 3200, 0)));
		assertFalse(BankProximityService.isWithinBankDistance(playerLocation, new WorldPoint(3210, 3200, 1)));
	}

	@Test
	public void pvpRelevantBanksUseTwentyTileRadius()
	{
		assertTrue(BankProximityService.isPvpRelevantBankLocation(new WorldPoint(2443, 3083, 0)));
		assertTrue(BankProximityService.isPvpRelevantBankLocation(new WorldPoint(2463, 3103, 0)));
		assertTrue(BankProximityService.isPvpRelevantBankLocation(new WorldPoint(3164, 3487, 0)));
		assertTrue(BankProximityService.isPvpRelevantBankLocation(new WorldPoint(3130, 3631, 0)));
		assertTrue(BankProximityService.isPvpRelevantBankLocation(new WorldPoint(3094, 3493, 0)));
		assertTrue(BankProximityService.isPvpRelevantBankLocation(new WorldPoint(1253, 3741, 0)));
		assertTrue(BankProximityService.isPvpRelevantBankLocation(new WorldPoint(1324, 3824, 0)));

		assertFalse(BankProximityService.isPvpRelevantBankLocation(new WorldPoint(2464, 3103, 0)));
		assertFalse(BankProximityService.isPvpRelevantBankLocation(new WorldPoint(2443, 3083, 1)));
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
