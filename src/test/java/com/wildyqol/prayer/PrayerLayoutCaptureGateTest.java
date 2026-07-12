package com.wildyqol.prayer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PrayerLayoutCaptureGateTest
{
	@Test
	public void rejectsConfigChangesFromNewProfileUntilActivated()
	{
		PrayerLayoutCaptureGate gate = new PrayerLayoutCaptureGate();
		gate.activate(1L);

		assertNull(gate.token(2L));
	}

	@Test
	public void invalidatesQueuedCaptureAcrossProfileChange()
	{
		PrayerLayoutCaptureGate gate = new PrayerLayoutCaptureGate();
		gate.activate(1L);
		PrayerLayoutCaptureGate.Token oldCapture = gate.token(1L);
		assertNotNull(oldCapture);

		gate.invalidate();
		gate.activate(2L);

		assertFalse(gate.isCurrent(oldCapture, 2L));
		PrayerLayoutCaptureGate.Token newCapture = gate.token(2L);
		assertNotNull(newCapture);
		assertTrue(gate.isCurrent(newCapture, 2L));
	}
}
