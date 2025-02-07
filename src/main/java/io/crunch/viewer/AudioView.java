package io.crunch.viewer;

import io.crunch.media.MediaFiles;
import io.crunch.shared.MediaFileNotFoundException;
import io.crunch.store.MediaFileContentProvider;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import org.omnifaces.util.Faces;
import org.primefaces.model.StreamedContent;

/**
 * A JSF managed bean responsible for handling audio media requests.
 * <p>
 * This class retrieves an audio file based on either a media ID passed as a request parameter
 * or a token stored in a request cookie. If a valid media file is found, its content is streamed
 * to the client.
 * </p>
 */
@Named
@RequestScoped
public class AudioView {

    private final StreamedContent media;

    /**
     * Constructs an {@code AudioView} instance and initializes the streamed media content.
     * <p>
     * The media file is identified using either:
     * <ul>
     *   <li>A media ID provided as a request parameter ({@code MediaViewerRequestParameters.MEDIA_ID_QUERY_PARAMETER}).</li>
     *   <li>A token stored in a request cookie ({@code MediaViewerRequestParameters.TOKEN_COOKIE_NAME}),
     *       which is resolved to a media ID using {@code MediaRequestCache}.</li>
     * </ul>
     * If neither a valid media ID nor a token is provided, an {@code IllegalArgumentException} is thrown.
     * If the media file corresponding to the resolved media ID is not found, a {@code MediaFileNotFoundException} is thrown.
     * </p>
     *
     * @param requestCache               the cache for resolving media IDs from token cookies
     * @param mediaFiles                 the service for retrieving media file metadata
     * @param mediaFileContentProvider   the provider for reading and streaming media file content
     * @throws IllegalArgumentException  if no media ID or token cookie is found in the request
     * @throws MediaFileNotFoundException if the requested audio file does not exist
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
     * Returns the streamed content of the requested audio file.
     * <p>
     * This content is obtained from {@code MediaFileContentProvider} and is intended
     * for use in JSF views, typically with PrimeFaces' {@code p:audio} component.
     * </p>
     *
     * @return the streamed content of the audio file, never {@code null}
     */
    public StreamedContent getMedia() {
        return media;
    }

    /**
     * Returns the file extension of the audio file.
     * <p>
     * Currently, this implementation assumes that all audio files are in MP3 format.
     * If additional formats are supported in the future, this method may need to be updated.
     * </p>
     *
     * @return the file extension of the audio file, always {@code "mp3"}
     */
    public String getExtension() {
        return "mp3";
    }
}
