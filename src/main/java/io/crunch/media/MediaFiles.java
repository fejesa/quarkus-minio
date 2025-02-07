package io.crunch.media;

import java.util.List;
import java.util.Optional;

public interface MediaFiles {

    /**
     * Stores a new media file.
     * We assume that the file is already validated - for example the checksum value is verified.
     *
     * @param mediaId   The media identifier is unique, and it is used to access the media file.
     * @param contentType The type of the media file, for example audio/mpeg.
     * @return The id of the {@link MediaFile}.
     */
    long store(String mediaId, String contentType);

    /**
     * Gets a media file by its media identifier.<p>
     *
     * @param mediaId The media identifier.
     * @return The media file if it is available, otherwise empty {@link Optional}.
     */
    Optional<MediaFile> getByMediaId(String mediaId);

    /**
     * Gets a list of all media identifiers.
     *
     * @return A list of media identifiers.
     */
    List<String> getMediaIds();
}
