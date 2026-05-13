package com.wildyqol.warnings.charges;

import java.util.Map;
import java.util.Set;
import lombok.Value;

@Value
public class ItemChargeState
{
	Set<ItemChargeKind> chargedItems;
	Set<ItemChargeKind> unchargedItems;
	Map<ItemChargeKind, Integer> charges;
}
