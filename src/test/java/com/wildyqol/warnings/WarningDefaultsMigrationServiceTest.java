package com.wildyqol.warnings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.wildyqol.WildyQoLConfig;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class WarningDefaultsMigrationServiceTest
{
	@Test
	public void warningThresholdsUseNewDefaults()
	{
		WildyQoLConfig config = new WildyQoLConfig()
		{
		};

		assertEquals(100, config.atlatlDartMinimum());
		assertEquals(50, config.boltMinimum());
		assertEquals(50, config.javelinMinimum());
		assertEquals(20, config.arrowMinimum());
		assertEquals(300, config.bowfaChargeMinimum());
		assertEquals(300, config.toxicStaffChargeMinimum());
		assertEquals(300, config.serpentineHelmChargeMinimum());
		assertEquals(50, config.surgeMinimum());
		assertEquals(50, config.iceSpellMinimum());
	}

	@Test
	public void migratesOldDefaultsAndPreservesCustomizedValues()
	{
		MapStore store = new MapStore();
		store.values.put("atlatlDartMinimum", "250");
		store.values.put("boltMinimum", "75");
		store.values.put("javelinMinimum", "100");
		store.values.put("arrowMinimum", "100");
		store.values.put("bowfaChargeMinimum", "250");
		store.values.put("toxicStaffChargeMinimum", "500");
		store.values.put("serpentineHelmChargeMinimum", "1000");
		store.values.put("surgeMinimum", "100");
		store.values.put("iceSpellMinimum", "100");

		new WarningDefaultsMigrationService(store).migrate();

		assertEquals("100", store.values.get("atlatlDartMinimum"));
		assertEquals("75", store.values.get("boltMinimum"));
		assertEquals("50", store.values.get("javelinMinimum"));
		assertEquals("20", store.values.get("arrowMinimum"));
		assertEquals("300", store.values.get("bowfaChargeMinimum"));
		assertEquals("300", store.values.get("toxicStaffChargeMinimum"));
		assertEquals("1000", store.values.get("serpentineHelmChargeMinimum"));
		assertEquals("50", store.values.get("surgeMinimum"));
		assertEquals("50", store.values.get("iceSpellMinimum"));
		assertEquals("true", store.values.get(WarningDefaultsMigrationService.MIGRATION_KEY));
	}

	@Test
	public void leavesMissingValuesForNewInstallationsUnchanged()
	{
		MapStore store = new MapStore();

		new WarningDefaultsMigrationService(store).migrate();

		assertEquals(1, store.values.size());
		assertEquals("true", store.values.get(WarningDefaultsMigrationService.MIGRATION_KEY));
	}

	@Test
	public void runsOnlyOncePerConfigurationProfile()
	{
		MapStore store = new MapStore();
		store.values.put(WarningDefaultsMigrationService.MIGRATION_KEY, "true");
		store.values.put("arrowMinimum", "100");

		new WarningDefaultsMigrationService(store).migrate();

		assertEquals("100", store.values.get("arrowMinimum"));
		assertFalse(store.wasWritten);
	}

	private static class MapStore implements WarningDefaultsMigrationService.ConfigurationStore
	{
		private final Map<String, String> values = new HashMap<>();
		private boolean wasWritten;

		@Override
		public String get(String key)
		{
			return values.get(key);
		}

		@Override
		public void set(String key, String value)
		{
			values.put(key, value);
			wasWritten = true;
		}
	}
}
