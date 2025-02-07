package io.crunch.viewer;

import io.crunch.media.MediaFile;
import io.crunch.media.MediaFiles;
import io.crunch.shared.MediaFileNotFoundException;
import io.crunch.store.MediaFileContentProvider;
import io.crunch.store.MediaFileStore;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.omnifaces.util.Faces;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@QuarkusTest
class ImageViewTest {

    @Inject
    MediaRequestCache requestCache;

    @Inject
    MediaFileContentProvider mediaFileContentProvider;

    @InjectMock
    MediaFiles mediaFilesService;

    @InjectMock
    MediaFileStore mediaFileStore;

    private static MockedStatic<Faces> faces;

    @BeforeAll
    static void init() {
        faces = mockStatic(Faces.class);
    }

    @AfterAll
    static void close() {
        faces.close();
    }

    @Test
    void fetchImageBasedOnQueryParam() {
        var mediaFile = new MediaFile();
        mediaFile.setMediaId("69I7Ky0oQlmOKauiUoPJ-Q");
        mediaFile.setMediaType("image/png");
        when(mediaFilesService.getByMediaId(anyString())).thenReturn(Optional.of(mediaFile));
        when(mediaFileStore.read(anyString())).thenReturn(new ByteArrayInputStream("content".getBytes()));
        faces.when(() -> Faces.getRequestParameter(MediaViewerRequestParameters.MEDIA_ID_QUERY_PARAMETER)).thenReturn("69I7Ky0oQlmOKauiUoPJ-Q");

        var imageView = new ImageView(requestCache, mediaFilesService, mediaFileContentProvider);
        assertThat(imageView.getMedia()).isNotNull();
        assertThat(imageView.getMedia().getContentType()).isEqualTo("image/png");
        assertThat(imageView.getMedia().getStream().get()).isNotNull();
    }

    @Test
    void fetchImageBasedOnCookieParam() {
        var token = UUID.randomUUID().toString();
        var mediaFile = new MediaFile();
        mediaFile.setMediaId("123456789");
        mediaFile.setMediaType("image/png");
        when(mediaFilesService.getByMediaId(anyString())).thenReturn(Optional.of(mediaFile));
        when(mediaFileStore.read(anyString())).thenReturn(new ByteArrayInputStream("content".getBytes()));
        requestCache.putMediaId(token, "123456789");
        faces.when(() -> Faces.getRequestCookie(MediaViewerRequestParameters.TOKEN_COOKIE_NAME)).thenReturn(token);
        faces.when(() -> Faces.getRequestParameter(MediaViewerRequestParameters.MEDIA_ID_QUERY_PARAMETER)).thenReturn(null);

        var imageView = new ImageView(requestCache, mediaFilesService, mediaFileContentProvider);
        assertThat(imageView.getMedia()).isNotNull();
        assertThat(imageView.getMedia().getContentType()).isEqualTo("image/png");
        assertThat(imageView.getMedia().getStream().get()).isNotNull();
        await()
            .atMost(6, SECONDS)
            .untilAsserted(() -> assertThat(requestCache.getMediaId(token)).isNull());
    }

    @Test
    void throwErrorWhenNoParams() {
        faces.when(() -> Faces.getRequestParameter(MediaViewerRequestParameters.MEDIA_ID_QUERY_PARAMETER)).thenReturn(null);
        faces.when(() -> Faces.getRequestCookie(MediaViewerRequestParameters.TOKEN_COOKIE_NAME)).thenReturn(null);

        assertThatThrownBy(() -> new ImageView(requestCache, mediaFilesService, mediaFileContentProvider))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No media id or cookie param found in request for image fetch");
    }

    @Test
    void throwErrorWhenMediaFileNotFound() {
        when(mediaFilesService.getByMediaId(anyString())).thenReturn(Optional.empty());
        faces.when(() -> Faces.getRequestParameter(MediaViewerRequestParameters.MEDIA_ID_QUERY_PARAMETER)).thenReturn("69I7Ky0oQlmOKauiUoPJ-Q");

        assertThatThrownBy(() -> new ImageView(requestCache, mediaFilesService, mediaFileContentProvider))
                .isInstanceOf(MediaFileNotFoundException.class)
                .hasMessage("Image file not found: 69I7Ky0oQlmOKauiUoPJ-Q");
    }
}
