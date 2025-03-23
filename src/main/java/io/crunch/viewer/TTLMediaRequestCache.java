package io.crunch.viewer;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * TTLMediaRequestCache is an implementation of {@link MediaRequestCache} that caches media IDs with a time-to-live (TTL).
 * It automatically removes expired entries using a scheduled cleanup task.
 *
 * <p>This cache is application-scoped, meaning it exists for the lifetime of the application.
 * It uses a {@link LinkedHashMap} with access-order to maintain an LRU-style behavior.
 * </p>
 *
 * <p><strong>Key Features:</strong>
 * <ul>
 *     <li>Stores media IDs mapped to tokens with a default TTL of 5 seconds.</li>
 *     <li>Scheduled cleanup runs every 1 second to remove expired entries.</li>
 *     <li>Thread-safe operations using synchronization.</li>
 *     <li>Uses {@link PreDestroy} annotation to gracefully shut down the cleanup task.</li>
 * </ul>
 * </p>
 */
@ApplicationScoped
public class TTLMediaRequestCache implements MediaRequestCache {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Cache map storing tokens mapped to media IDs with expiration timestamps.
     * Uses {@link LinkedHashMap} in LRU mode (access order) for efficient removal of least recently accessed elements.
     */
    private final Map<String, CacheEntry> cache;

    /**
     * Default time-to-live (TTL) for cache entries in seconds.
     */
    private static final long DEFAULT_TTL_IN_SEC = 5;

    /**
     * Interval at which the cleanup task runs in seconds.
     */
    private static final long DEFAULT_CLEANUP_INTERVAL_IN_SEC = 1;

    /**
     * Scheduled executor service for running the cleanup task periodically.
     */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Initializes the cache and starts the cleanup task.
     * The cleanup task runs at a fixed interval to remove expired entries.
     */
    public TTLMediaRequestCache() {
        this.cache = new LinkedHashMap<>(16, 0.75f, true); // LRU-style access order
        scheduler.scheduleAtFixedRate(this::cleanup, DEFAULT_CLEANUP_INTERVAL_IN_SEC, DEFAULT_CLEANUP_INTERVAL_IN_SEC, TimeUnit.SECONDS);
    }

    /**
     * Stores a media ID associated with a token in the cache.
     * The entry will expire after the default TTL period.
     *
     * @param token   the token used as the cache key.
     * @param mediaId the media ID to be cached.
     */
    @Override
    public synchronized void putMediaId(String token, String mediaId) {
        cache.put(token, new CacheEntry(mediaId, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(DEFAULT_TTL_IN_SEC)));
    }

    /**
     * Retrieves the media ID associated with the given token.
     * If the token is expired or not found, it returns {@code null} and removes the entry from the cache.
     *
     * @param token the token used to look up the media ID.
     * @return the associated media ID, or {@code null} if not found or expired.
     */
    @Override
    public synchronized String getMediaId(String token) {
        var entry = cache.get(token);
        if (entry == null || System.currentTimeMillis() > entry.expiryTime) {
            logger.info("Remove token {} from cache, media id : {}", token, entry);
            cache.remove(token);
            return null;
        }
        return entry.value;
    }

    /**
     * Checks whether a given token exists in the cache.
     *
     * @param token the token to check.
     * @return {@code true} if the token exists, {@code false} otherwise.
     */
    @Override
    public synchronized boolean isValidToken(String token) {
        logger.info("Check token {} in cache", token);
        return cache.containsKey(token);
    }

    /**
     * Removes expired cache entries.
     * This method is periodically executed by the scheduled cleanup task.
     */
    private synchronized void cleanup() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().expiryTime < now);
    }

    /**
     * Shuts down the scheduled executor service to prevent memory leaks.
     * This method is automatically invoked when the application is shutting down.
     */
    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
    }

    /**
     * Represents a cache entry storing a media ID with its expiration timestamp.
     *
     * @param value      the cached media ID.
     * @param expiryTime the timestamp (in milliseconds) after which the entry is considered expired.
     */
    private record CacheEntry(String value, long expiryTime) {
    }
}
