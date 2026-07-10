package com.wildyqol.itemskeptondeath;

import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import java.util.Map;
import net.runelite.api.gameval.ItemID;

final class IkodUntradeableRepairCosts
{
	static final long MANGLED_REPAIR_COST = 500_000L;

	private static final long ANCIENT_SCEPTRE_BASE_REPAIR_COST = 200_000L;

	private static final Map<Integer, RepairCost> COSTS = ImmutableMap.<Integer, RepairCost>builder()
		.put(ItemID.TZHAAR_CAPE_FIRE, fixed(150_000L))
		.put(ItemID.TZHAAR_CAPE_FIRE_TROUVER, fixed(150_000L))
		.put(ItemID.TZHAAR_CAPE_FIRE_BROKEN, fixed(150_000L))
		.put(ItemID.SKILLCAPE_MAX_FIRECAPE, fixed(150_000L))
		.put(ItemID.SKILLCAPE_MAX_FIRECAPE_TROUVER, fixed(150_000L))
		.put(ItemID.SKILLCAPE_MAX_FIRECAPE_BROKEN, fixed(150_000L))
		.put(ItemID.INFERNAL_CAPE, lostAbove20(225_000L))
		.put(ItemID.INFERNAL_CAPE_TROUVER, mangledAbove20(225_000L))
		.put(ItemID.INFERNAL_CAPE_BROKEN, fixed(225_000L))
		.put(ItemID.INFERNAL_CAPE_TROUVER_BROKEN, fixed(225_000L))
		.put(ItemID.INFERNAL_CAPE_TROUVER_MANGLED, mangled())
		.put(ItemID.SKILLCAPE_MAX_INFERNALCAPE, lostAbove20(225_000L))
		.put(ItemID.SKILLCAPE_MAX_INFERNALCAPE_TROUVER, mangledAbove20(225_000L))
		.put(ItemID.SKILLCAPE_MAX_INFERNALCAPE_BROKEN, fixed(225_000L))
		.put(ItemID.SKILLCAPE_MAX_INFERNALCAPE_TROUVER_BROKEN, fixed(225_000L))
		.put(ItemID.SKILLCAPE_MAX_INFERNALCAPE_TROUVER_MANGLED, mangled())
		.put(ItemID.BH_RUNE_POUCH, lostAbove20(500_000L))
		.put(ItemID.BH_RUNE_POUCH_TROUVER, fixed(500_000L))
		.put(ItemID.DIVINE_RUNE_POUCH, lostAbove20(500_000L))
		.put(ItemID.DIVINE_RUNE_POUCH_TROUVER, fixed(500_000L))
		.put(ItemID.AVAS_ASSEMBLER, fixed(240_000L))
		.put(ItemID.AVAS_ASSEMBLER_TROUVER, fixed(240_000L))
		.put(ItemID.AVAS_ASSEMBLER_BROKEN, fixed(240_000L))
		.put(ItemID.SKILLCAPE_MAX_ASSEMBLER, fixed(240_000L))
		.put(ItemID.SKILLCAPE_MAX_ASSEMBLER_TROUVER, fixed(240_000L))
		.put(ItemID.SKILLCAPE_MAX_ASSEMBLER_BROKEN, fixed(240_000L))
		.put(ItemID.AVAS_ASSEMBLER_MASORI, fixed(240_000L))
		.put(ItemID.AVAS_ASSEMBLER_MASORI_TROUVER, fixed(240_000L))
		.put(ItemID.AVAS_ASSEMBLER_MASORI_BROKEN, fixed(240_000L))
		.put(ItemID.SKILLCAPE_MAX_ASSEMBLER_MASORI, fixed(240_000L))
		.put(ItemID.SKILLCAPE_MAX_ASSEMBLER_MASORI_TROUVER, fixed(240_000L))
		.put(ItemID.SKILLCAPE_MAX_ASSEMBLER_MASORI_BROKEN, fixed(240_000L))
		.put(ItemID.MA2_GUTHIX_CAPE, fixed(96_000L))
		.put(ItemID.MA2_GUTHIX_CAPE_TROUVER, fixed(96_000L))
		.put(ItemID.MA2_GUTHIX_CAPE_BROKEN, fixed(96_000L))
		.put(ItemID.MA2_SARADOMIN_CAPE, fixed(96_000L))
		.put(ItemID.MA2_SARADOMIN_CAPE_TROUVER, fixed(96_000L))
		.put(ItemID.MA2_SARADOMIN_CAPE_BROKEN, fixed(96_000L))
		.put(ItemID.MA2_ZAMORAK_CAPE, fixed(96_000L))
		.put(ItemID.MA2_ZAMORAK_CAPE_TROUVER, fixed(96_000L))
		.put(ItemID.MA2_ZAMORAK_CAPE_BROKEN, fixed(96_000L))
		.put(ItemID.SKILLCAPE_MAX_GUTHIX2, fixed(99_000L))
		.put(ItemID.SKILLCAPE_MAX_GUTHIX2_TROUVER, fixed(99_000L))
		.put(ItemID.SKILLCAPE_MAX_GUTHIX2_BROKEN, fixed(99_000L))
		.put(ItemID.SKILLCAPE_MAX_SARADOMIN2, fixed(99_000L))
		.put(ItemID.SKILLCAPE_MAX_SARADOMIN2_TROUVER, fixed(99_000L))
		.put(ItemID.SKILLCAPE_MAX_SARADOMIN2_BROKEN, fixed(99_000L))
		.put(ItemID.SKILLCAPE_MAX_ZAMORAK2, fixed(99_000L))
		.put(ItemID.SKILLCAPE_MAX_ZAMORAK2_TROUVER, fixed(99_000L))
		.put(ItemID.SKILLCAPE_MAX_ZAMORAK2_BROKEN, fixed(99_000L))
		.put(ItemID.BRONZE_PARRYINGDAGGER, fixed(1_000L))
		.put(ItemID.BRONZE_PARRYINGDAGGER_TROUVER, fixed(1_000L))
		.put(ItemID.BRONZE_PARRYINGDAGGER_BROKEN, fixed(1_000L))
		.put(ItemID.IRON_PARRYINGDAGGER, fixed(2_000L))
		.put(ItemID.IRON_PARRYINGDAGGER_TROUVER, fixed(2_000L))
		.put(ItemID.IRON_PARRYINGDAGGER_BROKEN, fixed(2_000L))
		.put(ItemID.STEEL_PARRYINGDAGGER, fixed(2_500L))
		.put(ItemID.STEEL_PARRYINGDAGGER_TROUVER, fixed(2_500L))
		.put(ItemID.STEEL_PARRYINGDAGGER_BROKEN, fixed(2_500L))
		.put(ItemID.BLACK_PARRYINGDAGGER, fixed(5_000L))
		.put(ItemID.BLACK_PARRYINGDAGGER_TROUVER, fixed(5_000L))
		.put(ItemID.BLACK_PARRYINGDAGGER_BROKEN, fixed(5_000L))
		.put(ItemID.MITHRIL_PARRYINGDAGGER, fixed(15_000L))
		.put(ItemID.MITHRIL_PARRYINGDAGGER_TROUVER, fixed(15_000L))
		.put(ItemID.MITHRIL_PARRYINGDAGGER_BROKEN, fixed(15_000L))
		.put(ItemID.ADAMANT_PARRYINGDAGGER, fixed(25_000L))
		.put(ItemID.ADAMANT_PARRYINGDAGGER_TROUVER, fixed(25_000L))
		.put(ItemID.ADAMANT_PARRYINGDAGGER_BROKEN, fixed(25_000L))
		.put(ItemID.RUNE_PARRYINGDAGGER, fixed(35_000L))
		.put(ItemID.RUNE_PARRYINGDAGGER_TROUVER, fixed(35_000L))
		.put(ItemID.RUNE_PARRYINGDAGGER_T, fixed(35_000L))
		.put(ItemID.RUNE_PARRYINGDAGGER_T_TROUVER, fixed(35_000L))
		.put(ItemID.RUNE_PARRYINGDAGGER_BROKEN, fixed(35_000L))
		.put(ItemID.DRAGON_PARRYINGDAGGER, fixed(240_000L))
		.put(ItemID.DRAGON_PARRYINGDAGGER_TROUVER, fixed(240_000L))
		.put(ItemID.DRAGON_PARRYINGDAGGER_T, fixed(240_000L))
		.put(ItemID.DRAGON_PARRYINGDAGGER_T_TROUVER, fixed(240_000L))
		.put(ItemID.DRAGON_PARRYINGDAGGER_BROKEN, fixed(240_000L))
		.put(ItemID.INFERNAL_DEFENDER, fixed(600_000L))
		.put(ItemID.INFERNAL_DEFENDER_TROUVER, fixed(600_000L))
		.put(ItemID.INFERNAL_DEFENDER_GHOMMAL_5, fixed(600_000L))
		.put(ItemID.INFERNAL_DEFENDER_GHOMMAL_5_TROUVER, fixed(600_000L))
		.put(ItemID.INFERNAL_DEFENDER_GHOMMAL_6, fixed(600_000L))
		.put(ItemID.INFERNAL_DEFENDER_GHOMMAL_6_TROUVER, fixed(600_000L))
		.put(ItemID.INFERNAL_DEFENDER_BROKEN, fixed(600_000L))
		.put(ItemID.GAME_PEST_MELEE_HELM, lostAbove20(160_000L))
		.put(ItemID.GAME_PEST_MELEE_HELM_TROUVER, mangledAbove20(160_000L))
		.put(ItemID.LEAGUE_3_VOID_MELEE_HELM, lostAbove20(160_000L))
		.put(ItemID.LEAGUE_3_VOID_MELEE_HELM_TROUVER, mangledAbove20(160_000L))
		.put(ItemID.GAME_PEST_MELEE_HELM_BROKEN, fixed(160_000L))
		.put(ItemID.GAME_PEST_MELEE_HELM_TROUVER_BROKEN, fixed(160_000L))
		.put(ItemID.GAME_PEST_MELEE_HELM_TROUVER_MANGLED, mangled())
		.put(ItemID.GAME_PEST_MAGE_HELM, lostAbove20(160_000L))
		.put(ItemID.GAME_PEST_MAGE_HELM_TROUVER, mangledAbove20(160_000L))
		.put(ItemID.LEAGUE_3_VOID_MAGE_HELM, lostAbove20(160_000L))
		.put(ItemID.LEAGUE_3_VOID_MAGE_HELM_TROUVER, mangledAbove20(160_000L))
		.put(ItemID.GAME_PEST_MAGE_HELM_BROKEN, fixed(160_000L))
		.put(ItemID.GAME_PEST_MAGE_HELM_TROUVER_BROKEN, fixed(160_000L))
		.put(ItemID.GAME_PEST_MAGE_HELM_TROUVER_MANGLED, mangled())
		.put(ItemID.GAME_PEST_ARCHER_HELM, lostAbove20(160_000L))
		.put(ItemID.GAME_PEST_ARCHER_HELM_TROUVER, mangledAbove20(160_000L))
		.put(ItemID.LEAGUE_3_VOID_RANGE_HELM, lostAbove20(160_000L))
		.put(ItemID.LEAGUE_3_VOID_RANGE_HELM_TROUVER, mangledAbove20(160_000L))
		.put(ItemID.GAME_PEST_ARCHER_HELM_BROKEN, fixed(160_000L))
		.put(ItemID.GAME_PEST_ARCHER_HELM_TROUVER_BROKEN, fixed(160_000L))
		.put(ItemID.GAME_PEST_ARCHER_HELM_TROUVER_MANGLED, mangled())
		.put(ItemID.PEST_VOID_KNIGHT_TOP, lostAbove20(180_000L))
		.put(ItemID.PEST_VOID_KNIGHT_TOP_TROUVER, mangledAbove20(180_000L))
		.put(ItemID.LEAGUE_3_VOID_KNIGHT_TOP, lostAbove20(180_000L))
		.put(ItemID.LEAGUE_3_VOID_KNIGHT_TOP_TROUVER, mangledAbove20(180_000L))
		.put(ItemID.PEST_VOID_KNIGHT_TOP_BROKEN, fixed(180_000L))
		.put(ItemID.PEST_VOID_KNIGHT_TOP_TROUVER_BROKEN, fixed(180_000L))
		.put(ItemID.PEST_VOID_KNIGHT_TOP_TROUVER_MANGLED, mangled())
		.put(ItemID.ELITE_VOID_KNIGHT_TOP, lostAbove20(250_000L))
		.put(ItemID.ELITE_VOID_KNIGHT_TOP_TROUVER, mangledAbove20(250_000L))
		.put(ItemID.LEAGUE_3_VOID_KNIGHT_TOP_ELITE, lostAbove20(250_000L))
		.put(ItemID.LEAGUE_3_VOID_KNIGHT_TOP_ELITE_TROUVER, mangledAbove20(250_000L))
		.put(ItemID.ELITE_VOID_KNIGHT_TOP_BROKEN, fixed(250_000L))
		.put(ItemID.ELITE_VOID_KNIGHT_TOP_TROUVER_BROKEN, fixed(250_000L))
		.put(ItemID.ELITE_VOID_KNIGHT_TOP_TROUVER_MANGLED, mangled())
		.put(ItemID.PEST_VOID_KNIGHT_ROBES, lostAbove20(180_000L))
		.put(ItemID.PEST_VOID_KNIGHT_ROBES_TROUVER, mangledAbove20(180_000L))
		.put(ItemID.LEAGUE_3_VOID_KNIGHT_ROBES, lostAbove20(180_000L))
		.put(ItemID.LEAGUE_3_VOID_KNIGHT_ROBES_TROUVER, mangledAbove20(180_000L))
		.put(ItemID.PEST_VOID_KNIGHT_ROBES_BROKEN, fixed(180_000L))
		.put(ItemID.PEST_VOID_KNIGHT_ROBES_TROUVER_BROKEN, fixed(180_000L))
		.put(ItemID.PEST_VOID_KNIGHT_ROBES_TROUVER_MANGLED, mangled())
		.put(ItemID.ELITE_VOID_KNIGHT_ROBES, lostAbove20(250_000L))
		.put(ItemID.ELITE_VOID_KNIGHT_ROBES_TROUVER, mangledAbove20(250_000L))
		.put(ItemID.LEAGUE_3_VOID_KNIGHT_ROBES_ELITE, lostAbove20(250_000L))
		.put(ItemID.LEAGUE_3_VOID_KNIGHT_ROBES_ELITE_TROUVER, mangledAbove20(250_000L))
		.put(ItemID.ELITE_VOID_KNIGHT_ROBES_BROKEN, fixed(250_000L))
		.put(ItemID.ELITE_VOID_KNIGHT_ROBES_TROUVER_BROKEN, fixed(250_000L))
		.put(ItemID.ELITE_VOID_KNIGHT_ROBES_TROUVER_MANGLED, mangled())
		.put(ItemID.PEST_VOID_KNIGHT_GLOVES, lostAbove20(120_000L))
		.put(ItemID.PEST_VOID_KNIGHT_GLOVES_TROUVER, mangledAbove20(120_000L))
		.put(ItemID.LEAGUE_3_VOID_KNIGHT_GLOVES, lostAbove20(120_000L))
		.put(ItemID.LEAGUE_3_VOID_KNIGHT_GLOVES_TROUVER, mangledAbove20(120_000L))
		.put(ItemID.PEST_VOID_KNIGHT_GLOVES_BROKEN, fixed(120_000L))
		.put(ItemID.PEST_VOID_KNIGHT_GLOVES_TROUVER_BROKEN, fixed(120_000L))
		.put(ItemID.PEST_VOID_KNIGHT_GLOVES_TROUVER_MANGLED, mangled())
		.put(ItemID.PEST_VOID_KNIGHT_MACE, fixed(20_000L))
		.put(ItemID.PEST_VOID_KNIGHT_MACE_TROUVER, fixed(20_000L))
		.put(ItemID.PEST_VOID_KNIGHT_MACE_BROKEN, fixed(20_000L))
		.put(ItemID.CASTLEWARS_SWORD_3, fixed(5_000L))
		.put(ItemID.CASTLEWARS_SWORD_3_TROUVER, fixed(5_000L))
		.put(ItemID.CASTLEWARS_SWORD_3_BROKEN, fixed(5_000L))
		.put(ItemID.CASTLEWARS_ARMOUR_BODY_3, fixed(5_000L))
		.put(ItemID.CASTLEWARS_ARMOUR_BODY_3_TROUVER, fixed(5_000L))
		.put(ItemID.CASTLEWARS_ARMOUR_BODY_3_BROKEN, fixed(5_000L))
		.put(ItemID.CASTLEWARS_ARMOUR_LEGS_3, fixed(5_000L))
		.put(ItemID.CASTLEWARS_ARMOUR_LEGS_3_TROUVER, fixed(5_000L))
		.put(ItemID.CASTLEWARS_ARMOUR_LEGS_3_BROKEN, fixed(5_000L))
		.put(ItemID.CASTLEWARS_MED_HELM_3, fixed(5_000L))
		.put(ItemID.CASTLEWARS_MED_HELM_3_TROUVER, fixed(5_000L))
		.put(ItemID.CASTLEWARS_MED_HELM_3_BROKEN, fixed(5_000L))
		.put(ItemID.CASTLEWARS_SHIELD_3, fixed(5_000L))
		.put(ItemID.CASTLEWARS_SHIELD_3_TROUVER, fixed(5_000L))
		.put(ItemID.CASTLEWARS_SHIELD_3_BROKEN, fixed(5_000L))
		.put(ItemID.CASTLEWARS_ARMOUR_SKIRT_3, fixed(5_000L))
		.put(ItemID.CASTLEWARS_ARMOUR_SKIRT_3_TROUVER, fixed(5_000L))
		.put(ItemID.CASTLEWARS_ARMOUR_SKIRT_3_BROKEN, fixed(5_000L))
		.put(ItemID.CASTLEWARS_MAGE_TOP, fixed(5_000L))
		.put(ItemID.CASTLEWARS_MAGE_TOP_TROUVER, fixed(5_000L))
		.put(ItemID.CASTLEWARS_MAGE_TOP_BROKEN, fixed(5_000L))
		.put(ItemID.CASTLEWARS_MAGE_LEGS, fixed(5_000L))
		.put(ItemID.CASTLEWARS_MAGE_LEGS_TROUVER, fixed(5_000L))
		.put(ItemID.CASTLEWARS_MAGE_LEGS_BROKEN, fixed(5_000L))
		.put(ItemID.CASTLEWARS_MAGE_HAT, fixed(5_000L))
		.put(ItemID.CASTLEWARS_MAGE_HAT_TROUVER, fixed(5_000L))
		.put(ItemID.CASTLEWARS_MAGE_HAT_BROKEN, fixed(5_000L))
		.put(ItemID.CASTLEWARS_RANGE_TOP, fixed(5_000L))
		.put(ItemID.CASTLEWARS_RANGE_TOP_TROUVER, fixed(5_000L))
		.put(ItemID.CASTLEWARS_RANGE_TOP_BROKEN, fixed(5_000L))
		.put(ItemID.CASTLEWARS_RANGE_LEGS, fixed(5_000L))
		.put(ItemID.CASTLEWARS_RANGE_LEGS_TROUVER, fixed(5_000L))
		.put(ItemID.CASTLEWARS_RANGE_LEGS_BROKEN, fixed(5_000L))
		.put(ItemID.CASTLEWARS_RANGE_QUIVER, fixed(5_000L))
		.put(ItemID.CASTLEWARS_RANGE_QUIVER_TROUVER, fixed(5_000L))
		.put(ItemID.CASTLEWARS_RANGE_QUIVER_BROKEN, fixed(5_000L))
		.put(ItemID.CASTLEWARS_BOOTS_3, fixed(5_000L))
		.put(ItemID.CASTLEWARS_BOOTS_3_TROUVER, fixed(5_000L))
		.put(ItemID.CASTLEWARS_BOOTS_3_BROKEN, fixed(5_000L))
		.put(ItemID.CASTLEWARS_FULL_HELM_3, fixed(5_000L))
		.put(ItemID.CASTLEWARS_FULL_HELM_3_TROUVER, fixed(5_000L))
		.put(ItemID.CASTLEWARS_FULL_HELM_3_BROKEN, fixed(5_000L))
		.put(ItemID.CASTLEWARS_GUTHIX_HALO, fixed(25_000L))
		.put(ItemID.CASTLEWARS_GUTHIX_HALO_TROUVER, fixed(25_000L))
		.put(ItemID.CASTLEWARS_GUTHIX_HALO_BROKEN, fixed(25_000L))
		.put(ItemID.CASTLEWARS_SARADOMIN_HALO, fixed(25_000L))
		.put(ItemID.CASTLEWARS_SARADOMIN_HALO_TROUVER, fixed(25_000L))
		.put(ItemID.CASTLEWARS_SARADOMIN_HALO_BROKEN, fixed(25_000L))
		.put(ItemID.CASTLEWARS_ZAMORAK_HALO, fixed(25_000L))
		.put(ItemID.CASTLEWARS_ZAMORAK_HALO_TROUVER, fixed(25_000L))
		.put(ItemID.CASTLEWARS_ZAMORAK_HALO_BROKEN, fixed(25_000L))
		.put(ItemID.ARMADYL_HALO, fixed(25_000L))
		.put(ItemID.ARMADYL_HALO_TROUVER, fixed(25_000L))
		.put(ItemID.ARMADYL_HALO_BROKEN, fixed(25_000L))
		.put(ItemID.BANDOS_HALO, fixed(25_000L))
		.put(ItemID.BANDOS_HALO_TROUVER, fixed(25_000L))
		.put(ItemID.BANDOS_HALO_BROKEN, fixed(25_000L))
		.put(ItemID.SEREN_HALO, fixed(25_000L))
		.put(ItemID.SEREN_HALO_TROUVER, fixed(25_000L))
		.put(ItemID.SEREN_HALO_BROKEN, fixed(25_000L))
		.put(ItemID.ZAROS_HALO, fixed(25_000L))
		.put(ItemID.ZAROS_HALO_TROUVER, fixed(25_000L))
		.put(ItemID.ZAROS_HALO_BROKEN, fixed(25_000L))
		.put(ItemID.BRASSICA_HALO, fixed(25_000L))
		.put(ItemID.BRASSICA_HALO_TROUVER, fixed(25_000L))
		.put(ItemID.BRASSICA_HALO_BROKEN, fixed(25_000L))
		.put(ItemID.BARBASSAULT_PENANCE_FIGHTER_HAT, fixed(45_000L))
		.put(ItemID.BARBASSAULT_PENANCE_FIGHTER_HAT_TROUVER, fixed(45_000L))
		.put(ItemID.BARBASSAULT_PENANCE_FIGHTER_HAT_BROKEN, fixed(45_000L))
		.put(ItemID.BARBASSAULT_PENANCE_RANGER_HAT, fixed(45_000L))
		.put(ItemID.BARBASSAULT_PENANCE_RANGER_HAT_TROUVER, fixed(45_000L))
		.put(ItemID.BARBASSAULT_PENANCE_RANGER_HAT_BROKEN, fixed(45_000L))
		.put(ItemID.BARBASSAULT_PENANCE_RUNNER_HAT, fixed(40_005L))
		.put(ItemID.BARBASSAULT_PENANCE_RUNNER_HAT_TROUVER, fixed(40_005L))
		.put(ItemID.BARBASSAULT_PENANCE_RUNNER_HAT_BROKEN, fixed(40_005L))
		.put(ItemID.BARBASSAULT_PENANCE_HEALER_HAT, fixed(45_000L))
		.put(ItemID.BARBASSAULT_PENANCE_HEALER_HAT_TROUVER, fixed(45_000L))
		.put(ItemID.BARBASSAULT_PENANCE_HEALER_HAT_BROKEN, fixed(45_000L))
		.put(ItemID.BARBASSAULT_PENANCE_FIGHTER_TORSO, lostAbove20(150_000L))
		.put(ItemID.BARBASSAULT_PENANCE_FIGHTER_TORSO_TROUVER, mangledAbove20(150_000L))
		.put(ItemID.BARBASSAULT_PENANCE_FIGHTER_TORSO_BROKEN, fixed(150_000L))
		.put(ItemID.BARBASSAULT_PENANCE_FIGHTER_TORSO_TROUVER_BROKEN, fixed(150_000L))
		.put(ItemID.BARBASSAULT_PENANCE_FIGHTER_TORSO_TROUVER_MANGLED, mangled())
		.put(ItemID.BH_BARBASSAULT_PENANCE_FIGHTER_TORSO_CORRUPTED, lostAbove20(150_000L))
		.put(ItemID.BH_BARBASSAULT_PENANCE_FIGHTER_TORSO_CORRUPTED_TROUVER, mangledAbove20(150_000L))
		.put(ItemID.BARBASSAULT_PENANCE_RANGER_LEGS, fixed(20_000L))
		.put(ItemID.BARBASSAULT_PENANCE_RANGER_LEGS_TROUVER, fixed(20_000L))
		.put(ItemID.BARBASSAULT_PENANCE_RANGER_LEGS_BROKEN, fixed(20_000L))
		.put(ItemID.BARRONITE_MACE, fixed(10_500L))
		.put(ItemID.BARRONITE_MACE_TROUVER, fixed(10_500L))
		.put(ItemID.BARRONITE_MACE_BROKEN, fixed(10_500L))
		.put(ItemID.ANCIENT_SCEPTRE, ancientSceptreLostAbove20())
		.put(ItemID.ANCIENT_SCEPTRE_TROUVER, ancientSceptreMangledAbove20())
		.put(ItemID.ANCIENT_SCEPTRE_BLOOD, ancientSceptreLostAbove20())
		.put(ItemID.ANCIENT_SCEPTRE_BLOOD_TROUVER, ancientSceptreMangledAbove20())
		.put(ItemID.ANCIENT_SCEPTRE_BLOOD_BROKEN, ancientSceptre())
		.put(ItemID.ANCIENT_SCEPTRE_BLOOD_TROUVER_BROKEN, ancientSceptre())
		.put(ItemID.ANCIENT_SCEPTRE_BLOOD_TROUVER_MANGLED, mangled())
		.put(ItemID.ANCIENT_SCEPTRE_SMOKE, ancientSceptreLostAbove20())
		.put(ItemID.ANCIENT_SCEPTRE_SMOKE_TROUVER, ancientSceptreMangledAbove20())
		.put(ItemID.ANCIENT_SCEPTRE_SMOKE_BROKEN, ancientSceptre())
		.put(ItemID.ANCIENT_SCEPTRE_SMOKE_TROUVER_BROKEN, ancientSceptre())
		.put(ItemID.ANCIENT_SCEPTRE_SMOKE_TROUVER_MANGLED, mangled())
		.put(ItemID.ANCIENT_SCEPTRE_ICE, ancientSceptreLostAbove20())
		.put(ItemID.ANCIENT_SCEPTRE_ICE_TROUVER, ancientSceptreMangledAbove20())
		.put(ItemID.ANCIENT_SCEPTRE_ICE_BROKEN, ancientSceptre())
		.put(ItemID.ANCIENT_SCEPTRE_ICE_TROUVER_BROKEN, ancientSceptre())
		.put(ItemID.ANCIENT_SCEPTRE_ICE_TROUVER_MANGLED, mangled())
		.put(ItemID.ANCIENT_SCEPTRE_SHADOW, ancientSceptreLostAbove20())
		.put(ItemID.ANCIENT_SCEPTRE_SHADOW_TROUVER, ancientSceptreMangledAbove20())
		.put(ItemID.ANCIENT_SCEPTRE_SHADOW_BROKEN, ancientSceptre())
		.put(ItemID.ANCIENT_SCEPTRE_SHADOW_TROUVER_BROKEN, ancientSceptre())
		.put(ItemID.ANCIENT_SCEPTRE_SHADOW_TROUVER_MANGLED, mangled())
		.put(ItemID.DIZANAS_QUIVER_UNCHARGED, lostAbove20(270_000L))
		.put(ItemID.DIZANAS_QUIVER_UNCHARGED_TROUVER, mangledAbove20(270_000L))
		.put(ItemID.DIZANAS_QUIVER_CHARGED, lostAbove20(270_000L))
		.put(ItemID.DIZANAS_QUIVER_CHARGED_TROUVER, mangledAbove20(270_000L))
		.put(ItemID.DIZANAS_QUIVER_BROKEN, fixed(270_000L))
		.put(ItemID.DIZANAS_QUIVER_TROUVER_BROKEN, fixed(270_000L))
		.put(ItemID.DIZANAS_QUIVER_TROUVER_MANGLED, mangled())
		.put(ItemID.DIZANAS_QUIVER_INFINITE, lostAbove20(400_000L))
		.put(ItemID.DIZANAS_QUIVER_INFINITE_TROUVER, mangledAbove20(400_000L))
		.put(ItemID.DIZANAS_QUIVER_INFINITE_BROKEN, fixed(400_000L))
		.put(ItemID.DIZANAS_QUIVER_INFINITE_TROUVER_BROKEN, fixed(400_000L))
		.put(ItemID.DIZANAS_QUIVER_INFINITE_TROUVER_MANGLED, mangled())
		.put(ItemID.SKILLCAPE_MAX_DIZANAS, lostAbove20(400_000L))
		.put(ItemID.SKILLCAPE_MAX_DIZANAS_TROUVER, mangledAbove20(400_000L))
		.put(ItemID.SKILLCAPE_MAX_DIZANAS_BROKEN, fixed(400_000L))
		.put(ItemID.SKILLCAPE_MAX_DIZANAS_TROUVER_BROKEN, fixed(400_000L))
		.put(ItemID.SKILLCAPE_MAX_DIZANAS_TROUVER_MANGLED, mangled())
		.build();

