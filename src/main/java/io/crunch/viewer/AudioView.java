package io.crunch.viewer;

import io.crunch.media.MediaFiles;
import io.crunch.shared.MediaFileNotFoundException;
import io.crunch.store.MediaFileContentProvider;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import org.omnifaces.util.Faces;
import org.primefaces.model.StreamedContent;

/**
 * This class is a JSF managed bean responsible for handling audio media requests.
 * It retrieves the audio file based on the media ID or token cookie from the request.
 */
@Named
@RequestScoped
public class AudioView {

    private final StreamedContent media;

    /**
     * Constructs an `AudioView` instance.
     *
     * @param requestCache the cache for media requests
     * @param mediaFiles the service to access media files
     * @param mediaFileContentProvider the provider to read media file content
     * @throws IllegalArgumentException if no media ID or token cookie is found in the request or if the media ID is invalid
     * @throws MediaFileNotFoundException if the audio file is not found
     */
    public AudioView(MediaRequestCache requestCache, MediaFiles mediaFiles, MediaFileContentProvider mediaFileContentProvider) {
        var param = Faces.getRequestParameter(MediaViewerRequestParameters.MEDIA_ID_QUERY_PARAMETER);
        var cookie = Faces.getRequestCookie(MediaViewerRequestParameters.TOKEN_COOKIE_NAME);
        if (param == null && cookie == null) {
            throw new IllegalArgumentException("No media id or cookie param found in request for audio fetch");
        }
        var mediaId = param != null ? param : requestCache.getMediaId(cookie);
        var mediaFile = mediaFiles.getByMediaId(mediaId).orElseThrow(() -> new MediaFileNotFoundException("Audio file not found: " + mediaId));
        media = mediaFileContentProvider.readContent(mediaFile.getMediaId(), mediaFile.getMediaType());
    }

    /**
     * Returns the streamed content of the audio file.
     *
     * @return the streamed content of the audio file
     */
    public StreamedContent getMedia() {
        return media;
    }

    /**
     * Returns the file extension of the audio file.
     *
     * @return the file extension of the audio file
     */
    public String getExtension() {
        return "mp3";
    }
}
