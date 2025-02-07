package io.crunch.media;

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
}
