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
	SERPENTINE_HELM("serpentine helm", null),
	TOXIC_STAFF("toxic SOTD", null),
	ACCURSED_THAMMARONS("Accursed/Thammaron's sceptre", null),
	CRAWS_WEBWEAVER("Craw's/Webweaver bow", null),
	URSINE_VIGGORAS("Ursine/Viggora's mace", null),
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

	int threshold(ItemChargeThresholds thresholds)
	{
		return 0;
	}
}