	private IkodUntradeableRepairCosts()
	{
	}

	static long getRepairCost(int canonicalItemId, String itemName, boolean aboveLevel20, ItemPriceLookup itemPriceLookup)
	{
		String normalizedName = normalize(itemName);
		if (isMangledName(normalizedName))
		{
			return MANGLED_REPAIR_COST;
		}

		RepairCost repairCost = COSTS.get(canonicalItemId);
		if (repairCost == null)
		{
			return 0L;
		}

		return repairCost.cost(aboveLevel20, itemPriceLookup);
	}

	static boolean hasRepairCost(int canonicalItemId, String itemName)
	{
		return COSTS.containsKey(canonicalItemId) || isDisplayedBrokenOrMangled(itemName);
	}

	static boolean isDisplayedBrokenOrMangled(String itemName)
	{
		String normalizedName = normalize(itemName);
		return normalizedName.endsWith(" (broken)")
			|| isMangledName(normalizedName);
	}

	private static boolean isMangledName(String normalizedName)
	{
		return normalizedName.endsWith(" (mangled)");
	}

	private static String normalize(String itemName)
	{
		return itemName == null ? "" : itemName.toLowerCase(Locale.ROOT);
	}

	private static RepairCost fixed(long brokenCost)
	{
		return new RepairCost(brokenCost, false, false, false);
	}

