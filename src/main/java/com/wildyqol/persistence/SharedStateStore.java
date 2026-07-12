package com.wildyqol.persistence;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;

@Slf4j
@Singleton
public class SharedStateStore
{
	private static final int CURRENT_VERSION = 1;
	private static final String DATA_DIRECTORY = "wildy-qol";
	private static final String DATA_FILE = "shared-state.json";
	private static final String LOCK_FILE = "shared-state.lock";
	private static final ReentrantLock PROCESS_LOCK = new ReentrantLock();

	private final Gson gson;
	private final Path dataDirectory;
	private final Path dataFile;
	private final Path lockFile;
	private final Executor writer;
	private final Runnable beforeWrite;
	private final Map<PendingKey, String> pendingChanges = new ConcurrentHashMap<>();
	private final AtomicBoolean flushScheduled = new AtomicBoolean();

	@Inject
	private SharedStateStore(Gson gson)
	{
		this(
			gson,
			new java.io.File(RuneLite.RUNELITE_DIR, DATA_DIRECTORY).toPath(),
			Executors.newSingleThreadExecutor(runnable ->
			{
				Thread thread = new Thread(runnable, "wildy-qol-state-writer");
				thread.setDaemon(true);
				return thread;
			}));
	}

	SharedStateStore(Gson gson, Path dataDirectory, Executor writer)
	{
		this(gson, dataDirectory, writer, () -> { });
	}

	SharedStateStore(Gson gson, Path dataDirectory, Executor writer, Runnable beforeWrite)
	{
		this.gson = gson;
		this.dataDirectory = dataDirectory;
		this.dataFile = dataDirectory.resolve(DATA_FILE);
		this.lockFile = dataDirectory.resolve(LOCK_FILE);
		this.writer = writer;
		this.beforeWrite = beforeWrite;
	}

	public Map<String, String> readCharacter(String profileKey)
	{
		if (profileKey == null)
		{
			return Collections.emptyMap();
		}

		flush();
		SharedState state = readState();
		Map<String, String> values = state.characters.get(profileKey);
		return values == null ? Collections.emptyMap() : new HashMap<>(values);
	}

	public Map<String, String> readShared()
	{
		flush();
		return new HashMap<>(readState().shared);
	}

	public void putCharacter(String profileKey, String key, String value)
	{
		if (profileKey == null || key == null || value == null)
		{
			return;
		}

		pendingChanges.put(PendingKey.character(profileKey, key), value);
		scheduleFlush();
	}

	public void putCharacter(String profileKey, Map<String, String> values)
	{
		if (profileKey == null || values == null || values.isEmpty())
		{
			return;
		}

		boolean changed = false;
		for (Map.Entry<String, String> entry : values.entrySet())
		{
			if (entry.getKey() != null && entry.getValue() != null)
			{
				pendingChanges.put(PendingKey.character(profileKey, entry.getKey()), entry.getValue());
				changed = true;
			}
		}

		if (changed)
		{
			scheduleFlush();
		}
	}

	public void putShared(String key, String value)
	{
		if (key == null || value == null)
		{
			return;
		}

		pendingChanges.put(PendingKey.shared(key), value);
		scheduleFlush();
	}

	public void flush()
	{
		while (flushAll())
		{
			PROCESS_LOCK.lock();
			try
			{
				if (pendingChanges.isEmpty())
				{
					return;
				}
			}
			finally
			{
				PROCESS_LOCK.unlock();
			}
		}
	}

	private boolean flushAll()
	{
		while (!pendingChanges.isEmpty())
		{
			if (!flushPendingOnce())
			{
				return false;
			}
		}
		return true;
	}

	private void scheduleFlush()
	{
		if (!flushScheduled.compareAndSet(false, true))
		{
			return;
		}

		writer.execute(() ->
		{
			boolean success = false;
			try
			{
				success = flushAll();
			}
			finally
			{
				flushScheduled.set(false);
				if (success && !pendingChanges.isEmpty())
				{
					scheduleFlush();
				}
			}
		});
	}

