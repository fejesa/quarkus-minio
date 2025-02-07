package io.crunch.media.resource;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ContentTypeCheckerTest {

    @ParameterizedTest
    @MethodSource("mediaSources")
    void checkContentType(String fileName, String expectedType) throws Exception {
        var contentTypeChecker = new ContentTypeChecker();
        var path = resolvePath(fileName);
        var result = contentTypeChecker.getContentType(path, path.getFileName().toString());
        assertThat(result).isEqualTo(expectedType);
    }

    private static Path resolvePath(String fileName) throws URISyntaxException {
        var url = ContentTypeCheckerTest.class.getResource(fileName);
        return Path.of(Objects.requireNonNull(url).toURI());
    }

    private static Stream<Arguments> mediaSources() {
        return Stream.of(
                Arguments.of("/sample-audio.mp3", "audio/mpeg"),
                Arguments.of("/sample-video.mp4", "video/mp4"),
                Arguments.of("/sample-image.jpg", "image/jpeg"),
                Arguments.of("/sample-image.png", "image/png"),
                Arguments.of("/sample-pdf.pdf", "application/pdf")
        );
    }
}
