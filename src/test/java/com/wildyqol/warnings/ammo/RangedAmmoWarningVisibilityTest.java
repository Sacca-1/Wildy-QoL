package com.wildyqol.warnings.ammo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class RangedAmmoWarningVisibilityTest
{
	private final RangedAmmoWarning warning = new RangedAmmoWarning(
		RangedAmmoWarning.WarningPriority.MISSING,
		"Missing ammo: dragon arrows");

	@Test
	public void showsOutsidePvp()
	{
		RangedAmmoWarningVisibility visibility = new RangedAmmoWarningVisibility();

		assertTrue(visibility.update(Optional.of(warning), true, false, false).isPresent());
	}

	@Test
	public void suppressesWhenDisabled()
	{
		RangedAmmoWarningVisibility visibility = new RangedAmmoWarningVisibility();

		assertFalse(visibility.update(Optional.of(warning), false, false, false).isPresent());
	}

	@Test
	public void showsForGraceTicksAfterEnteringPvp()
	{
		RangedAmmoWarningVisibility visibility = new RangedAmmoWarningVisibility();
		visibility.update(Optional.of(warning), true, false, false);

		assertTrue(visibility.update(Optional.of(warning), true, true, false).isPresent());
		for (int i = 0; i < RangedAmmoWarningVisibility.PVP_GRACE_TICKS; i++)
		{
			visibility.update(Optional.of(warning), true, true, true);
		}

		assertFalse(visibility.update(Optional.of(warning), true, true, false).isPresent());
	}

	@Test
	public void resumesWhenLeavingPvp()
	{
		RangedAmmoWarningVisibility visibility = new RangedAmmoWarningVisibility();
		visibility.update(Optional.of(warning), true, false, false);
		visibility.update(Optional.of(warning), true, true, false);
		for (int i = 0; i < RangedAmmoWarningVisibility.PVP_GRACE_TICKS; i++)
		{
			visibility.update(Optional.of(warning), true, true, true);
		}

		assertFalse(visibility.update(Optional.of(warning), true, true, false).isPresent());
		assertTrue(visibility.update(Optional.of(warning), true, false, false).isPresent());
	}

	@Test
	public void keepsMultipleWarningsVisible()
	{
		RangedAmmoWarningVisibility visibility = new RangedAmmoWarningVisibility();
		List<RangedAmmoWarning> warnings = ImmutableList.of(
			warning,
			new RangedAmmoWarning(RangedAmmoWarning.WarningPriority.LOW, "Low bolts: 42/100"));

		List<RangedAmmoWarning> visibleWarnings = visibility.update(warnings, true, false, false);

		assertEquals(2, visibleWarnings.size());
		assertEquals("Missing ammo: dragon arrows", visibleWarnings.get(0).getText());
		assertEquals("Low bolts: 42/100", visibleWarnings.get(1).getText());
	}
}
