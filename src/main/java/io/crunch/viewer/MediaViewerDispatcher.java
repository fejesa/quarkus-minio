package io.crunch.viewer;

import io.crunch.media.MediaFile;
import io.crunch.media.MediaFiles;
import io.crunch.shared.MediaFileNotFoundException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serial;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.UUID;

/**
 * The {@code MediaViewerDispatcher} class is a servlet responsible for handling media requests and forwarding them to
 * appropriate viewers based on the media type. It processes HTTP GET requests and interacts with various components
 * such as media files, token caches, and viewer URLs.
 */
@WebServlet(urlPatterns = "/media")
public class MediaViewerDispatcher extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1;

    private final transient Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final transient MediaFiles mediaFiles;

    private final transient MediaRequestCache requestCache;

    /**
     * Constructs a {@code MediaViewerDispatcher} servlet with the specified dependencies.
     *
     * @param mediaFiles    the repository or service responsible for handling media files
     * @param tokenCache    the cache that stores associations between generated tokens and media IDs
     * @param tokenGenerator the utility that generates unique tokens
     * @param mediaUrls the utility that validates media IDs
     * @param viewerUrls    the utility that provides URLs for different media viewers based on the type of media
     */
    public MediaViewerDispatcher(MediaFiles mediaFiles, MediaRequestCache requestCache) {
        this.mediaFiles = mediaFiles;
        this.requestCache = requestCache;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /**
     * Handles the HTTP GET request by forwarding the request and response to the appropriate media viewer.<p>
     * If an exception occurs during the processing of the request, it is logged and an error page is displayed.
     *
     * @param request  the {@link HttpServletRequest} object that contains the request the client has made of the servlet
     * @param response the {@link HttpServletResponse} object that contains the response the servlet sends to the client
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            forward(request, response);
        } catch (Exception e) {
            logger.error("Media file request cannot be processed", e);
            try {
                handleException(e, request, response);
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    /**
     * Forwards the request and response to the appropriate viewer URL based on the media type.
     *
     * <p>This method performs the following steps:</p>
     * <ul>
     *     <li>Retrieves the media ID from the request parameters.</li>
     *     <li>Retrieves the media type from the media repository.</li>
     *     <li>Saves the media request by generating a token and adding it to the response as a cookie.</li>
     *     <li>Forwards the request to the appropriate viewer URL.</li>
     * </ul>
     *
     * @param request  the {@link HttpServletRequest} object that contains the request the client has made of the servlet
     * @param response the {@link HttpServletResponse} object that contains the response the servlet sends to the client
     * @throws ServletException if the request could not be forwarded
     * @throws IOException      if an input or output error occurs while forwarding the request
     * @throws IllegalArgumentException if the media ID is missing from the request parameters
     * @throws MediaFileNotFoundException if the media file corresponding to the given media ID is not found
     */
    private void forward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get media id from request parameter
        var mediaId = Optional.ofNullable(request.getParameter(MediaViewerRequestParameters.MEDIA_ID_QUERY_PARAMETER)).orElseThrow(IllegalArgumentException::new);

        // Get media type by id from the repository
        var mediaType = mediaFiles.getByMediaId(mediaId)
                .map(MediaFile::getMediaType).orElseThrow(() -> new MediaFileNotFoundException("Media file not found or empty: " + mediaId));

        // Save media request
        saveMediaRequest(response, mediaId);

        // Get the redirect URL based on the media type
        var url = ViewerUrls.getViewerUrl(mediaType);
        var dispatcher = getServletContext().getRequestDispatcher(url);
        logger.info("Forward request to {}", url);
        dispatcher.forward(request, response);
    }

    /**
     * Saves the media request by generating a token, setting it as a cookie, and associating it with the media ID in the cache.
     *
     * @param response the {@link HttpServletResponse} object that contains the response the servlet sends to the client
     * @param mediaId  the ID of the media file being requested
     */
    private void saveMediaRequest(HttpServletResponse response, String mediaId) {
        var token = createMediaRequestToken();
        logger.info("Set {} cookie: {}", MediaViewerRequestParameters.TOKEN_COOKIE_NAME, token);
        addMediaIdCookie(response, token);
        requestCache.putMediaId(token, mediaId);
    }

    private String createMediaRequestToken() {
        return UUID.randomUUID().toString();
    }

    private void addMediaIdCookie(HttpServletResponse response, String token) {
        var cookie = new Cookie(MediaViewerRequestParameters.TOKEN_COOKIE_NAME, token);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        var cookieHeaderValue = "%s=%s; Path=%s; HttpOnly; SameSite=None".formatted(
                cookie.getName(),
                cookie.getValue(),
                cookie.getPath());
        if (cookie.getSecure()) {
            cookieHeaderValue += "; Secure";
        }

        response.addHeader("Set-Cookie", cookieHeaderValue);
    }

    private void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (e instanceof MediaFileNotFoundException || e instanceof IllegalArgumentException) {
            response.setStatus(404); // Not Found
            request.getRequestDispatcher(ViewerUrls.NOT_FOUND_ERROR_PAGE).forward(request, response);
        } else {
            response.setStatus(500); // Internal Server Error
            request.getRequestDispatcher(ViewerUrls.GENERAL_ERROR_PAGE).forward(request, response);
        }
    }
}
