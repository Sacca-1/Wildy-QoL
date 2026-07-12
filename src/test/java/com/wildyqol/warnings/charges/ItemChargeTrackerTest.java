package com.wildyqol.warnings.charges;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.gson.Gson;
import com.wildyqol.persistence.SharedStateStore;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.Executor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ItemChargeTrackerTest
{
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void parsesCommaAndDotFormattedQuantities()
	{
		assertEquals(1234, ItemChargeTracker.parseQuantity("1,234"));
		assertEquals(1234, ItemChargeTracker.parseQuantity("1.234"));
		assertEquals(500, ItemChargeTracker.parseQuantity("500"));
	}

	@Test
	public void sharedStateIsAuthoritativeAcrossProfiles() throws Exception
	{
		SharedStateStore store = sharedStateStore(temporaryFolder.newFolder().toPath());
		store.putCharacter("profile-a", "charges.trackedBowfaCharges", "1234");
		ItemChargeTracker tracker = new ItemChargeTracker(null, store);

		tracker.refreshFromSharedState("profile-a");
		EnumMap<ItemChargeKind, Integer> charges = new EnumMap<>(ItemChargeKind.class);
		tracker.addKnownCharges(charges);
		assertEquals(Integer.valueOf(1234), charges.get(ItemChargeKind.BOWFA));

		tracker.markUncharged(ItemChargeKind.BOWFA);
		assertEquals("1234", store.readCharacter("profile-a").get("charges.trackedBowfaCharges"));
		tracker.shutDown();
		assertEquals("0", store.readCharacter("profile-a").get("charges.trackedBowfaCharges"));

		tracker.refreshFromSharedState("profile-b");
		charges.clear();
		tracker.addKnownCharges(charges);
		assertFalse(charges.containsKey(ItemChargeKind.BOWFA));
	}

	@Test
	public void profileExitPublishesChargesBeforeReturning() throws Exception
	{
		Path directory = temporaryFolder.newFolder().toPath();
		List<Runnable> delayedWrites = new ArrayList<>();
		SharedStateStore outgoingStore = sharedStateStore(directory, delayedWrites::add);
		SharedStateStore incomingStore = sharedStateStore(directory);
		ItemChargeTracker tracker = new ItemChargeTracker(null, outgoingStore);

		tracker.refreshFromSharedState("profile-a");
		tracker.markUncharged(ItemChargeKind.BOWFA);
		tracker.refreshFromSharedState(null);

		assertEquals(
			"0",
			incomingStore.readCharacter("profile-a").get("charges.trackedBowfaCharges"));
	}

	private SharedStateStore sharedStateStore(Path directory) throws Exception
	{
		return sharedStateStore(directory, Runnable::run);
	}

	private SharedStateStore sharedStateStore(Path directory, Executor executor) throws Exception
	{
		Constructor<SharedStateStore> constructor = SharedStateStore.class.getDeclaredConstructor(
			Gson.class,
			Path.class,
			Executor.class);
		constructor.setAccessible(true);
		return constructor.newInstance(new Gson(), directory, executor);
	}
}
