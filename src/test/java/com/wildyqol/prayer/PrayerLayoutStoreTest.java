package com.wildyqol.prayer;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.wildyqol.persistence.SharedStateStore;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.Executor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PrayerLayoutStoreTest
{
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void savesAccountAndMinigameLayoutsOnlyInSharedState() throws Exception
	{
		SharedStateStore sharedStateStore = sharedStateStore(temporaryFolder.newFolder().toPath());
		PrayerLayoutStore store = new PrayerLayoutStore(new Gson(), sharedStateStore);
		PrayerLayoutContext account = PrayerLayoutContext.account("profile-a", "Player");
		PrayerLayoutContext pure = PrayerLayoutContext.minigame(PrayerLayoutBuild.PURE);
		PrayerLayoutSnapshot accountLayout = snapshot("prayer_order_book_0", "1,2,3");
		PrayerLayoutSnapshot pureLayout = snapshot("prayer_hidden_book_0_2", "true");

		store.save(account, accountLayout);
		store.save(pure, pureLayout);
		store.flush();

		assertEquals(accountLayout, store.load(account));
		assertEquals(pureLayout, store.load(pure));
		assertEquals(
			new Gson().toJson(accountLayout),
			sharedStateStore.readCharacter("profile-a").get("prayer.layout"));
		assertEquals(
			new Gson().toJson(pureLayout),
			sharedStateStore.readShared().get("prayer.prayerLayoutPure"));
	}

	private PrayerLayoutSnapshot snapshot(String key, String value)
	{
		return new PrayerLayoutSnapshot(Collections.singletonMap(key, value));
	}

	private SharedStateStore sharedStateStore(Path directory) throws Exception
	{
		Constructor<SharedStateStore> constructor = SharedStateStore.class.getDeclaredConstructor(
			Gson.class,
			Path.class,
			Executor.class);
		constructor.setAccessible(true);
		return constructor.newInstance(new Gson(), directory, (Executor) Runnable::run);
	}
}