	private boolean flushPendingOnce()
	{
		Map<PendingKey, String> changes = Collections.emptyMap();
		PROCESS_LOCK.lock();
		try
		{
			changes = drainPendingChanges();
			if (changes.isEmpty())
			{
				return true;
			}

			Files.createDirectories(dataDirectory);
			try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				 FileLock ignored = channel.lock())
			{
				SharedState state = readStateFile();
				for (Map.Entry<PendingKey, String> entry : changes.entrySet())
				{
					entry.getKey().apply(state, entry.getValue());
				}
				beforeWrite.run();
				writeStateFile(state);
			}
			return true;
		}
		catch (IOException | RuntimeException ex)
		{
			for (Map.Entry<PendingKey, String> entry : changes.entrySet())
			{
				pendingChanges.putIfAbsent(entry.getKey(), entry.getValue());
			}
			log.warn("Unable to save shared Wildy QoL state", ex);
			return false;
		}
		finally
		{
			PROCESS_LOCK.unlock();
		}
	}

	private Map<PendingKey, String> drainPendingChanges()
	{
		Map<PendingKey, String> drained = new HashMap<>();
		for (Map.Entry<PendingKey, String> entry : pendingChanges.entrySet())
		{
			if (pendingChanges.remove(entry.getKey(), entry.getValue()))
			{
				drained.put(entry.getKey(), entry.getValue());
			}
		}
		return drained;
	}

	private SharedState readState()
	{
		PROCESS_LOCK.lock();
		try
		{
			Files.createDirectories(dataDirectory);
			try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				 FileLock ignored = channel.lock())
			{
				return readStateFile();
			}
		}
		catch (IOException | RuntimeException ex)
		{
			log.warn("Unable to read shared Wildy QoL state", ex);
			return new SharedState();
		}
		finally
		{
			PROCESS_LOCK.unlock();
		}
	}

	private SharedState readStateFile() throws IOException
	{
		if (!Files.exists(dataFile))
		{
			return new SharedState();
		}

		try (Reader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8))
		{
			SharedState state = gson.fromJson(reader, SharedState.class);
			if (state == null || state.version != CURRENT_VERSION)
			{
				throw new UnsupportedStateVersionException(state == null ? null : state.version);
			}
			state.normalize();
			return state;
		}
		catch (JsonParseException ex)
		{
			log.warn("Unable to parse shared Wildy QoL state", ex);
			return new SharedState();
		}
	}

	private void writeStateFile(SharedState state) throws IOException
	{
		Path temporaryFile = Files.createTempFile(dataDirectory, "shared-state-", ".tmp");
		try
		{
			try (Writer output = Files.newBufferedWriter(
				temporaryFile,
				StandardCharsets.UTF_8,
				StandardOpenOption.TRUNCATE_EXISTING))
			{
				gson.toJson(state, output);
			}

			try
			{
				Files.move(
					temporaryFile,
					dataFile,
					StandardCopyOption.ATOMIC_MOVE,
					StandardCopyOption.REPLACE_EXISTING);
			}
			catch (AtomicMoveNotSupportedException ex)
			{
				Files.move(temporaryFile, dataFile, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		finally
			{
				Files.deleteIfExists(temporaryFile);
			}
		}

	private static final class UnsupportedStateVersionException extends IOException
	{
		private UnsupportedStateVersionException(Integer version)
		{
			super("Unsupported shared Wildy QoL state version: " + version);
		}
	}

	private static final class SharedState
	{
		private int version = CURRENT_VERSION;
		private Map<String, Map<String, String>> characters = new HashMap<>();
		private Map<String, String> shared = new HashMap<>();

		private void normalize()
		{
			if (characters == null)
			{
				characters = new HashMap<>();
			}
			if (shared == null)
			{
				shared = new HashMap<>();
			}
		}
	}

	private static final class PendingKey
	{
		private final String profileKey;
		private final String key;

		private PendingKey(String profileKey, String key)
		{
			this.profileKey = profileKey;
			this.key = key;
		}

		private static PendingKey character(String profileKey, String key)
		{
			return new PendingKey(profileKey, key);
		}

		private static PendingKey shared(String key)
		{
			return new PendingKey(null, key);
		}

		private void apply(SharedState state, String value)
		{
			if (profileKey == null)
			{
				state.shared.put(key, value);
				return;
			}

			state.characters.computeIfAbsent(profileKey, ignored -> new HashMap<>()).put(key, value);
		}

		@Override
		public boolean equals(Object other)
		{
			if (this == other)
			{
				return true;
			}
			if (!(other instanceof PendingKey))
			{
				return false;
			}
			PendingKey that = (PendingKey) other;
			return Objects.equals(profileKey, that.profileKey) && key.equals(that.key);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(profileKey, key);
		}
	}
}
