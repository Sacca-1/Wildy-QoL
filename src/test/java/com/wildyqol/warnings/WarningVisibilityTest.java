package com.wildyqol.warnings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
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
	public void showsOutsidePvp()
	{
		WarningVisibility<RangedAmmoWarning> visibility = new WarningVisibility<>(RangedAmmoWarning::getText);

		assertTrue(visibility.update(Optional.of(warning), true, false, false).isPresent());
	}

	@Test
	public void suppressesWhenDisabled()
	{
		WarningVisibility<RangedAmmoWarning> visibility = new WarningVisibility<>(RangedAmmoWarning::getText);

		assertFalse(visibility.update(Optional.of(warning), false, false, false).isPresent());
	}

	@Test
	public void showsForGraceTicksAfterEnteringPvp()
	{
		WarningVisibility<RangedAmmoWarning> visibility = new WarningVisibility<>(RangedAmmoWarning::getText);
		visibility.update(Optional.of(warning), true, false, false);

		assertTrue(visibility.update(Optional.of(warning), true, true, false).isPresent());
		for (int i = 0; i < WarningVisibility.PVP_GRACE_TICKS; i++)
		{
			visibility.update(Optional.of(warning), true, true, true);
		}

		assertFalse(visibility.update(Optional.of(warning), true, true, false).isPresent());
	}

	@Test
	public void resumesWhenLeavingPvp()
	{
		WarningVisibility<RangedAmmoWarning> visibility = new WarningVisibility<>(RangedAmmoWarning::getText);
		visibility.update(Optional.of(warning), true, false, false);
		visibility.update(Optional.of(warning), true, true, false);
		for (int i = 0; i < WarningVisibility.PVP_GRACE_TICKS; i++)
		{
			visibility.update(Optional.of(warning), true, true, true);
		}

		assertFalse(visibility.update(Optional.of(warning), true, true, false).isPresent());
		assertTrue(visibility.update(Optional.of(warning), true, false, false).isPresent());
	}

	@Test
	public void keepsMultipleWarningsVisible()
	{
		WarningVisibility<RangedAmmoWarning> visibility = new WarningVisibility<>(RangedAmmoWarning::getText);
		List<RangedAmmoWarning> warnings = ImmutableList.of(
			warning,
			new RangedAmmoWarning(RangedAmmoWarning.WarningPriority.LOW, "Low bolts: 42/100"));

		List<RangedAmmoWarning> visibleWarnings = visibility.update(warnings, true, false, false);

		assertEquals(2, visibleWarnings.size());
		assertEquals("Missing ammo: dragon arrows", visibleWarnings.get(0).getText());
		assertEquals("Low bolts: 42/100", visibleWarnings.get(1).getText());
	}
}
