package io.crunch.store;

import jakarta.enterprise.context.ApplicationScoped;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@ApplicationScoped
public class MediaFileContentProvider {

    private final MediaFileStore mediaFileStore;

    public MediaFileContentProvider(MediaFileStore mediaFileStore) {
        this.mediaFileStore = mediaFileStore;
    }

    /**
     * Creates a {@link StreamedContent} object for serving media content, such as audio or video, on the viewer page.
     * This method constructs a {@link StreamedContent} using the provided file name (a.k.a. media id) and content type.
     * The file is fetched from the media storage, and its content is streamed for consumption by the client.
     *
     * @param fileName the name of the file to be served. This should be the exact name as stored in the {@code mediaFileStore}.
     * @param contentType the MIME type of the content being served (e.g., "audio/mpeg", "video/mp4").
     *                    This ensures that the content is rendered correctly in the client's browser.
     * @return a {@link StreamedContent} object that encapsulates the media content, ready to be streamed to the client.
     *
     * The {@link StreamedContent} is built with the following properties:
     * <ul>
     *   <li><b>contentType:</b> The MIME type of the media content provided by the {@code contentType} parameter.</li>
     *   <li><b>contentLength:</b> The size of the media file, obtained from the {@code MediaFileStore}.
     *   It can be used by the browser to calculate the media length for example in sec.</li>
     *   <li><b>InputStream:</b> A {@link java.util.function.Supplier} that streams the content of the file from the {@code MediaFileStore}.</li>
     * </ul>
     *
     * @see DefaultStreamedContent
     * @see StreamedContent
     */
    public StreamedContent readContent(String fileName, String contentType) {
        return DefaultStreamedContent.builder()
                .contentType(contentType)
                .contentLength(mediaFileStore.getFileSize(fileName))
                .stream(() -> mediaFileStore.read(fileName))
                .build();
    }
}
