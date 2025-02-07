package io.crunch.viewer;

import io.crunch.media.MediaFiles;
import io.crunch.shared.MediaFileNotFoundException;
import io.crunch.store.MediaFileContentProvider;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import org.omnifaces.util.Faces;
import org.primefaces.model.StreamedContent;

/**
 * A JSF managed bean responsible for handling PDF media requests.
 * <p>
 * This class retrieves an PDF file based on either a media ID passed as a request parameter
 * or a token stored in a request cookie. If a valid media file is found, its content is streamed
 * to the client.
 * </p>
 */
@Named
@RequestScoped
public class DocumentView {

    private final StreamedContent media;

    /**
     * Constructs an {@code DocumentView} instance and initializes the streamed media content.
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
     * @throws MediaFileNotFoundException if the requested image file does not exist
     */
    public DocumentView(MediaRequestCache requestCache, MediaFiles mediaFiles, MediaFileContentProvider mediaFileContentProvider) {
        var param = Faces.getRequestParameter(MediaViewerRequestParameters.MEDIA_ID_QUERY_PARAMETER);
        var cookie = Faces.getRequestCookie(MediaViewerRequestParameters.TOKEN_COOKIE_NAME);
        if (param == null && cookie == null) {
            throw new IllegalArgumentException("No media id or cookie param found in request for document fetch");
        }
        var mediaId = param != null ? param : requestCache.getMediaId(cookie);
        var mediaFile = mediaFiles.getByMediaId(mediaId).orElseThrow(() -> new MediaFileNotFoundException("Document file not found: " + mediaId));
        media = mediaFileContentProvider.readContent(mediaFile.getMediaId(), mediaFile.getMediaType());
    }

    /**
     * Returns the streamed content of the requested PDF file.
     * @return the streamed content of the PDF file, never {@code null}
     */
    public StreamedContent getMedia() {
        return media;
    }
}
