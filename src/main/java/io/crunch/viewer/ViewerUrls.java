package io.crunch.viewer;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ViewerUrls {

    public static final String GENERAL_ERROR_PAGE = "/error/generalError.xhtml";

    public static final String NOT_FOUND_ERROR_PAGE = "/error/fileNotFound.xhtml";

    public static final String TOO_MANY_REQUESTS_ERROR_PAGE = "/error/tooManyRequests.xhtml";

    public static String getViewerUrl(String contentType) {
        return switch (contentType) {
            case "jpg", "jp2", "jpeg", "png" -> "/view/image.xhtml";
            case "pdf" -> "/view/pdf.xhtml";
            case "audio/mpeg" -> "/view/audio.xhtml";
            case "mp4" -> "/view/video.xhtml";
            default -> throw new IllegalArgumentException("Unsupported content type: " + contentType);
        };
    }
}
