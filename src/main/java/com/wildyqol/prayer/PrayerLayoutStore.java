package com.wildyqol.prayer;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.wildyqol.persistence.SharedStateStore;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
final class PrayerLayoutStore
{
	static final String CONFIG_GROUP = "wildyqol";
	static final String ACCOUNT_STORAGE_KEY = "prayerLayoutAccount";
	private static final String SHARED_CHARACTER_KEY = "prayer.layout";
	private static final String SHARED_LAYOUT_PREFIX = "prayer.";

	private final Gson gson;
	private final SharedStateStore sharedStateStore;

	@Inject
	PrayerLayoutStore(Gson gson, SharedStateStore sharedStateStore)
	{
		this.gson = gson;
		this.sharedStateStore = sharedStateStore;
	}

	PrayerLayoutSnapshot load(PrayerLayoutContext context)
	{
		Map<String, String> sharedState = context.isAccount()
			? sharedStateStore.readCharacter(context.getProfileKey())
			: sharedStateStore.readShared();
		return parse(sharedState.get(sharedKey(context)));
	}

	private PrayerLayoutSnapshot parse(String json)
	{
		if (json == null || json.isEmpty())
		{
			return null;
		}
		try
		{
			PrayerLayoutSnapshot snapshot = gson.fromJson(json, PrayerLayoutSnapshot.class);
			if (snapshot != null && snapshot.isSupportedVersion())
			{
				return new PrayerLayoutSnapshot(snapshot.getValues());
			}
		}
		catch (JsonParseException ex)
		{
			log.warn("Unable to read saved prayer layout", ex);
		}

		return null;
	}

	void save(PrayerLayoutContext context, PrayerLayoutSnapshot snapshot)
	{
		String json = gson.toJson(snapshot);
		writeShared(context, sharedKey(context), json);
	}

	void flush()
	{
		sharedStateStore.flush();
	}

	private String sharedKey(PrayerLayoutContext context)
	{
		return context.isAccount() ? SHARED_CHARACTER_KEY : SHARED_LAYOUT_PREFIX + context.getStorageKey();
	}

	private void writeShared(PrayerLayoutContext context, String key, String value)
	{
		if (context.isAccount())
		{
			sharedStateStore.putCharacter(context.getProfileKey(), key, value);
		}
		else
		{
			sharedStateStore.putShared(key, value);
		}
	}
}
