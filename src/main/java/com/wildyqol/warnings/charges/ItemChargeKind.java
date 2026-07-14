package com.wildyqol.warnings.charges;

enum ItemChargeKind
{
	BOWFA("Bowfa", "Bowfa")
	{
		@Override
		int threshold(ItemChargeThresholds thresholds)
		{
			return thresholds.bowfaCharges();
		}
	},
	SERPENTINE_HELM("serpentine helm", "serpentine helm")
	{
		@Override
		int threshold(ItemChargeThresholds thresholds)
		{
			return thresholds.serpentineHelmCharges();
		}
	},
	TOXIC_STAFF("toxic staff of the dead", "toxic staff of the dead")
	{
		@Override
		int threshold(ItemChargeThresholds thresholds)
		{
			return thresholds.toxicStaffCharges();
		}
	},
	ACCURSED_THAMMARONS("Accursed or Thammaron's sceptre", null),
	CRAWS_WEBWEAVER("Craw's or Webweaver bow", null),
	URSINE_VIGGORAS("Ursine or Viggora's mace", null),
	RING_OF_SUFFERING("ring of suffering recoil", null),
	TOME_OF_FIRE("tome of fire", "tome of fire")
	{
		@Override
		int threshold(ItemChargeThresholds thresholds)
		{
			return thresholds.tomeCharges();
		}
	},
	TOME_OF_WATER("tome of water", "tome of water")
	{
		@Override
		int threshold(ItemChargeThresholds thresholds)
		{
			return thresholds.tomeCharges();
		}
	},
	TOME_OF_EARTH("tome of earth", "tome of earth")
	{
		@Override
		int threshold(ItemChargeThresholds thresholds)
		{
			return thresholds.tomeCharges();
		}
	},
	DRAGONFIRE_SHIELD("dragonfire shield", null),
	DRAGONFIRE_WARD("dragonfire ward", null),
	ANCIENT_WYVERN_SHIELD("ancient wyvern shield", null);

	private final String missingText;
	private final String lowText;

	ItemChargeKind(String missingText, String lowText)
	{
		this.missingText = missingText;
		this.lowText = lowText;
	}

	String getMissingText()
	{
		return missingText;
	}

	String getLowText()
	{
		return lowText;
	}

	boolean supportsLowWarning()
	{
		return lowText != null;
	}

	boolean hasEstimatedCharges()
	{
		return this == SERPENTINE_HELM
			|| this == TOXIC_STAFF
			|| this == TOME_OF_FIRE
			|| this == TOME_OF_WATER
			|| this == TOME_OF_EARTH;
	}

	boolean requiresManualTracking()
	{
		return this == BOWFA || hasEstimatedCharges();
	}

	int threshold(ItemChargeThresholds thresholds)
	{
		return 0;
	}
}
