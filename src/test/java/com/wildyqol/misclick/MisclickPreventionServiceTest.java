package com.wildyqol.misclick;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.wildyqol.WildyQoLConfig;
import com.wildyqol.WildyQoLConfig.SpecialAttackOrbBlockMode;
import net.runelite.api.gameval.InterfaceID;
import org.junit.Test;

public class MisclickPreventionServiceTest
{
	@Test
	public void emptyVialBlockerDefaultsToEnabled()
	{
		WildyQoLConfig config = new WildyQoLConfig()
		{
		};

		assertTrue(config.emptyVialBlocker());
	}

	@Test
	public void specialAttackOrbBlockerNeverDoesNotBlock()
	{
		assertFalse(MisclickPreventionService.shouldBlockSpecialAttackOrbClick(
			SpecialAttackOrbBlockMode.NEVER,
			true,
			InterfaceID.Orbs.SPECBUTTON
		));
	}

	@Test
	public void specialAttackOrbBlockerPvpBlocksOnlyInPvpArea()
	{
		assertTrue(MisclickPreventionService.shouldBlockSpecialAttackOrbClick(
			SpecialAttackOrbBlockMode.PVP,
			true,
			InterfaceID.Orbs.SPECBUTTON
		));
		assertFalse(MisclickPreventionService.shouldBlockSpecialAttackOrbClick(
			SpecialAttackOrbBlockMode.PVP,
			false,
			InterfaceID.Orbs.SPECBUTTON
		));
	}

	@Test
	public void specialAttackOrbBlockerAlwaysBlocksOutsidePvpArea()
	{
		assertTrue(MisclickPreventionService.shouldBlockSpecialAttackOrbClick(
			SpecialAttackOrbBlockMode.ALWAYS,
			false,
			InterfaceID.Orbs.SPECBUTTON
		));
	}

	@Test
	public void specialAttackOrbBlockerMatchesAllOrbLayouts()
	{
		assertTrue(MisclickPreventionService.isSpecialAttackOrbButton(InterfaceID.Orbs.SPECBUTTON));
		assertTrue(MisclickPreventionService.isSpecialAttackOrbButton(InterfaceID.OrbsNomap.SPECBUTTON));
		assertTrue(MisclickPreventionService.isSpecialAttackOrbButton(InterfaceID.OrbsOsm.SPECBUTTON));
		assertTrue(MisclickPreventionService.isSpecialAttackOrbButton(InterfaceID.OrbsOsmNomap.SPECBUTTON));
	}

	@Test
	public void specialAttackOrbBlockerDoesNotMatchCombatTabSpecialAttack()
	{
		assertFalse(MisclickPreventionService.shouldBlockSpecialAttackOrbClick(
			SpecialAttackOrbBlockMode.ALWAYS,
			true,
			InterfaceID.CombatInterface.SPECIAL_ATTACK
		));
	}
}
