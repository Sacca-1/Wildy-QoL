package com.wildyqol.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SharedStateStoreTest
{
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void sharesCharacterAndMinigameStateAcrossStoreInstances() throws Exception
	{
		Path directory = temporaryFolder.newFolder().toPath();
		SharedStateStore firstClient = store(directory);
		SharedStateStore secondClient = store(directory);

		firstClient.putCharacter("profile-a", "prayer.layout", "layout-a");
		firstClient.putShared("prayer.prayerLayoutPure", "pure-layout");

		assertEquals("layout-a", secondClient.readCharacter("profile-a").get("prayer.layout"));
		assertEquals("pure-layout", secondClient.readShared().get("prayer.prayerLayoutPure"));
	}

	@Test
	public void mergesChangesFromDifferentClientsAndCharacters() throws Exception
	{
		Path directory = temporaryFolder.newFolder().toPath();
		SharedStateStore firstClient = store(directory);
		SharedStateStore secondClient = store(directory);

		firstClient.putCharacter("profile-a", "charges.trackedBowfaCharges", "1000");
		secondClient.putCharacter("profile-b", "charges.trackedTomeOfFireCharges", "500");

		assertEquals("1000", secondClient.readCharacter("profile-a").get("charges.trackedBowfaCharges"));
		assertEquals("500", firstClient.readCharacter("profile-b").get("charges.trackedTomeOfFireCharges"));
	}

	@Test
	public void sameEntryUsesLastWriterWithoutCorruptingOtherEntries() throws Exception
	{
		Path directory = temporaryFolder.newFolder().toPath();
		SharedStateStore firstClient = store(directory);
		SharedStateStore secondClient = store(directory);

		firstClient.putCharacter("profile-a", "prayer.layout", "old-layout");
		firstClient.putCharacter("profile-b", "prayer.layout", "other-layout");
		secondClient.putCharacter("profile-a", "prayer.layout", "new-layout");

		assertEquals("new-layout", firstClient.readCharacter("profile-a").get("prayer.layout"));
		assertEquals("other-layout", secondClient.readCharacter("profile-b").get("prayer.layout"));
	}

	@Test
	public void writesCharacterBatchInOneFileUpdate() throws Exception
	{
		Path directory = temporaryFolder.newFolder().toPath();
		AtomicInteger writes = new AtomicInteger();
		SharedStateStore store = new SharedStateStore(new Gson(), directory, Runnable::run, writes::incrementAndGet);
		Map<String, String> changes = new HashMap<>();
		changes.put("charges.trackedBowfaCharges", "1000");
		changes.put("charges.trackedTomeOfFireCharges", "500");

		store.putCharacter("profile-a", changes);

		assertEquals(1, writes.get());
		assertEquals("1000", store.readCharacter("profile-a").get("charges.trackedBowfaCharges"));
		assertEquals("500", store.readCharacter("profile-a").get("charges.trackedTomeOfFireCharges"));
	}

	@Test
	public void fileDoesNotStoreCharacterDisplayNames() throws Exception
	{
		Path directory = temporaryFolder.newFolder().toPath();
		SharedStateStore store = store(directory);

		store.putCharacter("opaque-profile-key", "prayer.layout", "layout");
		store.flush();

		String json = new String(Files.readAllBytes(directory.resolve("shared-state.json")), StandardCharsets.UTF_8);
		assertFalse(json.contains("Player Name"));
		assertFalse(json.contains("displayName"));
	}

	@Test
	public void flushWaitsForInFlightWriter() throws Exception
	{
		Path directory = temporaryFolder.newFolder().toPath();
		ExecutorService writer = Executors.newSingleThreadExecutor();
		ExecutorService flusher = Executors.newSingleThreadExecutor();
		CountDownLatch writeStarted = new CountDownLatch(1);
		CountDownLatch allowWrite = new CountDownLatch(1);
		CountDownLatch flushStarted = new CountDownLatch(1);
		SharedStateStore store = new SharedStateStore(
			new Gson(),
			directory,
			writer,
			() ->
			{
				writeStarted.countDown();
				await(allowWrite);
			});

		try
		{
			store.putCharacter("profile-a", "prayer.layout", "layout");
			assertTrue(writeStarted.await(5, TimeUnit.SECONDS));

			Future<?> flush = flusher.submit(() ->
			{
				flushStarted.countDown();
				store.flush();
			});
			assertTrue(flushStarted.await(5, TimeUnit.SECONDS));
			try
			{
				flush.get(100, TimeUnit.MILLISECONDS);
				fail("flush returned before the active writer completed");
			}
			catch (TimeoutException expected)
			{
				// Expected: flush is waiting for the write protected by PROCESS_LOCK.
			}

			allowWrite.countDown();
			flush.get(5, TimeUnit.SECONDS);
			assertEquals("layout", store.readCharacter("profile-a").get("prayer.layout"));
		}
		finally
		{
			allowWrite.countDown();
			writer.shutdownNow();
			flusher.shutdownNow();
		}
	}

	@Test
	public void doesNotOverwriteUnsupportedStateVersion() throws Exception
	{
		Path directory = temporaryFolder.newFolder().toPath();
		Path dataFile = directory.resolve("shared-state.json");
		String newerState = "{\"version\":2,\"characters\":{\"profile-a\":{\"future.key\":\"value\"}},\"shared\":{}}";
		Files.write(dataFile, newerState.getBytes(StandardCharsets.UTF_8));
		SharedStateStore store = store(directory);

		store.putCharacter("profile-a", "prayer.layout", "layout");
		store.flush();

		assertEquals(
			newerState,
			new String(Files.readAllBytes(dataFile), StandardCharsets.UTF_8));
	}

	private static void await(CountDownLatch latch)
	{
		try
		{
			latch.await();
		}
		catch (InterruptedException ex)
		{
			Thread.currentThread().interrupt();
			throw new IllegalStateException(ex);
		}
	}

	private SharedStateStore store(Path directory)
	{
		return new SharedStateStore(new Gson(), directory, Runnable::run);
	}
}
