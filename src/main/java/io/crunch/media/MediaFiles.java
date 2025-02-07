package io.crunch.media;

import java.util.Optional;

public interface MediaFiles {

    /**
     * Stores a new media file.
     * We assume that the file is already validated - for example the checksum value is verified.
     *
     * @param mediaId   The media public identifier is unique, and it is used to access the media file.
     * @param contentType The type of the media file, for example audio/mpeg.
     * @return The id of the {@link MediaFile}.
     */
    long store(String mediaId, String contentType);

    /**
     * Gets a media file by its media public identifier.<p>
     * This method maybe rate-limited, and it throws a {@link io.smallrye.faulttolerance.api.RateLimitException} if the rate limit is exceeded.
     *
     * @param mediaId The media public identifier.
     * @return The media file if it is available, otherwise empty {@link Optional}.
     */
    Optional<MediaFile> getByMediaId(String mediaId);
}
