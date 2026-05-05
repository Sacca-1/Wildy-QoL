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
	SERPENTINE_HELM("serpentine helm", "serpentine helm charges")
	{
		@Override
		int threshold(ItemChargeThresholds thresholds)
		{
			return thresholds.serpentineHelmCharges();
		}
	},
	TOXIC_STAFF("toxic SOTD", "toxic SOTD charges")
	{
		@Override
		int threshold(ItemChargeThresholds thresholds)
		{
			return thresholds.toxicStaffCharges();
		}
	},
	ACCURSED_THAMMARONS("Accursed/Thammaron's sceptre", "Accursed/Thammaron's sceptre charges")
	{
		@Override
		int threshold(ItemChargeThresholds thresholds)
		{
			return thresholds.accursedThammaronsCharges();
		}
	},
	CRAWS_WEBWEAVER("Craw's/Webweaver bow", "Craw's/Webweaver bow charges")
	{
		@Override
		int threshold(ItemChargeThresholds thresholds)
		{
			return thresholds.crawsWebweaverCharges();
		}
	},
	URSINE_VIGGORAS("Ursine/Viggora's mace", "Ursine/Viggora's mace charges")
	{
		@Override
		int threshold(ItemChargeThresholds thresholds)
		{
			return thresholds.ursineViggorasCharges();
		}
	},
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