	private static RepairCost mangledAbove20(long brokenCost)
	{
		return new RepairCost(brokenCost, true, false, false);
	}

	private static RepairCost lostAbove20(long brokenCost)
	{
		return new RepairCost(brokenCost, false, true, false);
	}

	private static RepairCost mangled()
	{
		return fixed(MANGLED_REPAIR_COST);
	}

	private static RepairCost ancientSceptre()
	{
		return new RepairCost(ANCIENT_SCEPTRE_BASE_REPAIR_COST, false, false, true);
	}

	private static RepairCost ancientSceptreLostAbove20()
	{
		return new RepairCost(ANCIENT_SCEPTRE_BASE_REPAIR_COST, false, true, true);
	}

	private static RepairCost ancientSceptreMangledAbove20()
	{
		return new RepairCost(ANCIENT_SCEPTRE_BASE_REPAIR_COST, true, false, true);
	}

	private static final class RepairCost
	{
		private final long brokenCost;
		private final boolean mangledAbove20;
		private final boolean lostAbove20;
		private final boolean addAncientStaffPrice;

		private RepairCost(long brokenCost, boolean mangledAbove20, boolean lostAbove20, boolean addAncientStaffPrice)
		{
			this.brokenCost = brokenCost;
			this.mangledAbove20 = mangledAbove20;
			this.lostAbove20 = lostAbove20;
			this.addAncientStaffPrice = addAncientStaffPrice;
		}

		private long cost(boolean aboveLevel20, ItemPriceLookup itemPriceLookup)
		{
			if (aboveLevel20 && mangledAbove20)
			{
				return MANGLED_REPAIR_COST;
			}

			if (aboveLevel20 && lostAbove20)
			{
				return 0L;
			}

			if (!addAncientStaffPrice)
			{
				return brokenCost;
			}

			return brokenCost + itemPriceLookup.getItemPrice(ItemID.STAFF_OF_ZAROS);
		}
	}

	interface ItemPriceLookup
	{
		long getItemPrice(int itemId);
	}
}
