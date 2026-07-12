package com.wildyqol.prayer;

final class PrayerLayoutCoordinator
{
	interface Backend
	{
		PrayerLayoutSnapshot capture();

		PrayerLayoutSnapshot load(PrayerLayoutContext context);

		void save(PrayerLayoutContext context, PrayerLayoutSnapshot snapshot);

		boolean apply(PrayerLayoutSnapshot target, PrayerLayoutSnapshot current);

		void restored(PrayerLayoutContext context);
	}

	private final Backend backend;
	private PrayerLayoutContext currentContext;
	private PrayerLayoutSnapshot persistedSnapshot;

	PrayerLayoutCoordinator(Backend backend)
	{
		this.backend = backend;
	}

	void transitionTo(PrayerLayoutContext nextContext)
	{
		if (nextContext == null || nextContext.equals(currentContext))
		{
			return;
		}

		saveCurrent();

		PrayerLayoutSnapshot current = backend.capture();
		PrayerLayoutSnapshot saved = backend.load(nextContext);
		currentContext = nextContext;

		if (saved == null)
		{
			backend.save(nextContext, current);
			persistedSnapshot = current;
			return;
		}

		persistedSnapshot = saved;
		if (!saved.equals(current) && backend.apply(saved, current))
		{
			backend.restored(nextContext);
		}
	}

	void saveCurrent()
	{
		if (currentContext != null)
		{
			PrayerLayoutSnapshot current = backend.capture();
			if (!current.equals(persistedSnapshot))
			{
				backend.save(currentContext, current);
				persistedSnapshot = current;
			}
		}
	}

	void leaveCurrent()
	{
		saveCurrent();
		currentContext = null;
		persistedSnapshot = null;
	}

	void clear()
	{
		currentContext = null;
		persistedSnapshot = null;
	}
}
