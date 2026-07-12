package com.wildyqol.prayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class PrayerLayoutSnapshotTest
{
	@Test
	public void keepsOrderAndHiddenKeysForAllPrayerBooks()
	{
		Map<String, String> values = new HashMap<>();
		values.put("prayer_order_book_0", "1,2,3");
		values.put("prayer_hidden_book_0_2", "true");
		values.put("prayer_order_book_1", "3,2,1");
		values.put("prayer_hidden_book_1_3", "true");
		values.put("prayerIndicator", "true");

		PrayerLayoutSnapshot snapshot = new PrayerLayoutSnapshot(values);

		assertEquals(4, snapshot.getValues().size());
		assertFalse(snapshot.getValues().containsKey("prayerIndicator"));
		assertTrue(snapshot.getValues().containsKey("prayer_order_book_0"));
		assertTrue(snapshot.getValues().containsKey("prayer_order_book_1"));
	}

	@Test
	public void roundTripsVersionedJson()
	{
		Map<String, String> values = new HashMap<>();
		values.put("prayer_order_book_0", "1,2,3");
		values.put("prayer_hidden_book_0_2", "true");
		PrayerLayoutSnapshot original = new PrayerLayoutSnapshot(values);
		Gson gson = new Gson();

		PrayerLayoutSnapshot decoded = gson.fromJson(gson.toJson(original), PrayerLayoutSnapshot.class);

		assertTrue(decoded.isSupportedVersion());
		assertEquals(original, decoded);
	}
}
