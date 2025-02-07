package io.crunch.viewer;

public class ViewerUrls {

    public static final String GENERAL_ERROR_PAGE = "/error/generalError.xhtml";

    public static final String NOT_FOUND_ERROR_PAGE = "/error/fileNotFound.xhtml";

    public static String getViewerUrl(String contentType) {
        return switch (contentType) {
            case "image/jpeg", "image/png" -> "/view/image.xhtml";
            case "application/pdf" -> "/view/pdf.xhtml";
            case "audio/mpeg" -> "/view/audio.xhtml";
            case "video/mp4" -> "/view/video.xhtml";
            default -> throw new IllegalArgumentException("Unsupported content type: " + contentType);
        };
    }
}
