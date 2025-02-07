package io.crunch.viewer;

import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class LocalMediaRequestCache implements MediaRequestCache {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Map<String, String> requestIdCache = new HashMap<>();

    @Override
    public void putMediaId(String token, String mediaId) {
        requestIdCache.put(token, mediaId);
    }

    @Override
    public String getMediaId(String token) {
        var removed = requestIdCache.remove(token);
        logger.info("Remove token {} from cache, media id : {}", token, removed);
        return removed;
    }

    @Override
    public boolean isValidToken(String token) {
        logger.info("Check token {} in cache", token);
        return requestIdCache.containsKey(token);
    }
}
