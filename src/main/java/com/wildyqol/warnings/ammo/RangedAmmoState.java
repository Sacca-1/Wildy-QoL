package com.wildyqol.warnings.ammo;

import java.util.Map;
import java.util.Set;
import lombok.Value;

@Value
public class RangedAmmoState
{
	Set<RangedAmmoRequirement> requirements;
	Map<Integer, Integer> ammoCounts;
	boolean chargedBowfa;
	boolean inactiveBowfa;
	int bowfaCharges;
}
