package com.wildyqol.warnings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.wildyqol.WildyQoLConfig.WarningDisplayMode;
import com.wildyqol.warnings.ammo.RangedAmmoWarning;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class WarningVisibilityTest
{
	private final RangedAmmoWarning warning = new RangedAmmoWarning(
		RangedAmmoWarning.WarningPriority.MISSING,
		"Missing ammo: dragon arrows");

	@Test
	public void alwaysShowsWhenBankGateDisabled()
	{
		WarningVisibility<RangedAmmoWarning> visibility = new WarningVisibility<>(RangedAmmoWarning::getText);

		assertTrue(visibility.update(Optional.of(warning), true, WarningDisplayMode.ALWAYS, false, false, false).isPresent());
		for (int i = 0; i < WarningVisibility.PVP_GRACE_TICKS + 1; i++)
		{
			assertTrue(visibility.update(Optional.of(warning), true, WarningDisplayMode.ALWAYS, true, false, true).isPresent());
		}
	}

	@Test
	public void pvpAreaModeOnlyShowsInsidePvp()
	{
		WarningVisibility<RangedAmmoWarning> visibility = new WarningVisibility<>(RangedAmmoWarning::getText);

		assertFalse(visibility.update(Optional.of(warning), true, WarningDisplayMode.PVP_AREA, false, true, false).isPresent());
		assertTrue(visibility.update(Optional.of(warning), true, WarningDisplayMode.PVP_AREA, true, false, false).isPresent());
	}

	@Test
	public void suppressesWhenDisabled()
	{
		WarningVisibility<RangedAmmoWarning> visibility = new WarningVisibility<>(RangedAmmoWarning::getText);

		assertFalse(visibility.update(Optional.of(warning), false, WarningDisplayMode.BANK, false, true, false).isPresent());
	}

	@Test
	public void hidesOutsidePvpWhenBankGateEnabledAndNotEligible()
	{
		WarningVisibility<RangedAmmoWarning> visibility = new WarningVisibility<>(RangedAmmoWarning::getText);

		assertFalse(visibility.update(Optional.of(warning), true, WarningDisplayMode.BANK, false, false, false).isPresent());
	}

	@Test
	public void showsOutsidePvpWhenBankGateEnabledAndEligible()
	{
		WarningVisibility<RangedAmmoWarning> visibility = new WarningVisibility<>(RangedAmmoWarning::getText);

		assertTrue(visibility.update(Optional.of(warning), true, WarningDisplayMode.BANK, false, true, false).isPresent());
	}

	@Test
	public void showsForGraceTicksAfterEnteringPvp()
	{
		WarningVisibility<RangedAmmoWarning> visibility = new WarningVisibility<>(RangedAmmoWarning::getText);
		visibility.update(Optional.of(warning), true, WarningDisplayMode.BANK, false, true, false);

		assertTrue(visibility.update(Optional.of(warning), true, WarningDisplayMode.BANK, true, false, false).isPresent());
		for (int i = 0; i < WarningVisibility.PVP_GRACE_TICKS; i++)
		{
			visibility.update(Optional.of(warning), true, WarningDisplayMode.BANK, true, false, true);
		}

		assertFalse(visibility.update(Optional.of(warning), true, WarningDisplayMode.BANK, true, false, false).isPresent());
	}

	@Test
	public void doesNotStartPvpGraceWithoutExistingVisibleWarning()
	{
		WarningVisibility<RangedAmmoWarning> visibility = new WarningVisibility<>(RangedAmmoWarning::getText);
		visibility.update(Optional.of(warning), true, WarningDisplayMode.BANK, false, false, false);

		assertFalse(visibility.update(Optional.of(warning), true, WarningDisplayMode.BANK, true, false, false).isPresent());
	}

	@Test
	public void resumesWhenLeavingPvp()
	{
		WarningVisibility<RangedAmmoWarning> visibility = new WarningVisibility<>(RangedAmmoWarning::getText);
		visibility.update(Optional.of(warning), true, WarningDisplayMode.BANK, false, true, false);
		visibility.update(Optional.of(warning), true, WarningDisplayMode.BANK, true, false, false);
		for (int i = 0; i < WarningVisibility.PVP_GRACE_TICKS; i++)
		{
			visibility.update(Optional.of(warning), true, WarningDisplayMode.BANK, true, false, true);
		}

		assertFalse(visibility.update(Optional.of(warning), true, WarningDisplayMode.BANK, true, false, false).isPresent());
		assertTrue(visibility.update(Optional.of(warning), true, WarningDisplayMode.BANK, false, true, false).isPresent());
	}

	@Test
	public void keepsMultipleWarningsVisible()
	{
		WarningVisibility<RangedAmmoWarning> visibility = new WarningVisibility<>(RangedAmmoWarning::getText);
		List<RangedAmmoWarning> warnings = ImmutableList.of(
			warning,
			new RangedAmmoWarning(RangedAmmoWarning.WarningPriority.LOW, "Low ammo: bolts 42/100"));

		List<RangedAmmoWarning> visibleWarnings = visibility.update(
			warnings,
			true,
			WarningDisplayMode.BANK,
			false,
			true,
			false);

		assertEquals(2, visibleWarnings.size());
		assertEquals("Missing ammo: dragon arrows", visibleWarnings.get(0).getText());
		assertEquals("Low ammo: bolts 42/100", visibleWarnings.get(1).getText());
	}
}
