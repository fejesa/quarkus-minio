package io.crunch.viewer;

public class MediaViewerRequestParameters {

    public static final String MEDIA_ID_QUERY_PARAMETER = "m";

    public static final String TOKEN_COOKIE_NAME = "media-token";

    private MediaViewerRequestParameters() {
        throw new IllegalStateException("Utility class");
    }
}
