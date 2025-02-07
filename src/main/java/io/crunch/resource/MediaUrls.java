package io.crunch.resource;

import io.crunch.shared.MediaFileServerException;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64.Encoder;

import static java.net.URLDecoder.decode;
import static java.util.stream.Collectors.*;

/**
 * Generates a unique URL for media files in the format like <b>http://localhost:8080/media?m=YZmdnnBYTH2lZbAbHvaqnA</b>
 * <p>
 * The query parameter is a base64 unique id with length 22 based on random UUID.
 */
@ApplicationScoped
public class MediaUrls {

    /** Defines the length of the media file unique identifier that is part of the public url. **/
    private static final int MEDIA_FILE_UNIQUE_ID_LENGTH = 22;

    private final Encoder encoder;

    private final int fileServerPort;

    public MediaUrls(@ConfigProperty(name = "quarkus.http.port", defaultValue = "8080") int fileServerPort) {
        this.encoder = Base64.getUrlEncoder();
        this.fileServerPort = fileServerPort;
    }

    public String createUrl() {
        var query = "m=" + randomId();
        try {
            var url = new URI("http", null, "localhost", fileServerPort, "/media", query, null);
            return url.toString();
        } catch (URISyntaxException e) {
            throw new MediaFileServerException(e);
        }
    }

    /**
     * Extracts the media file unique identifier from the specified URL.
     * <p>
     * This method parses the URL and extracts the media file unique identifier from the query parameters.
     * </p>
     *
     * @param url the URL from which to extract the media file unique identifier.
     * @return the media file unique identifier extracted from the URL.
     * @throws IllegalArgumentException if the URL is invalid or the media file unique identifier is missing.
     */
    public String getMediaId(String url) {
        try {
            var uri = new URI(url);
            return splitQuery(uri).getOrDefault("m", List.of())
                    .stream()
                    .filter(StringUtils::isNotBlank)
                    .findAny().orElseThrow(() -> new IllegalArgumentException("Missing query parameter from URL: " + url));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
    }

    private Map<String, List<String>> splitQuery(URI uri) {
        if (StringUtils.isBlank(uri.getQuery())) {
            return Map.of();
        }
        return Arrays.stream(uri.getQuery().split("&"))
                .map(this::splitQueryParameter)
                .collect(groupingBy(AbstractMap.SimpleImmutableEntry::getKey, LinkedHashMap::new, mapping(Map.Entry::getValue, toList())));
    }

    private AbstractMap.SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
        var idx = it.indexOf("=");
        var key = idx > 0 ? it.substring(0, idx) : it;
        var value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : "";
        return new AbstractMap.SimpleImmutableEntry<>(
                decode(key, StandardCharsets.UTF_8),
                decode(value, StandardCharsets.UTF_8)
        );
    }

    public String randomId() {
        var uuid = UUID.randomUUID();
        var src = ByteBuffer.wrap(new byte[16])
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
        // Encode to Base64 and remove trailing ==
        return encoder.encodeToString(src).substring(0, MEDIA_FILE_UNIQUE_ID_LENGTH);
    }
}
