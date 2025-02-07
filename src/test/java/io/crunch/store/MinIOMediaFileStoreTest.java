package io.crunch.store;

import io.crunch.shared.MediaFileServerException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;

import static org.assertj.core.api.Assertions.*;

@QuarkusTest
class MinIOMediaFileStoreTest {

    @Inject
    MinIOMediaFileStore mediaFileStore;

    @Test
    void throwErrorWhenFileDoesNotExist() {
        var thrown = catchThrowable(() -> mediaFileStore.store(Path.of("file-does-not-exist"), "random", "text/plain"));
        assertThat(thrown).isInstanceOf(MediaFileServerException. class).hasMessage("Error storing file");
    }

    @Test
    void validFileShouldBeStored() throws Exception {
        var path = getSampleMediaFile("/sample-image.png");
        String fileName = "random-image-name";
        mediaFileStore.store(path, fileName, "image/png");
        try (var inputStream = mediaFileStore.read(fileName)) {
            assertThat(inputStream).isNotNull();
        }
    }

    private Path getSampleMediaFile(String path) throws URISyntaxException {
        var url = MinIOMediaFileStoreTest.class.getResource(path);
        return Path.of(Objects.requireNonNull(url).toURI());
    }
}
