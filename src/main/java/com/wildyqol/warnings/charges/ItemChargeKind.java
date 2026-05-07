package com.wildyqol.warnings.charges;

enum ItemChargeKind
{
	BOWFA("Bowfa", "Bowfa charges")
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
	TOME_OF_FIRE("tome of fire", "tome of fire charges")
	{
		@Override
		int threshold(ItemChargeThresholds thresholds)
		{
			return thresholds.tomeCharges();
		}
	},
	TOME_OF_WATER("tome of water", "tome of water charges")
	{
		@Override
		int threshold(ItemChargeThresholds thresholds)
		{
			return thresholds.tomeCharges();
		}
	},
	TOME_OF_EARTH("tome of earth", "tome of earth charges")
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
