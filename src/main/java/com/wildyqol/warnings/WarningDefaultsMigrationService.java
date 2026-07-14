package com.wildyqol.warnings;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

@Singleton
public class WarningDefaultsMigrationService
{
	static final String CONFIG_GROUP = "wildyqol";
	static final String MIGRATION_KEY = "warningThresholdDefaultsMigratedV1";

	private static final String[][] THRESHOLD_MIGRATIONS =
	{
		{"atlatlDartMinimum", "250", "100"},
		{"boltMinimum", "100", "50"},
		{"javelinMinimum", "100", "50"},
		{"arrowMinimum", "100", "20"},
		{"bowfaChargeMinimum", "250", "300"},
		{"toxicStaffChargeMinimum", "500", "300"},
		{"serpentineHelmChargeMinimum", "500", "300"},
		{"surgeMinimum", "100", "50"},
		{"iceSpellMinimum", "100", "50"}
	};

	private final ConfigurationStore configurationStore;

	@Inject
	WarningDefaultsMigrationService(ConfigManager configManager)
	{
		this(new ConfigManagerStore(configManager));
	}

	WarningDefaultsMigrationService(ConfigurationStore configurationStore)
	{
		this.configurationStore = configurationStore;
	}

	public void migrate()
	{
		if (Boolean.parseBoolean(configurationStore.get(MIGRATION_KEY)))
		{
			return;
		}

		for (String[] migration : THRESHOLD_MIGRATIONS)
		{
			String key = migration[0];
			String oldDefault = migration[1];
			String newDefault = migration[2];
			if (oldDefault.equals(configurationStore.get(key)))
			{
				configurationStore.set(key, newDefault);
			}
		}

		configurationStore.set(MIGRATION_KEY, "true");
	}

	interface ConfigurationStore
	{
		String get(String key);

		void set(String key, String value);
	}

	private static class ConfigManagerStore implements ConfigurationStore
	{
		private final ConfigManager configManager;

		private ConfigManagerStore(ConfigManager configManager)
		{
			this.configManager = configManager;
		}

		@Override
		public String get(String key)
		{
			return configManager.getConfiguration(CONFIG_GROUP, key);
		}

		@Override
		public void set(String key, String value)
		{
			configManager.setConfiguration(CONFIG_GROUP, key, value);
		}
	}
}
