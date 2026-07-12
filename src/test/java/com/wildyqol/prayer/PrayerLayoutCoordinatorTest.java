package com.wildyqol.prayer;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class PrayerLayoutCoordinatorTest
{
	private static final PrayerLayoutContext ACCOUNT = PrayerLayoutContext.account("account-one", "Player");
	private static final PrayerLayoutContext PURE = PrayerLayoutContext.minigame(PrayerLayoutBuild.PURE);

	@Test
	public void seedsMissingContextWithoutRestoreNotification()
	{
		FakeBackend backend = new FakeBackend(snapshot("prayer_order_book_0", "1,2,3"));
		PrayerLayoutCoordinator coordinator = new PrayerLayoutCoordinator(backend);

		coordinator.transitionTo(ACCOUNT);

		assertEquals(backend.live, backend.saved.get(ACCOUNT));
		assertEquals(0, backend.applyCount);
		assertEquals(0, backend.restoreCount);
	}

	@Test
	public void savesOutgoingAndRestoresIncomingContext()
	{
		PrayerLayoutSnapshot accountLayout = snapshot("prayer_order_book_0", "1,2,3");
		PrayerLayoutSnapshot editedAccountLayout = snapshot("prayer_order_book_0", "3,2,1");
		PrayerLayoutSnapshot pureLayout = snapshot("prayer_hidden_book_0_2", "true");
		FakeBackend backend = new FakeBackend(accountLayout);
		backend.saved.put(ACCOUNT, accountLayout);
		backend.saved.put(PURE, pureLayout);
		PrayerLayoutCoordinator coordinator = new PrayerLayoutCoordinator(backend);

		coordinator.transitionTo(ACCOUNT);
		backend.live = editedAccountLayout;
		coordinator.transitionTo(PURE);

		assertEquals(editedAccountLayout, backend.saved.get(ACCOUNT));
		assertEquals(pureLayout, backend.live);
		assertEquals(1, backend.applyCount);
		assertEquals(1, backend.restoreCount);
	}

	@Test
	public void restoresAccountAfterLeavingMinigameContext()
	{
		PrayerLayoutSnapshot accountLayout = snapshot("prayer_order_book_0", "1,2,3");
		PrayerLayoutSnapshot pureLayout = snapshot("prayer_hidden_book_0_2", "true");
		PrayerLayoutSnapshot editedPureLayout = snapshot("prayer_hidden_book_0_7", "true");
		FakeBackend backend = new FakeBackend(accountLayout);
		backend.saved.put(ACCOUNT, accountLayout);
		backend.saved.put(PURE, pureLayout);
		PrayerLayoutCoordinator coordinator = new PrayerLayoutCoordinator(backend);

		coordinator.transitionTo(ACCOUNT);
		coordinator.transitionTo(PURE);
		backend.live = editedPureLayout;
		coordinator.transitionTo(ACCOUNT);

		assertEquals(editedPureLayout, backend.saved.get(PURE));
		assertEquals(accountLayout, backend.live);
		assertEquals(2, backend.applyCount);
		assertEquals(2, backend.restoreCount);
	}

	@Test
	public void continuouslySavesCurrentContextAfterEdits()
	{
		FakeBackend backend = new FakeBackend(snapshot("prayer_order_book_0", "1,2,3"));
		PrayerLayoutCoordinator coordinator = new PrayerLayoutCoordinator(backend);
		coordinator.transitionTo(ACCOUNT);
		PrayerLayoutSnapshot edited = snapshot("prayer_hidden_book_1_7", "true");
		backend.live = edited;

		coordinator.saveCurrent();

		assertEquals(edited, backend.saved.get(ACCOUNT));
	}

	@Test
	public void noOpRestoreDoesNotNotify()
	{
		PrayerLayoutSnapshot layout = snapshot("prayer_order_book_0", "1,2,3");
		FakeBackend backend = new FakeBackend(layout);
		backend.saved.put(ACCOUNT, layout);
		PrayerLayoutCoordinator coordinator = new PrayerLayoutCoordinator(backend);

		coordinator.transitionTo(ACCOUNT);

		assertEquals(0, backend.applyCount);
		assertEquals(0, backend.restoreCount);
	}

	@Test
	public void unchangedContextIsNotSavedAgain()
	{
		PrayerLayoutSnapshot layout = snapshot("prayer_order_book_0", "1,2,3");
		FakeBackend backend = new FakeBackend(layout);
		backend.saved.put(ACCOUNT, layout);
		PrayerLayoutCoordinator coordinator = new PrayerLayoutCoordinator(backend);
		coordinator.transitionTo(ACCOUNT);
		int saveCount = backend.saveCount;

		coordinator.leaveCurrent();

		assertEquals(saveCount, backend.saveCount);
	}

	@Test
	public void clearedCoordinatorDoesNotSaveInactiveContext()
	{
		FakeBackend backend = new FakeBackend(snapshot("prayer_order_book_0", "1,2,3"));
		PrayerLayoutCoordinator coordinator = new PrayerLayoutCoordinator(backend);
		coordinator.transitionTo(ACCOUNT);
		backend.saved.clear();

		coordinator.clear();
		coordinator.saveCurrent();

		assertEquals(0, backend.saved.size());
	}

	private static PrayerLayoutSnapshot snapshot(String key, String value)
	{
		Map<String, String> values = new HashMap<>();
		values.put(key, value);
		return new PrayerLayoutSnapshot(values);
	}

	private static final class FakeBackend implements PrayerLayoutCoordinator.Backend
	{
		private final Map<PrayerLayoutContext, PrayerLayoutSnapshot> saved = new HashMap<>();
		private PrayerLayoutSnapshot live;
		private int applyCount;
		private int restoreCount;
		private int saveCount;

		private FakeBackend(PrayerLayoutSnapshot live)
		{
			this.live = live;
		}

		@Override
		public PrayerLayoutSnapshot capture()
		{
			return live;
		}

		@Override
		public PrayerLayoutSnapshot load(PrayerLayoutContext context)
		{
			return saved.get(context);
		}

		@Override
		public void save(PrayerLayoutContext context, PrayerLayoutSnapshot snapshot)
		{
			saveCount++;
			saved.put(context, snapshot);
		}

		@Override
		public boolean apply(PrayerLayoutSnapshot target, PrayerLayoutSnapshot current)
		{
			applyCount++;
			live = target;
			return true;
		}

		@Override
		public void restored(PrayerLayoutContext context)
		{
			restoreCount++;
		}
	}
}
